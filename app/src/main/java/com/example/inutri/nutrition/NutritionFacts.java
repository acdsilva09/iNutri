package com.example.inutri.nutrition;

public class NutritionFacts {
    public final float kcal;
    public final float carbs;
    public final float protein;
    public final float fat;

    public NutritionFacts(float kcal, float carbs, float protein, float fat) {
        this.kcal = kcal;
        this.carbs = carbs;
        this.protein = protein;
        this.fat = fat;
    }

    /** Fábrica estática para facilitar chamadas como NutritionFacts.of(…) */
    public static NutritionFacts of(float kcal, float carbs, float protein, float fat) {
        return new NutritionFacts(kcal, carbs, protein, fat);
    }
}
