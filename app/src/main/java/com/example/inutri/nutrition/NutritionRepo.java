package com.example.inutri.nutrition;

import com.example.inutri.nutrition.NutritionFacts;

import java.text.Normalizer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Repositório simples, em memória, com alguns exemplos.
 * Em produção, você pode trocar por uma API/DB.
 */
public class NutritionRepo {

    // Singleton
    private static volatile NutritionRepo INSTANCE;

    public static NutritionRepo getInstance() {
        if (INSTANCE == null) {
            synchronized (NutritionRepo.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NutritionRepo();
                }
            }
        }
        return INSTANCE;
    }

    // "Banco" em memória: chave normalizada -> facts por 100 g
    private final Map<String, NutritionFacts> db = new HashMap<>();
    // Aliases: apelidos -> chave normalizada
    private final Map<String, String> aliases = new HashMap<>();

    private NutritionRepo() {
        // Exemplos básicos (por 100 g)
        put("arroz cozido", NutritionFacts.of(130f, 28f, 2.4f, 0.3f),
                "arroz", "arroz branco", "rice");
        put("feijao cozido", NutritionFacts.of(76f, 14f, 5f, 0.5f),
                "feijao", "feijão", "beans");
        put("frango grelhado", NutritionFacts.of(165f, 0f, 31f, 3.6f),
                "frango", "peito de frango", "chicken");
        put("batata cozida", NutritionFacts.of(87f, 20f, 2f, 0.1f),
                "batata", "potato", "batata inglesa");
        put("pao frances", NutritionFacts.of(270f, 53f, 9f, 3.2f),
                "pao", "pão", "pao frances", "bread", "pao de sal");
        put("maça", NutritionFacts.of(52f, 14f, 0.3f, 0.2f),
                "maca", "apple");
        put("banana", NutritionFacts.of(89f, 23f, 1.1f, 0.3f),
                "banana", "bananas");
        // …adicione o que quiser
    }

    /** Adiciona alimento e seus aliases. */
    public void put(String canonicalName, NutritionFacts facts, String... alsoKnownAs) {
        String key = normalize(canonicalName);
        db.put(key, facts);
        // o próprio nome canônico também funciona como alias
        aliases.put(key, key);
        if (alsoKnownAs != null) {
            for (String a : alsoKnownAs) {
                aliases.put(normalize(a), key);
            }
        }
    }

    /**
     * Retorna os macros por 100 g do alimento informado ou null se não achar.
     * Aceita nomes com/sem acentos e ignora maiúsculas/minúsculas.
     */
    public NutritionFacts getPer100g(String foodName) {
        if (foodName == null) return null;

        String q = normalize(foodName);

        // 1) procura por alias exato
        String key = aliases.get(q);
        if (key != null) return db.get(key);

        // 2) procura por contains (aproximação simples)
        for (Map.Entry<String, String> e : aliases.entrySet()) {
            if (e.getKey().contains(q) || q.contains(e.getKey())) {
                NutritionFacts nf = db.get(e.getValue());
                if (nf != null) return nf;
            }
        }

        // 3) fallback: tenta diretamente no db
        NutritionFacts direct = db.get(q);
        if (direct != null) return direct;

        // não encontrado
        return null;
    }

    /** Retorna um mapa imutável (útil para debug). */
    public Map<String, NutritionFacts> snapshot() {
        return Collections.unmodifiableMap(db);
    }

    // --- utils ---
    private static String normalize(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return n.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }
}
