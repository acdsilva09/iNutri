package com.example.inutri.model;

import android.graphics.RectF;
import androidx.annotation.Nullable;

public class DetectedFood {
    private final String name;
    private final float confidence; // 0..1
    @Nullable private final RectF box; // pode ser null (ML Kit de rótulos não dá caixa)
    private final float gramsEstimate; // estimativa (pode ser 0f)

    public DetectedFood(String name, float confidence, Object o) {
        this(name, confidence, null, 0f);
    }

    public DetectedFood(String name, float confidence, @Nullable RectF box, float gramsEstimate) {
        this.name = name;
        this.confidence = confidence;
        this.box = box;
        this.gramsEstimate = gramsEstimate;
    }

    public String getName() { return name; }
    public float getConfidence() { return confidence; }
    @Nullable public RectF getBox() { return box; }
    public float getGramsEstimate() { return gramsEstimate; }




}
