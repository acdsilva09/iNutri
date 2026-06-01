package com.example.inutri.data.repository;

import android.content.Context;
import android.net.Uri;

import android.util.Log;
import com.example.inutri.data.network.RetrofitClient;
import com.example.inutri.model.api.NutritionResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NutritionRepository {

    private static final String AUTH_TOKEN = "Bearer NCbpvdmO2cXAr4H0MGVfEiWoyTnzl9LJYgIRhuekF753tx6D";

    public interface NutritionCallback {
        void onSuccess(NutritionResponse response);
        void onError(String message);
    }

    public void analyzeImage(Context context, Uri uri, NutritionCallback callback) {
        Log.d("NutritionRepository", "analyzeImage: Iniciando análise para URI: " + uri);
        File file = getFileFromUri(context, uri);
        if (file == null) {
            Log.e("NutritionRepository", "analyzeImage: Falha ao obter arquivo da URI");
            callback.onError("Não foi possível processar a imagem");
            return;
        }

        Log.d("NutritionRepository", "analyzeImage: Arquivo criado: " + file.getAbsolutePath() + " Size: " + file.length());
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        RetrofitClient.getApi().analyzeNutrition(AUTH_TOKEN, body).enqueue(new Callback<NutritionResponse>() {
            @Override
            public void onResponse(Call<NutritionResponse> call, Response<NutritionResponse> response) {
                Log.d("NutritionRepository", "onResponse: Código " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("NutritionRepository", "onResponse: Sucesso! Itens: " + (response.body().items != null ? response.body().items.size() : 0));
                    callback.onSuccess(response.body());
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) errorBody = response.errorBody().string();
                    } catch (IOException ignored) {}
                    Log.e("NutritionRepository", "onResponse: Erro. Body: " + errorBody);
                    callback.onError("Erro na análise: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<NutritionResponse> call, Throwable t) {
                Log.e("NutritionRepository", "onFailure: Falha na conexão", t);
                callback.onError("Falha na conexão: " + t.getMessage());
            }
        });
    }

    private File getFileFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("upload", ".jpg", context.getCacheDir());
            tempFile.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                return tempFile;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
