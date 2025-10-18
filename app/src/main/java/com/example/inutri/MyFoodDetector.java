// com/example/inutri/MyFoodDetector.java
package com.example.inutri;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.graphics.ImageDecoder;

import androidx.annotation.NonNull;

import com.example.inutri.model.DetectedFood;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.List;

public class MyFoodDetector {

    private static volatile MyFoodDetector INSTANCE;
    private final Context appContext;
    private final ImageLabeler labeler;

    private MyFoodDetector(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
        this.labeler = ImageLabeling.getClient(
                new ImageLabelerOptions.Builder()
                        .setConfidenceThreshold(0.50f) // ajuste se quiser mais/menos itens
                        .build()
        );
    }

    public static MyFoodDetector getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (MyFoodDetector.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MyFoodDetector(context);
                }
            }
        }
        return INSTANCE;
    }

    /** Rótula a imagem do Uri e retorna vários DetectedFood (sem bbox). */
    public List<DetectedFood> detectFromUri(@NonNull Uri uri) throws Exception {
        Bitmap bmp = loadBitmap(uri);
        InputImage image = InputImage.fromBitmap(bmp, 0);

        List<ImageLabel> labels = Tasks.await(labeler.process(image));

        List<DetectedFood> out = new ArrayList<>();
        for (ImageLabel l : labels) {
            String label = l.getText();      // ex.: "Bread", "Rice", etc.
            float conf   = l.getConfidence();// 0..1
            // seu modelo DetectedFood(String label, float confidence, RectF bbox)
            out.add(new DetectedFood(label, conf, null));
        }
        return out;
    }

    private Bitmap loadBitmap(@NonNull Uri uri) throws Exception {
        if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.Source src = ImageDecoder.createSource(appContext.getContentResolver(), uri);
            return ImageDecoder.decodeBitmap(src);
        } else {
            return MediaStore.Images.Media.getBitmap(appContext.getContentResolver(), uri);
        }
    }
}
