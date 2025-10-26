package com.example.inutri.model;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class DetectedFood implements Parcelable {
    private final String name;
    private final float confidence;     // 0..1
    @Nullable private final RectF box;  // pode ser null
    private final float gramsEstimate;  // estimativa (pode ser 0f)

    // Construtor “atalho” que você tinha
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

    /* -------------------- Parcelable -------------------- */

    protected DetectedFood(Parcel in) {
        name = in.readString();
        confidence = in.readFloat();
        // RectF é Parcelable; pode vir null:
        box = in.readParcelable(RectF.class.getClassLoader());
        gramsEstimate = in.readFloat();
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeFloat(confidence);
        dest.writeParcelable(box, flags);
        dest.writeFloat(gramsEstimate);
    }

    @Override public int describeContents() { return 0; }

    public static final Creator<DetectedFood> CREATOR = new Creator<DetectedFood>() {
        @Override public DetectedFood createFromParcel(Parcel in) { return new DetectedFood(in); }
        @Override public DetectedFood[] newArray(int size) { return new DetectedFood[size]; }
    };
}
