package com.example.inutri.data.remote;

import android.content.Context;
import android.graphics.RectF;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.inutri.model.DetectedFood;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Serviço de detecção de alimentos a partir de uma imagem.
 * MVP: retorna resultados MOCK para testar o fluxo ponta a ponta.
 * Futuro: trocar para chamada HTTP (endpoint próprio) ou TFLite on-device.
 */
public class ImageRecognitionService {

    private final Context appContext;

    // Ligue/desligue o modo mock facilmente
    private static final boolean USE_MOCK = true;

    public ImageRecognitionService(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
    }

    /**
     * Detecta alimentos na imagem.
     * @param imageUri Uri local (FileProvider/cache) da foto capturada
     */
    @NonNull
    public List<DetectedFood> detect(@NonNull Uri imageUri) throws Exception {
        if (USE_MOCK) return mockDetections();

        // Exemplo para quando for remoto:
        // 1) Ler bytes do arquivo
        // 2) Enviar multipart para endpoint
        // 3) Parsear resposta em List<DetectedFood>
        // throw new UnsupportedOperationException("Detecção remota ainda não implementada.");
        return mockDetections();
    }

    private List<DetectedFood> mockDetections() {
        List<DetectedFood> out = new ArrayList<>();
        return out;
    }

    /** Utilitário simples para normalizar rótulos. */
    public static String normalizeLabel(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase(Locale.ROOT);
    }
}
