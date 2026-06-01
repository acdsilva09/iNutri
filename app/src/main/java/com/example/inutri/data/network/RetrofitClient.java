package com.example.inutri.data.network;

import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://inutri-api-oppdntv5ga-rj.a.run.app/";
    private static Retrofit retrofit = null;

    public static NutritionApi getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build();
        }
        return retrofit.create(NutritionApi.class);
    }
}
