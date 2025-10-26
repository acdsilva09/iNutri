package com.example.inutri.ml;

import android.content.Context;
import androidx.annotation.WorkerThread;
import com.example.inutri.model.DetectedFood;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import java.text.Normalizer;
import java.util.*;

public final class VisionService {

    private final ImageLabeler labeler;
    private final VisionConfig cfg;
    private final Deque<String> window = new ArrayDeque<>();

    public VisionService(Context ctx, VisionConfig cfg) {
        this.cfg = cfg;
        this.labeler = ImageLabeling.getClient(
                new ImageLabelerOptions.Builder().setConfidenceThreshold(cfg.rawThreshold).build()
        );
    }

    @WorkerThread
    public List<DetectedFood> detect(InputImage image) throws Exception {
        List<ImageLabel> labels = Tasks.await(labeler.process(image));
        // passo 1 (Checklist): aqui depois entra allowlist/generics
        List<DetectedFood> out = new ArrayList<>();
        for (ImageLabel l : labels) {
            out.add(new DetectedFood(pretty(l.getText()), l.getConfidence(), null));
        }
        return out;
    }

    // ---- helpers locais (sem novas classes) ----
    private static String pretty(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase(Locale.ROOT);
        String[] parts = t.split("\\s+");
        StringBuilder b = new StringBuilder();
        for (String p: parts) if (!p.isEmpty())
            b.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
        return b.toString().trim();
    }
}
