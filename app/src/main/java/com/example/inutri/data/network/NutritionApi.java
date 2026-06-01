package com.example.inutri.data.network;

import com.example.inutri.model.api.NutritionResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface NutritionApi {
    @Multipart
    @POST("api/v1/nutrition/analyze")
    Call<NutritionResponse> analyzeNutrition(
            @Header("Authorization") String authHeader,
            @Part MultipartBody.Part image
    );
}
