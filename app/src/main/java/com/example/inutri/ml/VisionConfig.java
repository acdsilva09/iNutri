package com.example.inutri.ml;

public final class VisionConfig {
    public final float rawThreshold;
    public final int windowSize;   // usado no próximo passo (smoothing)
    public VisionConfig(float rawThreshold, int windowSize) {
        this.rawThreshold = rawThreshold;
        this.windowSize = windowSize;
    }
}
