package com.example.inutri.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MealItem implements Parcelable {

    public String foodId;
    public String nome;
    public double gramas;
    public double kcal100g;
    public double p100g;
    public double c100g;
    public double g100g;

    public MealItem(String label, float grams) { // mantém para compatibilidade
        this.nome = label;
        this.gramas = grams;
    }

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

    public double kcal()     { return (kcal100g * gramas) / 100.0; }
    public double proteina() { return (p100g * gramas) / 100.0; }
    public double carbo()    { return (c100g * gramas) / 100.0; }
    public double gordura()  { return (g100g * gramas) / 100.0; }

    protected MealItem(Parcel in) {
        foodId = in.readString();
        nome = in.readString();
        gramas = in.readDouble();
        kcal100g = in.readDouble();
        p100g = in.readDouble();
        c100g = in.readDouble();
        g100g = in.readDouble();
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(foodId);
        dest.writeString(nome);
        dest.writeDouble(gramas);
        dest.writeDouble(kcal100g);
        dest.writeDouble(p100g);
        dest.writeDouble(c100g);
        dest.writeDouble(g100g);
    }

    @Override public int describeContents() { return 0; }

    public static final Creator<MealItem> CREATOR = new Creator<MealItem>() {
        @Override public MealItem createFromParcel(Parcel in) { return new MealItem(in); }
        @Override public MealItem[] newArray(int size) { return new MealItem[size]; }
    };

    public String getFoodId() { return foodId; }
    public void setFoodId(String foodId) { this.foodId = foodId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public double getGramas() { return gramas; }
    public void setGramas(double gramas) { this.gramas = gramas; }
    public double getKcal100g() { return kcal100g; }
    public void setKcal100g(double kcal100g) { this.kcal100g = kcal100g; }
    public double getP100g() { return p100g; }
    public void setP100g(double p100g) { this.p100g = p100g; }
    public double getC100g() { return c100g; }
    public void setC100g(double c100g) { this.c100g = c100g; }
    public double getG100g() { return g100g; }
    public void setG100g(double g100g) { this.g100g = g100g; }
}
