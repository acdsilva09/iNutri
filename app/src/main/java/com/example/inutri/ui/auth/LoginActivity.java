package com.example.inutri.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.inutri.R;
import com.example.inutri.databinding.ActivityLoginBinding;
import com.example.inutri.ui.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleClient;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() == null) {
                    Toast.makeText(this, "Operação cancelada", Toast.LENGTH_SHORT).show();
                    return;
                }
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account == null || account.getIdToken() == null) {
                        Toast.makeText(this, "Não foi possível obter o token", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AuthCredential cred = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    mAuth.signInWithCredential(cred).addOnCompleteListener(this, t -> {
                        if (t.isSuccessful()) {
                            startMainActivity();
                        } else {
                            Toast.makeText(this, "Falha no login Google", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (ApiException e) {
                    Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // use a string gerada pelo Firebase (default_web_client_id)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);

        binding.buttonLogin.setOnClickListener(v ->
                clickButtonLogin(
                        safe(binding.editTextUser.getText()),
                        safe(binding.editTextPassword.getText()))
        );

        binding.buttonGoogleLogin.setOnClickListener(v ->
                googleLauncher.launch(googleClient.getSignInIntent())
        );

        binding.textCadastro.setOnClickListener(v -> {
            startActivity(new Intent(this, CadastroActivity.class));
        });

        binding.textEsqueciSenha.setOnClickListener(v -> {
            startActivity(new Intent(this, RecuperaCadastroActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser current = mAuth.getCurrentUser();
        if (current != null) startMainActivity();
    }

    private void clickButtonLogin(@NonNull String email, @NonNull String password) {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Informe e-mail e senha", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "E-mail inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                startMainActivity();
            } else {
                String msg = "Falha na autenticação";
                Exception e = task.getException();
                if (e instanceof FirebaseAuthInvalidUserException) msg = "Usuário não encontrado";
                else if (e instanceof FirebaseAuthInvalidCredentialsException) msg = "Credenciais inválidas";
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private String safe(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }
}
