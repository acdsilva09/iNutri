package com.example.inutri.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inutri.R;
import com.example.inutri.model.DetectedFood;

import java.util.function.Consumer;

public class DetectedFoodAdapter extends ListAdapter<DetectedFood, DetectedFoodAdapter.VH> {

    public interface OnGramsChanged {
        void onChanged(int position, String label, float grams);
    }

    private final OnGramsChanged onGramsChanged;

    public DetectedFoodAdapter(OnGramsChanged onGramsChanged) {
        super(DIFF);
        this.onGramsChanged = onGramsChanged;
    }

    static final DiffUtil.ItemCallback<DetectedFood> DIFF =
            new DiffUtil.ItemCallback<DetectedFood>() {
                @Override public boolean areItemsTheSame(DetectedFood o, DetectedFood n) {
                    return o.getName().equalsIgnoreCase(n.getName());
                }
                @Override public boolean areContentsTheSame(DetectedFood o, DetectedFood n) {
                    return o.getName().equals(n.getName())
                            && o.getConfidence() == n.getConfidence();
                }
            };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_detected_food_input, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DetectedFood item = getItem(position);
        h.tvLabel.setText(item.getName());

        // Limpa listeners anteriores para evitar loops
        if (h.watcher != null) h.etGrams.removeTextChangedListener(h.watcher);

        h.etGrams.setText(""); // default
        h.watcher = new SimpleTextWatcher(s -> {
            float g = 0f;
            try { g = Float.parseFloat(s.toString()); } catch (Exception ignore) {}
            if (onGramsChanged != null) onGramsChanged.onChanged(h.getBindingAdapterPosition(), item.getName(), g);
        });
        h.etGrams.addTextChangedListener(h.watcher);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvLabel;
        EditText etGrams;
        TextWatcher watcher;
        VH(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            etGrams = itemView.findViewById(R.id.etGrams);
        }
    }

    // util
    static class SimpleTextWatcher implements TextWatcher {
        private final Consumer<CharSequence> onChange;
        SimpleTextWatcher(Consumer<CharSequence> c) { onChange = c; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { onChange.accept(s); }
        @Override public void afterTextChanged(Editable s) {}



    }
}
