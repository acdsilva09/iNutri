package com.example.inutri;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.inutri.databinding.ActivityRecuperaCadastroBinding;
import com.google.firebase.auth.FirebaseAuth;

public class RecuperaCadastroActivity extends AppCompatActivity {

    private ActivityRecuperaCadastroBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecuperaCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // se preferir via código em vez de onClick no XML:
        // binding.btnRecuperar.setOnClickListener(this::clickButtonRecuperaCadastro);
    }

    // vincule este método ao botão no XML: android:onClick="clickButtonRecuperaCadastro"
    public void clickButtonRecuperaCadastro(View v) {
        String email = safeText(binding.recuperaEmail);

        if (email.isBlank()) {
            Toast.makeText(this, "Informe o e-mail", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "E-mail inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        setInputsEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setInputsEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "E-mail de recuperação enviado", Toast.LENGTH_LONG).show();
                        finish(); // volta para a tela anterior (login)
                    } else {
                        Toast.makeText(this, "Erro ao processar solicitação", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String safeText(android.widget.TextView tv) {
        return tv == null || tv.getText() == null ? "" : tv.getText().toString().trim();
    }

    private void setInputsEnabled(boolean enabled) {
        binding.recuperaEmail.setEnabled(enabled);
        // se tiver botão dedicado:
        // binding.btnRecuperar.setEnabled(enabled);
    }
}
