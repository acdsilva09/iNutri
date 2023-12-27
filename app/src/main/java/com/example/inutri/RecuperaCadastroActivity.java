package com.example.inutri;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.inutri.databinding.ActivityCadastroBinding;
import com.example.inutri.databinding.ActivityRecuperaCadastroBinding;
import com.google.firebase.auth.FirebaseAuth;

public class RecuperaCadastroActivity extends AppCompatActivity {

    ActivityRecuperaCadastroBinding binding;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecuperaCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();

    }

    public void clickButtonRecuperaCadastro(View v){
        String email = binding.recuperaEmail.getText().toString();


        if(!email.isEmpty()){

            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(this, "Email de recuperação enviado", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "Erro ao processar solicitação", Toast.LENGTH_SHORT).show();
                }
            });


        }else{
            Toast.makeText(this,"Informe o email",Toast.LENGTH_SHORT).show();
        }

    }

}