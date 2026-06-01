package com.example.inutri.ui.detection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inutri.R;
import com.example.inutri.model.api.NutritionResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NutritionResultAdapter extends RecyclerView.Adapter<NutritionResultAdapter.VH> {

    private final List<NutritionResponse.NutritionItem> items = new ArrayList<>();

    public void submitList(List<NutritionResponse.NutritionItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nutrition_result, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        NutritionResponse.NutritionItem item = items.get(position);
        
        holder.tvFoodName.setText(item.food.name);
        holder.tvConfidence.setText(String.format(Locale.getDefault(), "%.0f%%", item.food.confidence * 100));
        
        String portionTxt = String.format(Locale.getDefault(), "Porção: %.1fg - %s", 
                item.portion.estimated_grams, item.portion.description);
        holder.tvPortion.setText(portionTxt);
        
        holder.tvKcal.setText(String.format(Locale.getDefault(), "%.0f", item.nutrition.calories_kcal));
        holder.tvCarbs.setText(String.format(Locale.getDefault(), "%.1fg", item.nutrition.carbohydrates_g));
        holder.tvProtein.setText(String.format(Locale.getDefault(), "%.1fg", item.nutrition.protein_g));
        holder.tvFat.setText(String.format(Locale.getDefault(), "%.1fg", item.nutrition.fat_g));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvConfidence, tvPortion, tvKcal, tvCarbs, tvProtein, tvFat;

        VH(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvConfidence = itemView.findViewById(R.id.tvConfidence);
            tvPortion = itemView.findViewById(R.id.tvPortion);
            tvKcal = itemView.findViewById(R.id.tvKcal);
            tvCarbs = itemView.findViewById(R.id.tvCarbs);
            tvProtein = itemView.findViewById(R.id.tvProtein);
            tvFat = itemView.findViewById(R.id.tvFat);
        }
    }
}
