package com.example.inutri;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.inutri.databinding.ActivityCadastroBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class CadastroActivity extends AppCompatActivity {

    private ActivityCadastroBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
    }

    // vincule este método no onClick do botão no XML, se ainda não estiver
    public void clickButtonFinalizaCadastro(View v) {
        String nome  = safeText(binding.cadastroNome);
        String email = safeText(binding.cadastroEmail);
        // atenção: seu layout antigo tinha "cadastrSenha" com typo; mantenha igual ao id real do XML
        String senha = safeText(binding.cadastrSenha);

        if (!validarCampos(nome, email, senha)) return;

        setInputsEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && nome != null && !nome.isBlank()) {
                            UserProfileChangeRequest req =
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(nome)
                                            .build();
                            user.updateProfile(req);
                        }
                        Toast.makeText(this, "Cadastro concluído", Toast.LENGTH_SHORT).show();
                        navegarParaLogin();
                    } else {
                        String msg = traduzErro(task.getException());
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        setInputsEnabled(true);
                    }
                });
    }

    private boolean validarCampos(String nome, String email, String senha) {
        if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "E-mail inválido", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (senha.length() < 6) {
            Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void navegarParaLogin() {
        // encerra esta activity e volta para a tela de login
        Intent i = new Intent(CadastroActivity.this, LoginActivity.class);
        // limpa a pilha acima do login para evitar back para o cadastro
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private String traduzErro(Exception e) {
        if (e == null) return "Erro ao finalizar cadastro";
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "Senha fraca. Use ao menos 6 caracteres";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "E-mail inválido";
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "Já existe uma conta com este e-mail";
        }
        return "Erro ao finalizar cadastro";
    }

    private String safeText(android.widget.TextView tv) {
        return tv == null || tv.getText() == null ? "" : tv.getText().toString().trim();
    }

    private void setInputsEnabled(boolean enabled) {
        binding.cadastroNome.setEnabled(enabled);
        binding.cadastroEmail.setEnabled(enabled);
        binding.cadastrSenha.setEnabled(enabled); // mantenha o id igual ao do seu XML
        // se tiver um botão dedicado, desabilite aqui também, ex.:
        // binding.btnFinalizarCadastro.setEnabled(enabled);
    }
}
