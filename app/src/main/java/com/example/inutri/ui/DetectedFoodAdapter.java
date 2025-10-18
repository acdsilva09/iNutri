package com.example.inutri.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inutri.R;
import com.example.inutri.model.DetectedFood;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Mostra as detecções e permite ajustar a quantidade em gramas.
 * Usa o SeekBar.progress como "gramas".
 */
public class DetectedFoodAdapter extends RecyclerView.Adapter<DetectedFoodAdapter.VH> {

    public interface OnGramsChangedListener {
        void onGramsChanged(int position, DetectedFood item, int grams);
    }

    public interface OnItemClickListener {
        void onClick(int position, DetectedFood item);
    }

    private final List<DetectedFood> items = new ArrayList<>();
    // gramagem por posição (simples para MVP)
    private final List<Integer> grams = new ArrayList<>();
    private OnGramsChangedListener gramsListener;
    private OnItemClickListener clickListener;

    public void setOnGramsChangedListener(OnGramsChangedListener l) {
        this.gramsListener = l;
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.clickListener = l;
    }

    public void submitList(List<DetectedFood> list, Integer defaultGrams) {
        items.clear();
        grams.clear();
        if (list != null) {
            items.addAll(list);
            for (int i = 0; i < items.size(); i++) {
                grams.add(defaultGrams == null ? 150 : Math.max(0, defaultGrams));
            }
        }
        notifyDataSetChanged();
    }

    public int getGramsAt(int position) {
        if (position < 0 || position >= grams.size()) return 0;
        return grams.get(position);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_detected_food, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DetectedFood d = items.get(position);
        int g = grams.get(position);

        h.txtFoodName.setText(d.label == null ? "Alimento" : d.label);
        h.txtConfidence.setText(String.format(Locale.getDefault(), "%d%%",
                Math.round(Math.max(0f, Math.min(1f, d.confidence)) * 100)));
        h.txtGramsValue.setText(String.format(Locale.getDefault(), "%d g", g));

        // kcal estimada é apenas ilustrativa aqui (depende da API). Deixe vazio ou ≈0.
        h.txtKcalEstimate.setText("≈ 0 kcal");

        h.seekGrams.setOnSeekBarChangeListener(null);
        h.seekGrams.setMax(1000);
        h.seekGrams.setProgress(g);

        h.seekGrams.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                grams.set(h.getAdapterPosition(), value);
                h.txtGramsValue.setText(String.format(Locale.getDefault(), "%d g", value));
                if (gramsListener != null) {
                    gramsListener.onGramsChanged(h.getAdapterPosition(), d, value);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(h.getAdapterPosition(), d);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtFoodName, txtConfidence, txtGramsValue, txtKcalEstimate, txtHintPortion;
        SeekBar seekGrams;
        VH(@NonNull View itemView) {
            super(itemView);
            txtFoodName = itemView.findViewById(R.id.txtFoodName);
            txtConfidence = itemView.findViewById(R.id.txtConfidence);
            txtGramsValue = itemView.findViewById(R.id.txtGramsValue);
            txtKcalEstimate = itemView.findViewById(R.id.txtKcalEstimate);
            txtHintPortion = itemView.findViewById(R.id.txtHintPortion);
            seekGrams = itemView.findViewById(R.id.seekGrams);
        }
    }
}
