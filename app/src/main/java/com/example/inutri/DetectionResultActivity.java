package com.example.inutri;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

// imports no topo
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Locale;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inutri.R;
import com.example.inutri.model.DetectedFood;
import com.example.inutri.model.MealItem;
import com.example.inutri.ui.DetectedFoodAdapter;

import com.example.inutri.DetectionResultViewModel;
import com.example.inutri.ui.capture.MealItemAdapter;

import java.util.ArrayList;

public class DetectionResultActivity extends AppCompatActivity {

    private ImageView imgPhoto;
    private RecyclerView recyclerFoods;
    private Button btnContinue;

    private final ArrayList<MealItem> items = new ArrayList<>();   // <- A LISTA QUE FALTAVA
    private DetectedFoodAdapter detectedAdapter;                    // opcional: se você usa esse adapter
    private MealItemAdapter itemsAdapter;                           // opcional: lista dos escolhidos

    private DetectionResultViewModel viewModel;
    private DetectedFoodAdapter adapter; // adapter com campo de gramas (abaixo)

    private Uri photoUri;
    private final ArrayList<MealItem> selectedItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_detection_result);

        imgPhoto = findViewById(R.id.imgPhoto);
        recyclerFoods = findViewById(R.id.recyclerFoods);
        btnContinue = findViewById(R.id.btnContinue);

        viewModel = new ViewModelProvider(this).get(DetectionResultViewModel.class);

        String uriStr = getIntent().getStringExtra("photo_uri");
        photoUri = Uri.parse(uriStr);



        imgPhoto.setImageURI(photoUri);

        adapter = new DetectedFoodAdapter(
                /* onGramsChanged= */ (position, label, grams) -> {
            // mantenha a lista sincronizada com o que o usuário digita
            ensureMealItem(label, grams);
        }
        );
        recyclerFoods.setAdapter(adapter);
        recyclerFoods.setLayoutManager(new LinearLayoutManager(this));

        // Observa resultados
        viewModel.getDetectedFoods().observe(this, foods -> {
            adapter.submitList(foods);
            // Inicializa MealItem com 0g para cada
            selectedItems.clear();
            for (DetectedFood f : foods) {
                selectedItems.add(new MealItem(f.getName(), 0f));
            }
        });

        // Roda detecção
        viewModel.detectFoods(photoUri);

        btnContinue.setOnClickListener(v -> {
            // filtra só quem tem >0g
            ArrayList<MealItem> chosen = new ArrayList<>();
            for (MealItem mi : selectedItems) {
                if (mi.getGramas() > 0f) chosen.add(mi);
            }
            if (chosen.isEmpty()) {
                Toast.makeText(this, "Informe as gramas dos itens que deseja incluir.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(this, NutritionSummaryActivity.class);
            i.putParcelableArrayListExtra("meal_items", (ArrayList<? extends Parcelable>) chosen);
            startActivity(i);
        });

        ArrayList<MealItem> incoming =
                getIntent().getParcelableArrayListExtra("meal_items");
        if (incoming != null) {
            items.clear();
            items.addAll(incoming);
        }

        // adapter das detecções (usa o callback para sincronizar com items)
        detectedAdapter = new DetectedFoodAdapter((position, label, grams) -> {
            ensureMealItem(label, grams);    // <- atualiza a lista escolhida
        });
        recyclerFoods.setAdapter(detectedAdapter);


// Observa as detecções do ViewModel e entrega para o adapter correto
        viewModel.getDetectedFoods().observe(this, foods -> {
            detectedAdapter.submitList(foods);      // <- AQUI vai List<DetectedFood>
        });
    }

    private void ensureMealItem(@NonNull String rawLabel, float grams) {
        String label = safeLabel(rawLabel);

        int idx = -1;
        for (int i = 0; i < items.size(); i++) {
            String existing = items.get(i).getNome();
            if (existing != null && existing.equalsIgnoreCase(label)) {
                idx = i; break;
            }
        }

        if (idx < 0) {
            items.add(new MealItem(label, grams));
        } else {
            items.get(idx).setGramas(grams);
        }

        // atualize a UI
        if (itemsAdapter != null) {
            itemsAdapter.submitList(new ArrayList<>(items)); // ou itemsAdapter.setData(...); notifyDataSetChanged()
        }
    }

    private String safeLabel(String s) {
        if (s == null) return "Alimento";
        s = s.trim();
        return s.isEmpty() ? "Alimento" : s;
    }

}
