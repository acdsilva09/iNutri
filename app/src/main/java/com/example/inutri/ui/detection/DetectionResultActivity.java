package com.example.inutri.ui.detection;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.inutri.data.nutrition.NutritionSummaryActivity;
import com.example.inutri.databinding.ActivityDetectionResultBinding;
import com.example.inutri.model.DetectedFood;
import com.example.inutri.model.MealItem;


import java.util.ArrayList;
import java.util.List;

public class DetectionResultActivity extends AppCompatActivity {


    private ActivityDetectionResultBinding binding;
    private final ArrayList<MealItem> items = new ArrayList<>();
    private MealItemAdapter adapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetectionResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ✅ RecyclerView: garanta que no XML o id é @+id/recyclerDetected
        adapter = new MealItemAdapter();
        binding.recyclerFoods.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerFoods.setAdapter(adapter);

        // ✅ Recebe itens (se vieram) como PARCELABLE (seu MealItem é Parcelable)
        ArrayList<MealItem> incoming = getIntent().getParcelableArrayListExtra("items");
        if (incoming != null) items.addAll(incoming);

        // ✅ Preenche a lista no adapter (ajuste o nome do método conforme o seu adapter)
        adapter.submitMealItems(items);

        // ✅ Botão continuar: id deve ser @+id/btnContinueDetection no XML
        binding.btnContinueDetection.setOnClickListener(v -> {
            if (items.isEmpty()) {
                Toast.makeText(this, "Selecione pelo menos 1 item", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent it = new Intent(this, NutritionSummaryActivity.class);
            it.putParcelableArrayListExtra("items", items);
            startActivity(it);
        });

        // (Opcional) se esta Activity também receber detecções para transformar em MealItem:
        // ArrayList<DetectedFood> dfs = getIntent().getParcelableArrayListExtra("detections");
        // if (dfs != null) for (DetectedFood df : dfs) ensureMealItem(df);
    }

    /** Converte um DetectedFood em MealItem usando seu modelo (nome/gramas) */
    private void ensureMealItem(DetectedFood df) {
        if (df == null) return;
        int idx = findIndex(items, df.getName()); // ⚠️ use getLabel() (camel case correto)
        if (idx >= 0) {
            // Exemplo simples: soma 50g ao existente
            MealItem mi = items.get(idx);
            mi.setGramas(mi.getGramas() + 50);
        } else {
            // Cria com 100g padrão (ajuste como quiser)
            items.add(new MealItem(/*foodId*/null, /*nome*/df.getName(),
                    /*kcal100g*/0, /*p100g*/0, /*c100g*/0, /*g100g*/0,
                    /*gramas*/100));
        }
        adapter.submitMealItems(items); // atualiza visual
    }

    private int findIndex(List<MealItem> list, String label) {
        for (int i = 0; i < list.size(); i++) {
            if (label.equalsIgnoreCase(list.get(i).getNome())) return i; // ✅ getNome()
        }
        return -1;
    }
}
