package com.example.inutri.ui.capture;

import android.app.Application;
import android.media.Image;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.inutri.data.repository.NutritionRepository;
import com.example.inutri.ml.VisionConfig;
import com.example.inutri.ml.VisionService;
import com.example.inutri.model.DetectedFood;
import com.example.inutri.model.api.NutritionResponse;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CaptureViewModel extends AndroidViewModel {

    public enum UiState { IDLE, LOADING, READY, ERROR }

    private final MutableLiveData<UiState> uiState = new MutableLiveData<>(UiState.IDLE);
    private final MutableLiveData<List<DetectedFood>> detectedFoods = new MutableLiveData<>();
    private final MutableLiveData<NutritionResponse> nutritionResult = new MutableLiveData<>();

    private final VisionService vision;
    private final NutritionRepository nutritionRepository = new NutritionRepository();
    private final ExecutorService rtExecutor = Executors.newSingleThreadExecutor();

    private volatile String lastErrorText = null;

    public LiveData<UiState> getUiState() { return uiState; }
    public LiveData<List<DetectedFood>> getDetectedFoods() { return detectedFoods; }
    public LiveData<NutritionResponse> getNutritionResult() { return nutritionResult; }
    public String getLastErrorText() { return lastErrorText; }

    public CaptureViewModel(@NonNull Application app) {
        super(app);
        vision = new VisionService(app.getApplicationContext(), new VisionConfig(0.50f, 5));
    }

    public void onRealtimeFrame(ImageProxy proxy) {
        // Se estiver em modo LOADING, ignora
        if (uiState.getValue() == UiState.LOADING) {
            proxy.close();
            return;
        }

        @OptIn(markerClass = ExperimentalGetImage.class) Image mediaImage = proxy.getImage();
        if (mediaImage == null) { proxy.close(); return; }
        final int rotation = proxy.getImageInfo().getRotationDegrees();
        final InputImage input = InputImage.fromMediaImage(mediaImage, rotation);

        rtExecutor.execute(() -> {
            try {
                List<DetectedFood> list = vision.detect(input);
                detectedFoods.postValue(list);
                // REMOVIDO: uiState.postValue(UiState.READY); 
                // A análise em tempo real não deve resetar o estado global de loading
            } catch (Exception e) {
                // Silencioso para não interromper a UI
            } finally {
                proxy.close();
            }
        });
    }

    public void startLoading() {
        uiState.setValue(UiState.LOADING);
    }

    public void detectFoods(Uri imageUri) {
        uiState.setValue(UiState.LOADING); // Use setValue para garantir execução imediata se estiver na main thread
        nutritionRepository.analyzeImage(getApplication(), imageUri, new NutritionRepository.NutritionCallback() {
            @Override
            public void onSuccess(NutritionResponse response) {
                nutritionResult.postValue(response);
                uiState.postValue(UiState.READY);
            }

            @Override
            public void onError(String message) {
                lastErrorText = message;
                uiState.postValue(UiState.ERROR);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        rtExecutor.shutdown();
    }
}
