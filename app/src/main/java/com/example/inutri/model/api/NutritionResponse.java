package com.example.inutri.model.api;

import java.io.Serializable;
import java.util.List;

public class NutritionResponse implements Serializable {
    public List<NutritionItem> items;
    public TotalNutrition total_nutrition;

    public static class NutritionItem implements Serializable {
        public FoodInfo food;
        public PortionInfo portion;
        public NutritionData nutrition;
        public String source;
    }

    public static class FoodInfo implements Serializable {
        public String name;
        public String category;
        public double confidence;
    }

    public static class PortionInfo implements Serializable {
        public double estimated_grams;
        public String description;
    }

    public static class NutritionData implements Serializable {
        public double calories_kcal;
        public double protein_g;
        public double carbohydrates_g;
        public double fat_g;
        public double fiber_g;
        public double sugars_g;
        public double sodium_mg;
    }

    public static class TotalNutrition implements Serializable {
        public double calories_kcal;
        public double protein_g;
        public double carbohydrates_g;
        public double fat_g;
        public double fiber_g;
        public double sugars_g;
        public double sodium_mg;
    }
}
