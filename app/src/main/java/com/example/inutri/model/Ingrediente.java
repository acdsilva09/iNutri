package com.example.inutri.model;

import java.util.Objects;

/** Ingrediente de uma receita/refeição. */
public class Ingrediente {
    private int idIngredienteReceita;
    private Alimento alimento;
    private Unidade unidade;       // GRAMA, MILILITRO, UNIDADE
    private double valor;          // quantidade (ex.: 150 g, 200 ml, 1 unidade)
    private double pesoPadraoGramas; // usado quando unidade = UNIDADE (ex.: 1 ovo = 50 g)

    public Ingrediente() { }

    public Ingrediente(int idIngredienteReceita, Alimento alimento, Unidade unidade,
                       double valor, double pesoPadraoGramas) {
        this.idIngredienteReceita = idIngredienteReceita;
        this.alimento = Objects.requireNonNull(alimento);
        this.unidade = Objects.requireNonNull(unidade);
        this.valor = Math.max(0, valor);
        this.pesoPadraoGramas = Math.max(0, pesoPadraoGramas);
    }

    /** Converte a quantidade para gramas (aproximação p/ ml = g). */
    public double emGramas() {
        if (unidade == null) return 0;
        switch (unidade) {
            case GRAMA: return valor;
            case MILILITRO: return valor; // 1 ml ≈ 1 g (água). Ideal: densidade por alimento.
            case UNIDADE: return valor * pesoPadraoGramas;
            default: return 0;
        }
    }

    private double fator() { return emGramas() / 100.0; }

    // Cálculo dos macros do ingrediente
    public double kcal() { return safe(alimento.getKcal100g()) * fator(); }
    public double proteina() { return safe(alimento.getProteina100g()) * fator(); }
    public double carboidrato() { return safe(alimento.getCarbo100g()) * fator(); }
    public double gordura() { return safe(alimento.getGordura100g()) * fator(); }
    public double fibra() { return safe(alimento.getFibra100g()) * fator(); }
    public double sodio() { return safe(alimento.getSodio100g()) * fator(); }

    private double safe(double v) { return Double.isNaN(v) || v < 0 ? 0 : v; }

    // Getters/Setters
    public int getIdIngredienteReceita() { return idIngredienteReceita; }
    public void setIdIngredienteReceita(int id) { this.idIngredienteReceita = id; }
    public Alimento getAlimento() { return alimento; }
    public void setAlimento(Alimento alimento) { this.alimento = alimento; }
    public Unidade getUnidade() { return unidade; }
    public void setUnidade(Unidade unidade) { this.unidade = unidade; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = Math.max(0, valor); }
    public double getPesoPadraoGramas() { return pesoPadraoGramas; }
    public void setPesoPadraoGramas(double v) { this.pesoPadraoGramas = Math.max(0, v); }
}
