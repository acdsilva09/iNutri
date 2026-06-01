package com.example.inutri.ui.auth;

import android.util.Patterns;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AuthViewModel extends ViewModel {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>();
    public LiveData<Boolean> loading = _loading;

    private final MutableLiveData<Boolean> _cadastroSucesso = new MutableLiveData<>();
    public LiveData<Boolean> cadastroSucesso = _cadastroSucesso;

    public void cadastrar(String nome, String email, String senha) {
        if (!validarCampos(nome, email, senha)) return;

        _loading.setValue(true);

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && !nome.isBlank()) {
                            UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nome)
                                    .build();
                            user.updateProfile(req).addOnCompleteListener(profileTask -> {
                                _loading.setValue(false);
                                _cadastroSucesso.setValue(true);
                            });
                        } else {
                            _loading.setValue(false);
                            _cadastroSucesso.setValue(true);
                        }
                    } else {
                        _loading.setValue(false);
                        _error.setValue(traduzErro(task.getException()));
                    }
                });
    }

    private boolean validarCampos(String nome, String email, String senha) {
        if (nome == null || nome.isBlank() || email == null || email.isBlank() || senha == null || senha.isBlank()) {
            _error.setValue("Preencha todos os campos");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _error.setValue("E-mail inválido");
            return false;
        }
        if (senha.length() < 6) {
            _error.setValue("A senha deve ter no mínimo 6 caracteres");
            return false;
        }
        return true;
    }

    private String traduzErro(Exception e) {
        if (e instanceof FirebaseAuthWeakPasswordException) return "Senha fraca. Use ao menos 6 caracteres";
        if (e instanceof FirebaseAuthInvalidCredentialsException) return "E-mail inválido";
        if (e instanceof FirebaseAuthUserCollisionException) return "Já existe uma conta com este e-mail";
        return "Erro ao finalizar cadastro: " + (e != null ? e.getMessage() : "Erro desconhecido");
    }
}
