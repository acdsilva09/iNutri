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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetectionResultActivity extends AppCompatActivity {

    private ActivityDetectionResultBinding binding;

    // armazena o que o usuário digitou: label -> "123.0"
    private final Map<String, String> gramsByLabel = new HashMap<>();

    // mantemos a lista detectada para converter depois
    private final ArrayList<DetectedFood> detections = new ArrayList<>();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetectionResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1) Adapter com callback só para gravar o que foi digitado
        DetectedFoodAdapter adapter = new DetectedFoodAdapter((pos, label, grams) -> {
            // guarda como texto para preservar exatamente o que o usuário digitou
            gramsByLabel.put(label, (grams <= 0f) ? "" : String.valueOf(grams));
        });
        binding.recyclerFoods.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerFoods.setAdapter(adapter);

        // 2) Recupera a lista detectada vinda da CaptureMealActivity
        ArrayList<DetectedFood> incoming = getIntent().getParcelableArrayListExtra("detections");
        if (incoming != null) detections.addAll(incoming);

        // 3) Mostra a lista para o usuário preencher as gramas
        adapter.submitList(detections);

        // 4) Continuar -> monta os MealItem com as gramas digitadas

        // DetectionResultActivity.java – no clique do Continuar
        binding.btnContinueDetection.setOnClickListener(v -> {
            ArrayList<MealItem> items = buildMealItemsFromInput();
            if (items.isEmpty()) {
                Toast.makeText(this, "Selecione pelo menos 1 item", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent it = new Intent(this, NutritionSummaryActivity.class);
            it.putParcelableArrayListExtra("items", new ArrayList<>(items)); // ✅
            startActivity(it);
        });

    }

    private ArrayList<MealItem> buildMealItemsFromInput() {
        ArrayList<MealItem> out = new ArrayList<>();
        for (DetectedFood df : detections) {
            String label = df.getName();
            String gTxt = gramsByLabel.get(label);
            if (gTxt == null || gTxt.trim().isEmpty()) continue;

            float g;
            try { g = Float.parseFloat(gTxt); } catch (Exception e) { g = 0f; }
            if (g <= 0f) continue;

            // Cria seu MealItem usando apenas nome e gramas (ou complete com macros se tiver)
            out.add(new MealItem(/*foodId*/ null, /*nome*/ label,
                    /*kcal100g*/ 0, /*p100g*/ 0, /*c100g*/ 0, /*g100g*/ 0,
                    /*gramas*/ g));
        }
        return out;
    }
}
