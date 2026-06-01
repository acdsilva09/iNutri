package com.example.inutri.ui.meal;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inutri.R;
import com.example.inutri.ui.capture.CaptureMealActivity;

public class AddMealActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        findViewById(R.id.btnCaptureAI).setOnClickListener(v -> {
            startActivity(new Intent(this, CaptureMealActivity.class));
        });

        findViewById(R.id.btnManualEntry).setOnClickListener(v -> {
            // Futura implementação: Tela de formulário manual
        });
    }
}
