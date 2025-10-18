package com.example.inutri.model;

/** Item de refeição com valores por 100 g e porção (gramas). */
public class MealItem {
    public String foodId;  // id da API nutricional (opcional)
    public String nome;

    // Porção escolhida/estimada
    public double gramas;

    // Valores por 100 g
    public double kcal100g;
    public double p100g;   // proteína
    public double c100g;   // carboidrato
    public double g100g;   // gordura

    public MealItem() { }

    public MealItem(String foodId, String nome,
                    double kcal100g, double p100g, double c100g, double g100g,
                    double gramas) {
        this.foodId = foodId;
        this.nome = nome;
        this.kcal100g = Math.max(0, kcal100g);
        this.p100g = Math.max(0, p100g);
        this.c100g = Math.max(0, c100g);
        this.g100g = Math.max(0, g100g);
        this.gramas = Math.max(0, gramas);
    }

    // Cálculos da porção
    public double kcal() { return (kcal100g * gramas) / 100.0; }
    public double proteina() { return (p100g * gramas) / 100.0; }
    public double carbo() { return (c100g * gramas) / 100.0; }
    public double gordura() { return (g100g * gramas) / 100.0; }
}
