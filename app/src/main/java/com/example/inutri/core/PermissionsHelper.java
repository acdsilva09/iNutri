package com.example.inutri.core;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * Helper simples para lidar com permissões em runtime.
 * Para a Etapa 1, precisamos essencialmente da CAMERA.
 * (Se futuramente você ler imagens da galeria em Android 13+, pode usar READ_MEDIA_IMAGES.)
 */
public final class PermissionsHelper {

    private PermissionsHelper() {}

    /** Retorna true se a permissão de câmera já foi concedida. */
    public static boolean hasCameraPermission(@NonNull Context ctx) {
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Array de permissões necessárias para usar a câmera (pode ser usado no ActivityResultLauncher). */
    public static String[] cameraPermissions() {
        // Apenas CAMERA por enquanto; mantenha simples
        return new String[]{ Manifest.permission.CAMERA };
    }

    /** Exemplo para leitura de mídia (se precisar no futuro). */
    public static String[] readImagesPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{ Manifest.permission.READ_MEDIA_IMAGES };
        } else {
            // Em APIs antigas, normalmente usava-se READ_EXTERNAL_STORAGE (hoje deprecada).
            // Evite pedir se não for estritamente necessário.
            return new String[0];
        }
    }
}
