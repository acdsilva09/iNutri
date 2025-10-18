package com.example.inutri.ui.capture;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.inutri.data.remote.ImageRecognitionService;
import com.example.inutri.data.remote.NutritionService;
import com.example.inutri.data.repository.MealRepository;
import com.example.inutri.model.DetectedFood;
import com.example.inutri.model.MealItem;
import com.example.inutri.model.MealRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel da tela de captura.
 * Fluxo: detectFoods(uri) -> lista de DetectedFood -> fetchNutrition() -> MealItems -> Totais -> saveMeal().
 */
public class CaptureViewModel extends AndroidViewModel {

    // --- UI State ---
    public enum UiState { IDLE, LOADING, READY, ERROR }

    private final MutableLiveData<UiState> uiState = new MutableLiveData<>(UiState.IDLE);
    private final MutableLiveData<String> lastErrorMessage = new MutableLiveData<>("");

    // Detecções (rótulos + bboxes)
    private final MutableLiveData<List<DetectedFood>> detectedFoods = new MutableLiveData<>(Collections.emptyList());
    // Itens com nutrição (por 100 g) + gramas da porção
    private final MutableLiveData<List<MealItem>> mealItems = new MutableLiveData<>(Collections.emptyList());
    // Totais (kcal e macros)
    private final MutableLiveData<Totals> totals = new MutableLiveData<>(new Totals());

    // --- Serviços/Repos ---
    private final ImageRecognitionService imageRecognitionService;
    private final NutritionService nutritionService;
    private final MealRepository mealRepository;

    private final ExecutorService io = Executors.newSingleThreadExecutor();

    // Guarda a última foto usada no fluxo (para salvar depois)
    private Uri currentImageUri;

    public CaptureViewModel(@NonNull Application app) {
        super(app);
        // Crie os serviços conforme seus construtores concretos
        imageRecognitionService = new ImageRecognitionService(app);
        nutritionService = new NutritionService(app);
        mealRepository = new MealRepository();
    }

    // region Exposed LiveData
    public LiveData<UiState> getUiState() { return uiState; }
    public LiveData<String> getLastErrorMessage() { return lastErrorMessage; }
    public LiveData<List<DetectedFood>> getDetectedFoods() { return detectedFoods; }
    public LiveData<List<MealItem>> getMealItems() { return mealItems; }
    public LiveData<Totals> getTotals() { return totals; }
    // endregion

    // region Flow: Detect -> Nutrition -> Totals
    public void detectFoods(@NonNull Uri imageUri) {
        currentImageUri = imageUri;
        uiState.postValue(UiState.LOADING);
        lastErrorMessage.postValue("");

        io.execute(() -> {
            try {
                // 1) Detecção de alimentos (label + bbox + confiança)
                List<DetectedFood> detections = imageRecognitionService.detect(imageUri);
                if (detections == null) detections = new ArrayList<>();
                detectedFoods.postValue(detections);

                // 2) Mapeia cada detecção para um MealItem com valores por 100 g via API nutricional
                List<MealItem> items = new ArrayList<>();
                for (DetectedFood d : detections) {
                    // Busca por rótulo (o serviço pode internamente usar um LabelMapper -> foodId)
                    MealItem base = nutritionService.getByLabel(d.label);
                    if (base == null) {
                        base = new MealItem();
                        base.nome = (d.label == null ? "Item" : d.label);
                        base.kcal100g = base.p100g = base.c100g = base.g100g = 0.0;
                    }
                    // Estimativa inicial simples (poderá ser ajustada pelo usuário via slider/adapter)
                    base.gramas = estimateGrams(d);
                    items.add(base);
                }
                mealItems.postValue(items);

                // 3) Calcula totais
                recalcTotals(items);

                uiState.postValue(UiState.READY);
            } catch (Exception e) {
                lastErrorMessage.postValue("Falha na detecção: " + e.getMessage());
                uiState.postValue(UiState.ERROR);
            }
        });
    }

    /** Atualiza a gramagem de um item (chamado pelo adapter do slider). */
    public void updateGrams(int position, int grams) {
        List<MealItem> list = mealItems.getValue();
        if (list == null || position < 0 || position >= list.size()) return;
        list.get(position).gramas = grams;
        mealItems.setValue(list);
        recalcTotals(list);
    }

    /** Remove um item (caso o usuário descarte uma detecção equivocada). */
    public void removeItem(int position) {
        List<MealItem> list = mealItems.getValue();
        if (list == null || position < 0 || position >= list.size()) return;
        list.remove(position);
        mealItems.setValue(list);
        recalcTotals(list);
    }

    private void recalcTotals(List<MealItem> items) {
        Totals t = new Totals();
        if (items != null) {
            for (MealItem m : items) {
                double factor = (m.gramas <= 0 ? 0 : m.gramas / 100.0);
                t.kcal += m.kcal100g * factor;
                t.protein += m.p100g * factor;
                t.carbs += m.c100g * factor;
                t.fat += m.g100g * factor;
            }
        }
        totals.postValue(t);
    }
    // endregion

    // region Save
    public interface SaveCallback {
        void onResult(boolean success, String message);
    }

    /** Salva a refeição no Firestore + Storage (foto, itens, totais, timestamp). */
    public void saveMeal(@NonNull SaveCallback cb) {
        List<MealItem> items = mealItems.getValue();
        if (items == null || items.isEmpty() || currentImageUri == null) {
            cb.onResult(false, "Nada para salvar");
            return;
        }

        MealRecord record = new MealRecord();
        record.timestamp = System.currentTimeMillis();
        record.itens = new ArrayList<>(items); // cópia
        // totais já calculados
        Totals t = totals.getValue() == null ? new Totals() : totals.getValue();
        // (Se seu MealRecord tiver campos de totais específicos, você pode setá-los aqui também)

        uiState.postValue(UiState.LOADING);
        mealRepository.saveMeal(currentImageUri, record, success -> {
            if (success) {
                uiState.postValue(CaptureViewModel.UiState.READY);
                cb.onResult(true, "Refeição salva com sucesso");
            } else {
                uiState.postValue(CaptureViewModel.UiState.ERROR);
                cb.onResult(false, "Falha ao salvar a refeição");
            }
        });
    }
    // endregion

    // region Helpers
    /** Estimativa inicial de gramas por detecção. Ajuste conforme seu heurístico. */
    private int estimateGrams(DetectedFood d) {
        // Heurística simples: base por tipo + confiança
        String label = d == null || d.label == null ? "" : d.label.toLowerCase(Locale.ROOT);
        int base = 150; // default
        if (label.contains("arroz")) base = 150;
        else if (label.contains("feij")) base = 100;
        else if (label.contains("frango") || label.contains("bife")) base = 140;
        else if (label.contains("batata")) base = 120;

        float conf = (d == null ? 0.7f : Math.max(0, Math.min(1, d.confidence)));
        return Math.max(50, Math.round(base * (0.8f + 0.4f * conf))); // 80%..120% do base
    }

    public String getLastErrorText() {
        String s = lastErrorMessage.getValue();
        return s == null ? "" : s;
    }
    // endregion

    @Override
    protected void onCleared() {
        super.onCleared();
        io.shutdown();
    }

    // Pequena classe de totais exposta à UI
    public static class Totals {
        public double kcal = 0;
        public double protein = 0;
        public double carbs = 0;
        public double fat = 0;
    }
}
