package com.example.inutri.model;

import java.util.Objects;

/** Valores nutricionais por 100 g (ou 100 ml) para padronização. */
public class Alimento {
    private int id;
    private String nome;
    private String categoria; // ex.: Grãos, Laticínios, Carnes
    private double kcal100g;
    private double proteina100g;
    private double carbo100g;
    private double gordura100g;
    private double fibra100g;
    private double sodio100g;

    public Alimento() { }

    public Alimento(int id, String nome, String categoria,
                    double kcal100g, double proteina100g, double carbo100g,
                    double gordura100g, double fibra100g, double sodio100g) {
        this.id = id;
        this.nome = Objects.requireNonNull(nome);
        this.categoria = categoria;
        this.kcal100g = Math.max(0, kcal100g);
        this.proteina100g = Math.max(0, proteina100g);
        this.carbo100g = Math.max(0, carbo100g);
        this.gordura100g = Math.max(0, gordura100g);
        this.fibra100g = Math.max(0, fibra100g);
        this.sodio100g = Math.max(0, sodio100g);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public double getKcal100g() { return kcal100g; }
    public void setKcal100g(double v) { this.kcal100g = Math.max(0, v); }
    public double getProteina100g() { return proteina100g; }
    public void setProteina100g(double v) { this.proteina100g = Math.max(0, v); }
    public double getCarbo100g() { return carbo100g; }
    public void setCarbo100g(double v) { this.carbo100g = Math.max(0, v); }
    public double getGordura100g() { return gordura100g; }
    public void setGordura100g(double v) { this.gordura100g = Math.max(0, v); }
    public double getFibra100g() { return fibra100g; }
    public void setFibra100g(double v) { this.fibra100g = Math.max(0, v); }
    public double getSodio100g() { return sodio100g; }
    public void setSodio100g(double v) { this.sodio100g = Math.max(0, v); }
}
