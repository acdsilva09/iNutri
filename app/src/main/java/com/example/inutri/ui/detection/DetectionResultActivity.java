package com.example.inutri.ui.detection;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inutri.data.nutrition.NutritionSummaryActivity;
import com.example.inutri.databinding.ActivityDetectionResultBinding;
import com.example.inutri.model.DetectedFood;
import com.example.inutri.model.MealItem;
import com.example.inutri.model.api.NutritionResponse;

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

        binding.recyclerFoods.setLayoutManager(new LinearLayoutManager(this));

        // Tenta recuperar a foto
        String photoUri = getIntent().getStringExtra("photo_uri");
        if (photoUri != null) {
            binding.imgPhoto.setImageURI(android.net.Uri.parse(photoUri));
        }

        // Tenta recuperar a resposta da API
        NutritionResponse response = (NutritionResponse) getIntent().getSerializableExtra("nutrition_response");

        if (response != null) {
            setupNutritionResult(response);
        } else {
            setupManualInput();
        }

        // 4) Continuar -> monta os MealItem com as gramas digitadas
        binding.btnContinueDetection.setOnClickListener(v -> {
            if (response != null) {
                // Recupera a lista editada do Adapter
                NutritionEditAdapter editAdapter = (NutritionEditAdapter) binding.recyclerFoods.getAdapter();
                if (editAdapter != null) {
                    saveMealToFirestore(editAdapter.getItems());
                }
            } else {
                ArrayList<MealItem> items = buildMealItemsFromInput();
                if (items.isEmpty()) {
                    Toast.makeText(this, "Selecione pelo menos 1 item", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveMealToFirestoreManual(items);
            }
        });
    }

    private void saveMealToFirestore(List<NutritionResponse.NutritionItem> items) {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> meal = new HashMap<>();
        meal.put("uid", user.getUid());
        meal.put("timestamp", System.currentTimeMillis());
        
        List<Map<String, Object>> mealItems = new ArrayList<>();
        double totalKcal = 0;
        
        for (NutritionResponse.NutritionItem item : items) {
            Map<String, Object> mealItem = new HashMap<>();
            mealItem.put("name", item.food.name);
            mealItem.put("grams", item.portion.estimated_grams);
            mealItem.put("kcal", item.nutrition.calories_kcal);
            mealItem.put("protein", item.nutrition.protein_g);
            mealItem.put("carbs", item.nutrition.carbohydrates_g);
            mealItem.put("fat", item.nutrition.fat_g);
            mealItems.add(mealItem);
            totalKcal += item.nutrition.calories_kcal;
        }
        
        meal.put("items", mealItems);
        meal.put("totalKcal", totalKcal);

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("meals")
                .add(meal)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Refeição salva com sucesso!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, com.example.inutri.ui.MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void saveMealToFirestoreManual(ArrayList<MealItem> items) {
        // Similar ao acima, mas convertendo MealItem para Map
    }

    private void setupNutritionResult(NutritionResponse response) {
        NutritionEditAdapter adapter = new NutritionEditAdapter(this::updateTotalNutrients);
        binding.recyclerFoods.setAdapter(adapter);
        adapter.submitList(response.items);
    }

    private void updateTotalNutrients() {
        // Implementar se quiser mostrar o total em tempo real na tela
    }

    private void setupManualInput() {
        DetectedFoodAdapter adapter = new DetectedFoodAdapter((pos, label, grams) -> {
            gramsByLabel.put(label, (grams <= 0f) ? "" : String.valueOf(grams));
        });
        binding.recyclerFoods.setAdapter(adapter);

        ArrayList<DetectedFood> incoming = getIntent().getParcelableArrayListExtra("detections");
        if (incoming != null) {
            detections.addAll(incoming);
            adapter.submitList(detections);
        }
    }

    private ArrayList<MealItem> buildMealItemsFromResponse(NutritionResponse response) {
        ArrayList<MealItem> out = new ArrayList<>();
        for (NutritionResponse.NutritionItem item : response.items) {
            out.add(new MealItem(null, item.food.name,
                    item.nutrition.calories_kcal / (item.portion.estimated_grams / 100.0),
                    item.nutrition.protein_g / (item.portion.estimated_grams / 100.0),
                    item.nutrition.carbohydrates_g / (item.portion.estimated_grams / 100.0),
                    item.nutrition.fat_g / (item.portion.estimated_grams / 100.0),
                    (float) item.portion.estimated_grams));
        }
        return out;
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
