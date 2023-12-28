package com.example.inutri;


import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.inutri.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {



    ActivityLoginBinding binding;
    GoogleSignInClient googleSignInClient;
    //Button buttonLogin, buttonGoogleLogin;
    //EditText editTextUser, editTextPassword;
    private FirebaseAuth mAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions);

        binding.buttonLogin.setOnClickListener(view -> clickButtonLogin(binding.editTextUser.getText().toString(),
                binding.editTextPassword.getText().toString()));

        binding.buttonGoogleLogin.setOnClickListener(v -> {
            clickButtonLoginGoogle();
        });


    }

    private void clickButtonLoginGoogle() {
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent,1);
       // openActivity.launch(intent);
    }

    ActivityResultLauncher<Intent> openActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode()== Activity.RESULT_OK){
                    Intent intent = result.getData();

                    Task<GoogleSignInAccount> tarefa = GoogleSignIn.getSignedInAccountFromIntent(intent);
                    Log.d(TAG, "tarefa:"+ tarefa.getResult()+ "\nIntent:" + intent.getData());
                    try {
                        GoogleSignInAccount conta = tarefa.getResult(ApiException.class);
                        loginGoogle(conta.getIdToken());
                    } catch (ApiException e) {
                        // Tratamento específico para ApiException ao obter a conta do Google Sign-In
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Erro ao efetuar login: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void loginGoogle(String token) {

        try {
            AuthCredential authCredential = GoogleAuthProvider.getCredential(token, null);
            mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Login Google Efetuado com Sucesso!!.",
                            Toast.LENGTH_SHORT).show();
                    startMainActivity();
                } else {
                    Toast.makeText(getApplicationContext(), "Erro ao efetuar login Google",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (RuntimeException e){

        }
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == 1) {
            Task<GoogleSignInAccount> tarefa = GoogleSignIn.getSignedInAccountFromIntent(intent);

            try {
                        GoogleSignInAccount conta = tarefa.getResult(ApiException.class);
                        Toast.makeText(getApplicationContext(), ""+ conta.getIdToken(),
                        Toast.LENGTH_SHORT).show();
                        if(!conta.getIdToken().isEmpty()){
                            loginGoogle(conta.getIdToken());
                        }else{
                            Toast.makeText(getApplicationContext(), "Erro ao capturar Token",
                                    Toast.LENGTH_SHORT).show();
                        }

                    } catch (ApiException e) {
                        // Tratamento específico para ApiException ao obter a conta do Google Sign-In
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Erro ao efetuar login: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
        } else {
            // Se o requestCode não for igual a 1, lidar com isso adequadamente
            Toast.makeText(getApplicationContext(), "Código de solicitação inválido",
                    Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    public void clickButtonCadastro(View v){

        Intent i = new Intent(LoginActivity.this, CadastroActivity.class);
        startActivity(i);
        finishAffinity();
    }

    public void clickButtonRecuperaSenha(View v){

        Intent i = new Intent(LoginActivity.this, RecuperaCadastroActivity.class);
        startActivity(i);
        finishAffinity();
    }


    private void clickButtonLogin(String email, String password ){

        System.out.println("____________"+email+"________"+password);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCustomToken:success");

                            Toast.makeText(getApplicationContext(), "Login Efetuado com Sucesso!!.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            startMainActivity();




                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithCustomToken:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });

    }

    public void startMainActivity(){
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finishAffinity();
    }





}