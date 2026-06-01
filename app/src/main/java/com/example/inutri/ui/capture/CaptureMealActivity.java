package com.example.inutri.ui.capture;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inutri.databinding.ActivityCaptureMealBinding;
import com.example.inutri.model.DetectedFood;
import com.example.inutri.model.api.NutritionResponse;
import com.example.inutri.ui.detection.DetectedFoodAdapter;
import com.example.inutri.ui.detection.DetectionResultActivity;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CaptureMealActivity extends AppCompatActivity {

    private ActivityCaptureMealBinding binding;
    private CaptureViewModel viewModel;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private Uri lastCapturedUri;

    private final Map<String, String> gramsMap = new HashMap<>();

    private long lastAnalysisTs = 0L;
    private static final long ANALYSIS_DEBOUNCE_MS = 250L;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
                else {
                    Toast.makeText(this, "Permissão de câmera negada", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCaptureMealBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ✅ Adapter SEM parâmetros



        viewModel = new ViewModelProvider(this).get(CaptureViewModel.class);
        cameraExecutor = Executors.newSingleThreadExecutor();

        setupObservers();
        setupUi();
        ensureCameraPermissionAndStart();
    }

    private void setupUi() {
        if (binding.btnCapture != null) {
            binding.btnCapture.setOnClickListener(v -> takePhoto());
        }
    }

    private void setupObservers() {
        // Removido observador de realtime detections (não tem mais recycler nesta tela)
        viewModel.getUiState().observe(this, state -> {
            android.util.Log.d("CaptureActivity", "UI State changed to: " + state);
            
            boolean isLoading = (state == CaptureViewModel.UiState.LOADING);
            
            // Mantém o overlay visível durante o loading
            binding.loadingOverlay.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
            
            // Desabilita o botão de captura para evitar múltiplos cliques
            binding.btnCapture.setEnabled(!isLoading);
            if (isLoading) {
                binding.btnCapture.setAlpha(0.3f);
            } else {
                binding.btnCapture.setAlpha(1.0f);
            }

            if (state == CaptureViewModel.UiState.ERROR) {
                String err = viewModel.getLastErrorText();
                android.util.Log.e("CaptureActivity", "Error state: " + err);
                if (err != null && !err.isEmpty()) {
                    Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getNutritionResult().observe(this, response -> {
            if (response != null) {
                android.util.Log.d("CaptureActivity", "Nutrition result received, navigating...");
                onAnalysisComplete(response);
            }
        });
    }

    private void onAnalysisComplete(NutritionResponse response) {
        Intent i = new Intent(this, DetectionResultActivity.class);
        i.putExtra("nutrition_response", response);
        if (lastCapturedUri != null) {
            i.putExtra("photo_uri", lastCapturedUri.toString());
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        startActivity(i);
    }

    private void ensureCameraPermissionAndStart() {
        boolean granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        if (granted) startCamera();
        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                if (binding.previewView != null) {
                    preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());
                }

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(cameraExecutor, (ImageProxy image) -> {
                    long now = System.currentTimeMillis();
                    if (now - lastAnalysisTs < ANALYSIS_DEBOUNCE_MS) {
                        image.close();
                        return;
                    }
                    lastAnalysisTs = now;
                    viewModel.onRealtimeFrame(image);
                });

                CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, selector, preview, imageCapture, analysis);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erro ao iniciar a câmera", Toast.LENGTH_LONG).show();
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null || viewModel.getUiState().getValue() == CaptureViewModel.UiState.LOADING) return;

        // Inicia o estado de carregamento imediatamente ao clicar
        viewModel.startLoading();

        File dir = new File(getCacheDir(), "images");
        if (!dir.exists()) dir.mkdirs();
        File outFile = new File(dir, "meal_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(outFile).build();

        imageCapture.takePicture(
                options,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        lastCapturedUri = FileProvider.getUriForFile(
                                CaptureMealActivity.this,
                                "com.example.inutri.fileprovider",
                                outFile
                        );
                        grantUriPermission(getPackageName(), lastCapturedUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        onPhotoCaptured(lastCapturedUri);
                    }
                    @Override public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(CaptureMealActivity.this,
                                "Falha na captura: " + exception.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void onPhotoCaptured(@NonNull Uri imageUri) {
        // Inicia a análise na API
        viewModel.detectFoods(imageUri);
    }


    @Override protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }
}
