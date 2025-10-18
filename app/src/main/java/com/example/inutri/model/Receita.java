package com.example.inutri.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Coleção de ingredientes com totais (pode representar uma receita simples). */
public class Receita {
    private int id;
    private String nome;
    private final List<Ingrediente> ingredientes = new ArrayList<>();
    private int rendimentoPorcoes = 1;

    // Firestore precisa de construtor vazio
    public Receita() { }

    public Receita(int id, String nome, int rendimentoPorcoes) {
        this.id = id;
        this.nome = nome;
        this.rendimentoPorcoes = Math.max(1, rendimentoPorcoes);
    }

    public void adicionarIngrediente(Ingrediente ing) {
        if (ing == null) return;
        ingredientes.add(ing);
    }

    public List<Ingrediente> getIngredientes() {
        return Collections.unmodifiableList(ingredientes);
    }

    public double pesoTotalGramas() {
        double soma = 0;
        for (Ingrediente i : ingredientes) soma += i.emGramas();
        return soma;
    }

    public double kcalTotal() { return soma(ingredientes, "kcal"); }
    public double proteinaTotal() { return soma(ingredientes, "prot"); }
    public double carboTotal() { return soma(ingredientes, "carb"); }
    public double gorduraTotal() { return soma(ingredientes, "gord"); }
    public double fibraTotal() { return soma(ingredientes, "fibra"); }
    public double sodioTotal() { return soma(ingredientes, "sodio"); }

    // por porção
    public double kcalPorPorcao() { return kcalTotal() / rendimentoPorcoes; }
    public double proteinaPorPorcao() { return proteinaTotal() / rendimentoPorcoes; }
    public double carboPorPorcao() { return carboTotal() / rendimentoPorcoes; }
    public double gorduraPorPorcao() { return gorduraTotal() / rendimentoPorcoes; }

    private double soma(List<Ingrediente> list, String tipo) {
        double t = 0;
        for (Ingrediente i : list) {
            switch (tipo) {
                case "kcal": t += i.kcal(); break;
                case "prot": t += i.proteina(); break;
                case "carb": t += i.carboidrato(); break;
                case "gord": t += i.gordura(); break;
                case "fibra": t += i.fibra(); break;
                case "sodio": t += i.sodio(); break;
            }
        }
        return t;
    }

    // Getters/Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public int getRendimentoPorcoes() { return rendimentoPorcoes; }
    public void setRendimentoPorcoes(int rendimentoPorcoes) {
        this.rendimentoPorcoes = Math.max(1, rendimentoPorcoes);
    }
}
