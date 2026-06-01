package com.example.inutri.ui.detection;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inutri.R;
import com.example.inutri.model.api.NutritionResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NutritionEditAdapter extends RecyclerView.Adapter<NutritionEditAdapter.VH> {

    private final List<NutritionResponse.NutritionItem> items = new ArrayList<>();
    // Armazena os valores base (por 1g) para evitar erros de arredondamento em edições sucessivas
    private final Map<Integer, NutritionResponse.NutritionData> baseValues = new HashMap<>();
    private final Runnable onDataChanged;

    public NutritionEditAdapter(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    public void submitList(List<NutritionResponse.NutritionItem> newItems) {
        items.clear();
        baseValues.clear();
        if (newItems != null) {
            items.addAll(newItems);
            for (int i = 0; i < items.size(); i++) {
                NutritionResponse.NutritionItem item = items.get(i);
                double grams = item.portion.estimated_grams;
                if (grams <= 0) grams = 1.0;
                
                // Salva o valor base (por grama)
                NutritionResponse.NutritionData base = new NutritionResponse.NutritionData();
                base.calories_kcal = item.nutrition.calories_kcal / grams;
                base.protein_g = item.nutrition.protein_g / grams;
                base.carbohydrates_g = item.nutrition.carbohydrates_g / grams;
                base.fat_g = item.nutrition.fat_g / grams;
                baseValues.put(i, base);
            }
        }
        notifyDataSetChanged();
    }

    public List<NutritionResponse.NutritionItem> getItems() {
        return items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nutrition_edit, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        NutritionResponse.NutritionItem item = items.get(position);
        NutritionResponse.NutritionData base = baseValues.get(position);
        
        holder.tvFoodName.setText(item.food.name);
        
        holder.etGrams.removeTextChangedListener(holder.watcher);
        // Exibe o valor atual
        if (item.portion.estimated_grams > 0) {
            holder.etGrams.setText(String.format(Locale.US, "%.1f", item.portion.estimated_grams));
        } else {
            holder.etGrams.setText("");
        }
        
        updateMacros(holder, item);

        holder.watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    String val = s.toString().replace(",", ".");
                    double newGrams = val.isEmpty() ? 0 : Double.parseDouble(val);
                    
                    // Atualiza o item com base nos valores por grama salvos
                    if (base != null) {
                        item.portion.estimated_grams = newGrams;
                        item.nutrition.calories_kcal = base.calories_kcal * newGrams;
                        item.nutrition.protein_g = base.protein_g * newGrams;
                        item.nutrition.carbohydrates_g = base.carbohydrates_g * newGrams;
                        item.nutrition.fat_g = base.fat_g * newGrams;
                    }
                    
                    updateMacros(holder, item);
                    if (onDataChanged != null) onDataChanged.run();
                } catch (Exception ignored) {}
            }
        };
        holder.etGrams.addTextChangedListener(holder.watcher);
    }

    private void updateMacros(VH holder, NutritionResponse.NutritionItem item) {
        String macros = String.format(Locale.getDefault(),
                "Kcal: %.1f | Proteínas: %.1fg\nCarboidratos: %.1fg | Gorduras Totais: %.1fg",
                item.nutrition.calories_kcal,
                item.nutrition.protein_g,
                item.nutrition.carbohydrates_g,
                item.nutrition.fat_g);
        holder.tvMacros.setText(macros);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvMacros;
        EditText etGrams;
        TextWatcher watcher;

        VH(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvMacros = itemView.findViewById(R.id.tvMacros);
            etGrams = itemView.findViewById(R.id.etGrams);
        }
    }
}
