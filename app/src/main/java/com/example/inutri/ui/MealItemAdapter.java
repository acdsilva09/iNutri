package com.example.inutri.ui.capture;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inutri.R;
import com.example.inutri.model.MealItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MealItemAdapter extends RecyclerView.Adapter<MealItemAdapter.VH> {

    private final List<MealItem> items = new ArrayList<>();

    public void submitList(List<MealItem> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MealItem m = items.get(position);

        String nome = (m.nome == null ? "Item" : m.nome);
        h.txtItemName.setText(nome);

        h.txtItemGrams.setText(String.format(Locale.getDefault(), "%.0f g", m.gramas));

        // kcal e macros calculadas a partir dos valores por 100 g
        double kcal = (m.kcal100g * m.gramas) / 100.0;
        double p = (m.p100g * m.gramas) / 100.0;
        double c = (m.c100g * m.gramas) / 100.0;
        double g = (m.g100g * m.gramas) / 100.0;

        h.txtItemKcal.setText(String.format(Locale.getDefault(), "%.0f kcal", kcal));
        h.txtItemProtein.setText(String.format(Locale.getDefault(), "P: %.1f g", p));
        h.txtItemCarbs.setText(String.format(Locale.getDefault(), "C: %.1f g", c));
        h.txtItemFat.setText(String.format(Locale.getDefault(), "G: %.1f g", g));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtItemName, txtItemGrams, txtItemKcal, txtItemProtein, txtItemCarbs, txtItemFat;
        VH(@NonNull View itemView) {
            super(itemView);
            txtItemName = itemView.findViewById(R.id.txtItemName);
            txtItemGrams = itemView.findViewById(R.id.txtItemGrams);
            txtItemKcal = itemView.findViewById(R.id.txtItemKcal);
            txtItemProtein = itemView.findViewById(R.id.txtItemProtein);
            txtItemCarbs = itemView.findViewById(R.id.txtItemCarbs);
            txtItemFat = itemView.findViewById(R.id.txtItemFat);
        }
    }
}
