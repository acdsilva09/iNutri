package com.example.inutri;

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
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.example.inutri.databinding.ActivityCaptureMealBinding;
import com.example.inutri.model.DetectedFood;
import com.example.inutri.ui.capture.CaptureViewModel;
import androidx.camera.lifecycle.ProcessCameraProvider;
import com.google.common.util.concurrent.ListenableFuture;
import androidx.core.content.ContextCompat;



import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tela de captura da refeição (Etapa 1 do MVP).
 * - Mostra a câmera (CameraX)
 * - Captura a foto e gera um Uri via FileProvider
 * - Dispara a detecção via ViewModel
 * - Atualiza overlay/lista conforme resultados (você pluga os adapters)
 */
public class CaptureMealActivity extends AppCompatActivity {

    private ActivityCaptureMealBinding binding;
    private CaptureViewModel viewModel;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Permissão de câmera negada", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCaptureMealBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CaptureViewModel.class);
        cameraExecutor = Executors.newSingleThreadExecutor();

        setupObservers();
        setupUi();

        ensureCameraPermissionAndStart();
    }

    private void setupUi() {
        binding.btnCapture.setOnClickListener(v -> takePhoto());

        // Se você tiver uma RecyclerView de itens detectados, plugue os adapters aqui.
        // binding.recyclerDetected.setAdapter(new DetectedFoodAdapter(...));
        // binding.recyclerConfirm.setAdapter(new MealItemAdapter(...));
    }

    private void setupObservers() {
        viewModel.getUiState().observe(this, state -> {
            switch (state) {
                case IDLE:
                    // nada
                    break;
                case LOADING:
                    // exibir um progresso se quiser
                    break;
                case ERROR:
                    Toast.makeText(
                            CaptureMealActivity.this,viewModel.getLastErrorText(),
                            Toast.LENGTH_LONG
                    ).show();
                    break;
                case READY:
                    // resultados prontos (detecções feitas ou itens carregados)
                    break;
            }
        });

        viewModel.getDetectedFoods().observe(this, this::renderDetections);
        // viewModel.getMealItems().observe(this, items -> { /* atualizar lista/totais */ });
        // viewModel.getTotals().observe(this, totals -> { /* atualizar UI com totais */ });
    }

    private void renderDetections(List<DetectedFood> detections) {
        // Desenha as caixas no overlay (seu FoodOverlayView deve aceitar normalizado 0..1 ou px)
        if (binding.overlay != null) {
            binding.overlay.setDetections(detections);
            binding.overlay.invalidate();
        }
        // Atualize a lista dos detectados (adapter) se quiser:
        // detectedFoodAdapter.submitList(detections);
        Toast.makeText(
                this,
                String.format(Locale.getDefault(), "Itens detectados: %d", detections.size()),
                Toast.LENGTH_SHORT
        ).show();
    }

    private void ensureCameraPermissionAndStart() {
        boolean granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        if (granted) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // Analisador opcional (tempo real). Mantemos vazio no MVP.
                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                analysis.setAnalyzer(cameraExecutor, (ImageProxy image) -> {
                    // Se quiser detecção em tempo real, enviar frames aqui
                    image.close();
                });

                CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this, selector, preview, imageCapture, analysis
                );

            } catch (ExecutionException | InterruptedException e) {
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
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri uri = FileProvider.getUriForFile(
                                CaptureMealActivity.this,
                                "com.example.inutri.fileprovider",
                                outFile
                        );
                        onPhotoCaptured(uri);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(CaptureMealActivity.this,
                                "Falha na captura: " + exception.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void onPhotoCaptured(@NonNull Uri imageUri) {
        // 1) dispara detecção (remota ou local) via ViewModel
        viewModel.detectFoods(imageUri);

        // 2) se quiser abrir uma tela de resumo após a confirmação,
        // faça via observer quando MealItems estiver pronto.
        // startActivity(new Intent(this, MealSummaryActivity.class).putExtra("photo", imageUri.toString()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }
}
