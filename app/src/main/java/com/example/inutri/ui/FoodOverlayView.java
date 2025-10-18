package com.example.inutri.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.inutri.model.DetectedFood;

import java.util.ArrayList;
import java.util.List;

/**
 * Espera bboxes normalizadas (0..1) em DetectedFood.bbox.
 * Desenha um retângulo com rótulo no Preview.
 */
public class FoodOverlayView extends View {

    private final Paint boxPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final RectF temp = new RectF();

    private List<DetectedFood> detections = new ArrayList<>();

    public FoodOverlayView(Context context) {
        super(context);
        init();
    }

    public FoodOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FoodOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);

        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(5f);
        boxPaint.setAntiAlias(true);

        textPaint.setTextSize(36f);
        textPaint.setAntiAlias(true);
    }

    public void setDetections(List<DetectedFood> list) {
        detections = (list == null) ? new ArrayList<>() : list;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (detections == null || detections.isEmpty()) return;

        final float w = getWidth();
        final float h = getHeight();

        for (int i = 0; i < detections.size(); i++) {
            DetectedFood d = detections.get(i);
            if (d == null || d.bbox == null) continue;

            // Alterna cores básicas por item (sem estilos)
            int color = 0xFFFF5722; // default
            switch (i % 3) {
                case 1: color = 0xFF3F51B5; break;
                case 2: color = 0xFF388E3C; break;
            }
            boxPaint.setColor(color);
            textPaint.setColor(color);

            // bbox normalizada -> pixels
            temp.set(
                    clamp(d.bbox.left, 0f, 1f) * w,
                    clamp(d.bbox.top, 0f, 1f) * h,
                    clamp(d.bbox.right, 0f, 1f) * w,
                    clamp(d.bbox.bottom, 0f, 1f) * h
            );

            canvas.drawRect(temp, boxPaint);

            String label = (d.label == null ? "alimento" : d.label);
            float conf = Math.max(0f, Math.min(1f, d.confidence));
            String title = label + " (" + Math.round(conf * 100) + "%)";
            float tx = temp.left + 8f;
            float ty = Math.max(36f, temp.top + 36f);
            canvas.drawText(title, tx, ty, textPaint);
        }
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
