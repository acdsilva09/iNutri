package com.example.inutri.data.nutrition;

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

public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.VH> {

    private final List<MealItem> data = new ArrayList<>();

    public void submit(List<MealItem> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nutrition_summary_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MealItem m = data.get(position);
        String nome = (m.getNome() == null ? "Item" : m.getNome());
        double g = m.getGramas();

        h.txtItemName.setText(nome);
        h.txtItemGrams.setText(String.format(Locale.getDefault(), "%.0f g", g));

        // Usa os métodos do próprio MealItem
        double kcal = m.kcal();
        double p    = m.proteina();
        double c    = m.carbo();
        double f    = m.gordura();

        h.txtItemKcal.setText(String.format(Locale.getDefault(), "%.0f kcal", kcal));
        h.txtItemProtein.setText(String.format(Locale.getDefault(), "P: %.1f g", p));
        h.txtItemCarbs.setText(String.format(Locale.getDefault(), "C: %.1f g", c));
        h.txtItemFat.setText(String.format(Locale.getDefault(), "G: %.1f g", f));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtItemName, txtItemGrams, txtItemKcal, txtItemProtein, txtItemCarbs, txtItemFat;
        VH(@NonNull View itemView) {
            super(itemView);
            txtItemName    = itemView.findViewById(R.id.txtItemName);
            txtItemGrams   = itemView.findViewById(R.id.txtItemGrams);
            txtItemKcal    = itemView.findViewById(R.id.txtItemKcal);
            txtItemProtein = itemView.findViewById(R.id.txtItemProtein);
            txtItemCarbs   = itemView.findViewById(R.id.txtItemCarbs);
            txtItemFat     = itemView.findViewById(R.id.txtItemFat);
        }
    }
}
