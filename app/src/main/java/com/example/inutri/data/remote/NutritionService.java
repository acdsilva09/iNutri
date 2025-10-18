package com.example.inutri.data.remote;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.inutri.model.MealItem;

import java.util.Locale;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * Serviço que fornece valores nutricionais por 100 g.
 * MVP: primeiro tenta o LabelMapper local; se não achar e a API estiver configurada, consulta via Retrofit.
 */
public class NutritionService {

    // Defina sua baseUrl real quando integrar com um provedor externo (ex.: sua Cloud Function)
    private static final String DEFAULT_BASE_URL = "https://example.com/api/";

    private final Context appContext;
    @Nullable
    private final NutritionApi api; // pode ser null se você não quiser usar API agora

    public NutritionService(@NonNull Context context) {
        this(context, null); // construtor default: sem API (usa só LabelMapper)
    }

    public NutritionService(@NonNull Context context, @Nullable String baseUrl) {
        this.appContext = context.getApplicationContext();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            // Sem API neste momento (MVP offline)
            this.api = null;
        } else {
            OkHttpClient client = new OkHttpClient.Builder().build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/")
                    .addConverterFactory(MoshiConverterFactory.create())
                    .client(client)
                    .build();
            this.api = retrofit.create(NutritionApi.class);
        }
    }

    /**
     * Obtém um MealItem com valores por 100 g a partir de um rótulo em PT-BR.
     * 1) Tenta resolver com o LabelMapper local (rápido, offline).
     * 2) (Opcional) Se houver API configurada, tenta buscar remotamente.
     */
    @Nullable
    public MealItem getByLabel(@Nullable String rawLabel) {
        String label = normalize(rawLabel);

        // 1) Mapper local
        LabelMapper.FoodInfo info = LabelMapper.findByLabel(label);
        if (info != null) {
            return new MealItem(
                    info.foodId,
                    info.nome,
                    info.kcal100g,
                    info.p100g,
                    info.c100g,
                    info.g100g,
                    /* gramas (definido depois pela UI) */ 0
            );
        }

        // 2) (Opcional) Consulta remota – apenas se API estiver configurada
        if (api != null) {
            try {
                FoodSearchResponse resp =
                        api.searchFood(label, 1).execute().body();
                if (resp != null && resp.foods != null && !resp.foods.isEmpty()) {
                    FoodItemDTO dto = resp.foods.get(0);
                    return dtoToMealItem(dto);
                }
            } catch (Exception ignored) { }
        }

        // Não encontrado
        return null;
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    @Nullable
    private MealItem dtoToMealItem(@Nullable FoodItemDTO dto) {
        if (dto == null) return null;
        double kcal = nonNull(dto.kcal100g);
        double p = nonNull(dto.protein100g);
        double c = nonNull(dto.carbs100g);
        double g = nonNull(dto.fat100g);

        String nome = dto.description == null ? "Item" : dto.description;
        return new MealItem(dto.id, nome, kcal, p, c, g, 0);
    }

    private double nonNull(Double v) { return v == null ? 0.0 : v; }
}
