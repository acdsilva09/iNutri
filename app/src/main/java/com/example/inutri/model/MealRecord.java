package com.example.inutri.model;

import java.util.ArrayList;
import java.util.List;

/** Documento salvo no Firestore para representar uma refeição completa. */
public class MealRecord {
    public String uid;            // FirebaseAuth user id
    public String photoUrl;       // URL da foto no Firebase Storage
    public long timestamp;        // System.currentTimeMillis()
    public List<MealItem> itens = new ArrayList<>();

    public MealRecord() { }

    public double kcalTotal() {
        double t = 0;
        for (MealItem i : itens) t += i.kcal();
        return t;
    }
    public double protTotal() {
        double t = 0;
        for (MealItem i : itens) t += i.proteina();
        return t;
    }
    public double carbTotal() {
        double t = 0;
        for (MealItem i : itens) t += i.carbo();
        return t;
    }
    public double gordTotal() {
        double t = 0;
        for (MealItem i : itens) t += i.gordura();
        return t;
    }
}
