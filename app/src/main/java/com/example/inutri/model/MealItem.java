package com.example.inutri.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/** Item de refeição com valores por 100 g e porção (gramas). */
public class MealItem  implements Parcelable {
    public String foodId;  // id da API nutricional (opcional)
    public String nome;

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getGramas() {
        return gramas;
    }

    public void setGramas(double gramas) {
        this.gramas = gramas;
    }

    public double getP100g() {
        return p100g;
    }

    public void setP100g(double p100g) {
        this.p100g = p100g;
    }

    public double getC100g() {
        return c100g;
    }

    public void setC100g(double c100g) {
        this.c100g = c100g;
    }

    public double getG100g() {
        return g100g;
    }

    public void setG100g(double g100g) {
        this.g100g = g100g;
    }

    // Porção escolhida/estimada
    public double gramas;

    public double getKcal100g() {
        return kcal100g;
    }

    public void setKcal100g(double kcal100g) {
        this.kcal100g = kcal100g;
    }

    // Valores por 100 g
    public double kcal100g;
    public double p100g;   // proteína
    public double c100g;   // carboidrato
    public double g100g;   // gordura

    public MealItem(String label, float grams) { }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {

    }
}
