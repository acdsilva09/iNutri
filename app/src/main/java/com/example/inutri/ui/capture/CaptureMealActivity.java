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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inutri.databinding.ActivityCaptureMealBinding;
import com.example.inutri.model.DetectedFood;
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
        viewModel.getDetectedFoods().observe(this, this::renderDetections);
        viewModel.getUiState().observe(this, state -> {
            if (state == CaptureViewModel.UiState.ERROR) {
                CharSequence err = viewModel.getLastErrorText();
                if (err != null && err.length() > 0) {
                    Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void renderDetections(List<DetectedFood> detections) {
        if (detections == null) detections = Collections.emptyList();
        RecyclerView.Adapter<?> a = binding.recyclerDetected.getAdapter();
        if (a instanceof DetectedFoodAdapter) {
            ((DetectedFoodAdapter) a).submitList(detections);
        }
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
        if (imageCapture == null) return;

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
                        Uri uri = FileProvider.getUriForFile(
                                CaptureMealActivity.this,
                                "com.example.inutri.fileprovider",
                                outFile
                        );
                        grantUriPermission(getPackageName(), uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        onPhotoCaptured(uri);
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
        // Rode a detecção com a foto única
        viewModel.detectFoods(imageUri);

        // Observa UMA VEZ o resultado e navega
        viewModel.getDetectedFoods().observe(this, detections -> {
            viewModel.getDetectedFoods().removeObservers(this);
            if (detections == null) detections = Collections.emptyList();

            Intent i = new Intent(this, DetectionResultActivity.class);

            // ✅ Passe a lista de detecções
            // Se DetectedFood for Parcelable (recomendado):
            i.putParcelableArrayListExtra("detections", new ArrayList<>(detections));

            // Se quiser usar a foto também na próxima tela:
            i.putExtra("photo_uri", imageUri.toString());
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(i);
            viewModel.getPhotoDetections().removeObservers(this);
        });
    }


    @Override protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }
}
