package com.example.inutri.ui.detection;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.graphics.ImageDecoder;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.inutri.ml.VisionConfig;
import com.example.inutri.ml.VisionService;
import com.example.inutri.model.DetectedFood;
import com.google.mlkit.vision.common.InputImage;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetectionResultViewModel extends AndroidViewModel {

    private final MutableLiveData<List<DetectedFood>> detectedFoods = new MutableLiveData<>();
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final VisionService vision;

    public DetectionResultViewModel(@NonNull Application application) {
        super(application);
        // threshold e janela ficarão úteis no próximo passo (smoothing)
        this.vision = new VisionService(application.getApplicationContext(),
                new VisionConfig(0.50f, 5));
    }

    public LiveData<List<DetectedFood>> getDetectedFoods() {
        return detectedFoods;
    }

    public void detectFoods(@NonNull Uri photoUri) {
        io.execute(() -> {
            Bitmap bmp = null;
            try {
                bmp = loadBitmap(photoUri);
                InputImage image = InputImage.fromBitmap(bmp, 0);
                List<DetectedFood> result = vision.detect(image);
                detectedFoods.postValue(result);
            } catch (Exception e) {
                e.printStackTrace();
                detectedFoods.postValue(Collections.emptyList());
            } finally {
                if (bmp != null && !bmp.isRecycled()) bmp.recycle();
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        io.shutdown();
    }

    // --- helpers ---

    private Bitmap loadBitmap(@NonNull Uri uri) throws Exception {
        if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.Source src = ImageDecoder.createSource(
                    getApplication().getContentResolver(), uri);
            return ImageDecoder.decodeBitmap(src);
        } else {
            return MediaStore.Images.Media.getBitmap(
                    getApplication().getContentResolver(), uri);
        }
    }
}
