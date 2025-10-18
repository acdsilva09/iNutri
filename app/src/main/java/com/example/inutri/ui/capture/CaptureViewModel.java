package com.example.inutri.ui.capture;

import android.app.Application;
import android.graphics.ImageFormat;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.inutri.data.remote.OnDeviceLabeler;
import com.example.inutri.model.DetectedFood;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CaptureViewModel extends AndroidViewModel {

    public enum UiState { IDLE, LOADING, READY, ERROR }

    private final MutableLiveData<UiState> uiState = new MutableLiveData<>(UiState.IDLE);
    private final MutableLiveData<List<DetectedFood>> detectedFoods = new MutableLiveData<>();
    private final OnDeviceLabeler onDeviceLabeler;
    private final ExecutorService rtExecutor = Executors.newSingleThreadExecutor();

    private volatile String lastErrorText = null;

    public CaptureViewModel(@NonNull Application application) {
        super(application);
        onDeviceLabeler = new OnDeviceLabeler(application.getApplicationContext());
    }

    public LiveData<UiState> getUiState() { return uiState; }
    public LiveData<List<DetectedFood>> getDetectedFoods() { return detectedFoods; }
    public String getLastErrorText() { return lastErrorText; }

    /** Chame para cada frame do CameraX (em background via analyzer). */
    public void onRealtimeFrame(ImageProxy proxy) {
        // Converte o ImageProxy em InputImage e processa no executor
        @OptIn(markerClass = ExperimentalGetImage.class) Image mediaImage = proxy.getImage();
        if (mediaImage == null) {
            proxy.close();
            return;
        }
        final int rotation = proxy.getImageInfo().getRotationDegrees();
        final InputImage input = InputImage.fromMediaImage(mediaImage, rotation);

        rtExecutor.execute(() -> {
            try {
                List<DetectedFood> list = onDeviceLabeler.detect(input);
                detectedFoods.postValue(list);
                uiState.postValue(UiState.READY);
            } catch (Exception e) {
                lastErrorText = e.getMessage();
                uiState.postValue(UiState.ERROR);
            } finally {
                proxy.close(); // SEMPRE fechar
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        rtExecutor.shutdown();
    }
}
