package com.example.inutri.ui.detection;

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

    public interface OnGramsChanged { void onChanged(int position, String label, float grams); }

    private final OnGramsChanged onGramsChanged;

    // estado dos campos digitados
    private final java.util.Map<String, String> gramsByLabel = new java.util.HashMap<>();

    public DetectedFoodAdapter(OnGramsChanged onGramsChanged) {
        super(DIFF);
        this.onGramsChanged = onGramsChanged;
        setHasStableIds(true); // ajuda o RecyclerView a manter foco
    }

    // opcional: permitir a Activity compartilhar o mesmo Map
    public void setExternalGramsMap(java.util.Map<String, String> external) {
        gramsByLabel.clear();
        if (external != null) gramsByLabel.putAll(external);
        notifyDataSetChanged(); // chame isso raramente (não por tecla)
    }

    @Override public long getItemId(int position) {
        return getItem(position).getName().hashCode();
    }

    static final DiffUtil.ItemCallback<DetectedFood> DIFF = new DiffUtil.ItemCallback<DetectedFood>() {
        @Override public boolean areItemsTheSame(DetectedFood o, DetectedFood n) {
            return o.getName().equalsIgnoreCase(n.getName());
        }
        @Override public boolean areContentsTheSame(DetectedFood o, DetectedFood n) {
            return o.getName().equals(n.getName()) && o.getConfidence() == n.getConfidence();
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

        if (h.watcher != null) h.etGrams.removeTextChangedListener(h.watcher);

        // restaura o texto digitado (se houver) SEM causar flicker
        String desired = gramsByLabel.getOrDefault(item.getName(), "");
        if (!desired.equals(h.etGrams.getText().toString())) {
            h.etGrams.setText(desired);
            h.etGrams.setSelection(h.etGrams.getText().length());
        }

        h.watcher = new SimpleTextWatcher(s -> {
            String txt = s == null ? "" : s.toString();
            gramsByLabel.put(item.getName(), txt);

            if (onGramsChanged != null) {
                float g = 0f;
                try { g = Float.parseFloat(txt); } catch (Exception ignored) {}
                onGramsChanged.onChanged(h.getBindingAdapterPosition(), item.getName(), g);
            }
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

    static class SimpleTextWatcher implements TextWatcher {
        private final java.util.function.Consumer<CharSequence> onChange;
        SimpleTextWatcher(java.util.function.Consumer<CharSequence> c) { onChange = c; }
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) { onChange.accept(s); }
        @Override public void afterTextChanged(Editable s) {}
    }
}

