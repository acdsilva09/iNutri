package com.example.inutri.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.inutri.databinding.ActivityCadastroBinding;

public class CadastroActivity extends AppCompatActivity {

    private ActivityCadastroBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupObservers();
    }

    private void setupObservers() {
        viewModel.loading.observe(this, isLoading -> {
            setInputsEnabled(!isLoading);
            // Aqui você poderia mostrar um ProgressBar se tivesse um no XML
        });

        viewModel.error.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });

        viewModel.cadastroSucesso.observe(this, sucesso -> {
            if (sucesso) {
                Toast.makeText(this, "Cadastro concluído", Toast.LENGTH_SHORT).show();
                navegarParaLogin();
            }
        });
    }

    public void clickButtonFinalizaCadastro(View v) {
        String nome = safeText(binding.cadastroNome);
        String email = safeText(binding.cadastroEmail);
        String senha = safeText(binding.cadastroSenha);

        viewModel.cadastrar(nome, email, senha);
    }

    private void navegarParaLogin() {
        // encerra esta activity e volta para a tela de login
        Intent i = new Intent(CadastroActivity.this, LoginActivity.class);
        // limpa a pilha acima do login para evitar back para o cadastro
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private String safeText(android.widget.TextView tv) {
        return tv == null || tv.getText() == null ? "" : tv.getText().toString().trim();
    }

    private void setInputsEnabled(boolean enabled) {
        binding.cadastroNome.setEnabled(enabled);
        binding.cadastroEmail.setEnabled(enabled);
        binding.cadastroSenha.setEnabled(enabled); // id corrigido para 'cadastroSenha'
        // se tiver um botão dedicado, desabilite aqui também, ex.:
        // binding.btnFinalizarCadastro.setEnabled(enabled);
    }
}
