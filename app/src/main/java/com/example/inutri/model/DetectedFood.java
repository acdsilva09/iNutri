package com.example.inutri.model;

import android.graphics.RectF;

/** Resultado da detecção na imagem (rótulo + confiança + bbox normalizada). */
public class DetectedFood {
    public String label;       // ex.: "arroz", "feijão", "frango"
    public float confidence;   // 0..1
    public RectF bbox;         // coordenadas normalizadas (0..1) relativas ao preview

    public DetectedFood() { }

    public DetectedFood(String label, float confidence, RectF bbox) {
        this.label = label;
        this.confidence = confidence;
        this.bbox = bbox;
    }
}
