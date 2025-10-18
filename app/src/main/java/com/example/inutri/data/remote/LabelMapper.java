package com.example.inutri.data.remote;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Mapa local PT-BR -> informações nutricionais por 100 g.
 * Útil para o MVP (funciona offline) e como fallback caso a API externa falhe.
 */
public class LabelMapper {

    /** Informações mínimas de um alimento por 100 g. */
    public static class FoodInfo {
        public final String foodId;     // opcional (id de API externa)
        public final String nome;
        public final double kcal100g;
        public final double p100g;      // proteína
        public final double c100g;      // carbo
        public final double g100g;      // gordura

        public FoodInfo(String foodId, String nome,
                        double kcal100g, double p100g, double c100g, double g100g) {
            this.foodId = foodId;
            this.nome = nome;
            this.kcal100g = Math.max(0, kcal100g);
            this.p100g = Math.max(0, p100g);
            this.c100g = Math.max(0, c100g);
            this.g100g = Math.max(0, g100g);
        }
    }

    private static final Map<String, FoodInfo> MAP = new HashMap<>();

    static {
        // Valores aproximados de referências públicas (por 100 g) – bons para MVP
        putMany(new String[]{"arroz", "arroz branco", "arroz cozido"},
                new FoodInfo("rice_white_cooked", "Arroz branco cozido", 130, 2.4, 28.2, 0.3));

        putMany(new String[]{"feijao", "feijão", "feijao carioca", "feijão carioca cozido"},
                new FoodInfo("beans_pinto_cooked", "Feijão carioca cozido", 76, 4.8, 13.6, 0.3));

        putMany(new String[]{"frango", "frango grelhado", "peito de frango grelhado", "bife de frango"},
                new FoodInfo("chicken_breast_grilled", "Frango grelhado (peito)", 165, 31.0, 0.0, 3.6));

        putMany(new String[]{"batata", "batata cozida", "batata inglesa"},
                new FoodInfo("potato_boiled", "Batata cozida", 87, 1.9, 20.0, 0.1));

        // Você pode ampliar à vontade a lista acima conforme testar
    }

    private static void putMany(String[] aliases, FoodInfo info) {
        for (String a : aliases) {
            MAP.put(normalize(a), info);
        }
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    /** Busca por rótulo PT-BR (normalizado). */
    @Nullable
    public static FoodInfo findByLabel(String label) {
        return MAP.get(normalize(label));
    }
}
