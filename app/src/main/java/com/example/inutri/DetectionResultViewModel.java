// com/example/inutri/DetectionResultViewModel.java
package com.example.inutri;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.inutri.model.DetectedFood;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class DetectionResultViewModel extends AndroidViewModel {
    private final MutableLiveData<List<DetectedFood>> detectedFoods = new MutableLiveData<>();

    public DetectionResultViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<DetectedFood>> getDetectedFoods() {
        return detectedFoods;
    }

    public void detectFoods(@NonNull Uri photoUri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // usa o singleton passando o Application (context)
                List<DetectedFood> result =
                        MyFoodDetector.getInstance(getApplication()).detectFromUri(photoUri);
                detectedFoods.postValue(result);
            } catch (Exception e) {
                e.printStackTrace();
                detectedFoods.postValue(Collections.emptyList());
            }
        });
    }
}
