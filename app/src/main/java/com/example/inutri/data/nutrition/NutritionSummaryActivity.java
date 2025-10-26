package com.example.inutri.data.nutrition;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inutri.R;
import com.example.inutri.model.MealItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Locale;

public class NutritionSummaryActivity extends AppCompatActivity {

    private ImageView imgPhoto;
    private RecyclerView recyclerSummary;
    private TextView tvTotalCalories, tvTotalMacros, emptyView;
    private ProgressBar progress;
    private MaterialButton btnCancel, btnSave;

    private SummaryAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_nutrition_summary);

        // Toolbar (voltar)
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        imgPhoto        = findViewById(R.id.imgPhoto);
        recyclerSummary = findViewById(R.id.recyclerSummary);
        tvTotalCalories = findViewById(R.id.tvTotalCalories);
        tvTotalMacros   = findViewById(R.id.tvTotalMacros);
        emptyView       = findViewById(R.id.emptyView);
        progress        = findViewById(R.id.progress);
        btnCancel       = findViewById(R.id.btnCancel);
        btnSave         = findViewById(R.id.btnSave);

        // Foto (se enviada pela tela anterior)
        String photoStr = getIntent().getStringExtra("photo_uri");
        if (photoStr != null && imgPhoto != null) {
            Uri u = Uri.parse(photoStr);
            imgPhoto.setImageURI(u);
        }

        // Recupera a lista de MealItem (Parcelable)
        ArrayList<MealItem> items;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            items = getIntent().getParcelableArrayListExtra("items", MealItem.class);
        } else {
            @SuppressWarnings("deprecation")
            ArrayList<MealItem> tmp = getIntent().getParcelableArrayListExtra("items");
            items = tmp;
        }
        if (items == null) items = new ArrayList<>();

        // Lista
        adapter = new SummaryAdapter();
        recyclerSummary.setLayoutManager(new LinearLayoutManager(this));
        recyclerSummary.setAdapter(adapter);
        adapter.submit(items);

        // Empty state
        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        // Totais (usando métodos já existentes em MealItem)
        double totKcal = 0, totP = 0, totC = 0, totG = 0;
        for (MealItem m : items) {
            if (m == null) continue;
            totKcal += m.kcal();
            totP    += m.proteina();
            totC    += m.carbo();
            totG    += m.gordura();
        }

        if (tvTotalCalories != null) {
            tvTotalCalories.setText(String.format(Locale.getDefault(), "Calorias: %.0f kcal", totKcal));
        }
        if (tvTotalMacros != null) {
            tvTotalMacros.setText(String.format(
                    Locale.getDefault(),
                    "Carb: %.1f g  •  Prot: %.1f g  •  Gord: %.1f g",
                    totC, totP, totG
            ));
        }

        // Ações
        if (btnCancel != null) btnCancel.setOnClickListener(v -> finish());
        if (btnSave != null) btnSave.setOnClickListener(v -> {
            // TODO: salvar no BD, enviar para servidor, etc.
            finish();
        });
    }
}
