package com.example.inutri.data.remote;

import android.content.Context;

import androidx.annotation.WorkerThread;

import com.example.inutri.model.DetectedFood;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class OnDeviceLabeler {

    private final ImageLabeler labeler;

    public OnDeviceLabeler(Context ctx) {
        // Modelo “default” on-device
        this.labeler = ImageLabeling.getClient(
                new ImageLabelerOptions.Builder().setConfidenceThreshold(0.5f).build()
        );
    }

    /** Bloqueante (chame em background): rotula a imagem e retorna alimentos prováveis. */
    @WorkerThread
    public List<DetectedFood> detect(InputImage image) throws Exception {
        List<ImageLabel> labels = Tasks.await(
                labeler.process(image),
                2, TimeUnit.SECONDS
        );

        List<DetectedFood> out = new ArrayList<>();
        for (ImageLabel l : labels) {
            // normaliza nome (ex.: "Hot dog" -> "Hot Dog")
            String pretty = toTitleCase(l.getText());
            out.add(new DetectedFood(pretty, l.getConfidence(), null));
        }
        return out;
    }

    private static String toTitleCase(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] parts = s.toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder b = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            b.append(Character.toUpperCase(p.charAt(0)))
                    .append(p.substring(1))
                    .append(' ');
        }
        return b.toString().trim();
    }
}
