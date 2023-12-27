package com.example.inutri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.inutri.databinding.ActivityCadastroBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class CadastroActivity extends AppCompatActivity {


    ActivityCadastroBinding binding;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
    }


    public void clickButtonFinalizaCadastro(View v){
        String nome = binding.cadastroNome.getText().toString();
        String email = binding.cadastroEmail.getText().toString();
        String senha = binding.cadastrSenha.getText().toString();

        if(!email.isEmpty() && !senha.isEmpty() && !nome.isEmpty()){


            if(senha.length()>=6) {
                mAuth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        Intent i = new Intent(com.example.inutri.CadastroActivity.this, LoginActivity.class);
                        startActivity(i);
                        finishAffinity();

                    } else {
                        Toast.makeText(this, "Erro ao finalizar cadastro!", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                Toast.makeText(this,"A senha deve contar no mínimo 6 caracteres",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this,"Preencha todos os campos!",Toast.LENGTH_SHORT).show();
        }

    }


}