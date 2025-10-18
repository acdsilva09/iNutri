package com.example.inutri.data.remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NutritionApi {

    // Busca por texto
    @GET("foods/search")
    Call<FoodSearchResponse> searchFood(@Query("q") String query,
                                        @Query("pageSize") Integer pageSize);

    // Detalhe por ID
    @GET("foods/{id}")
    Call<FoodDetailResponse> getFoodDetail(@Path("id") String id);
}
