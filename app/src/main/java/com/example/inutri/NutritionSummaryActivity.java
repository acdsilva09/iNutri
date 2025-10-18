package com.example.inutri;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inutri.R;
import com.example.inutri.model.MealItem;
import com.example.inutri.nutrition.NutritionFacts;
import com.example.inutri.nutrition.NutritionRepo;

import java.util.ArrayList;

public class NutritionSummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_nutrition_summary);

        ArrayList<MealItem> items = getIntent().getParcelableArrayListExtra("meal_items");

        // Exemplo simples: busca por 100g e escala
        NutritionRepo repo = NutritionRepo.getInstance();
        float kcalTotal = 0, carbTotal = 0, protTotal = 0, fatTotal = 0;

        for (MealItem mi : items) {
            NutritionFacts per100g = repo.getPer100g(mi.getNome()); // mapeamento/BD/API
            float factor = (float) (mi.getGramas() / 100f);
            kcalTotal += per100g.kcal * factor;
            carbTotal += per100g.carbs * factor;
            protTotal += per100g.protein * factor;
            fatTotal  += per100g.fat * factor;
        }

        // Atualize sua UI aqui…
    }
}
