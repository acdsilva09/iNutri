package com.example.inutri;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inutri.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

public class MainActivity extends AppCompatActivity {


    ActivityMainBinding binding;
    GoogleSignInClient googleSignInClient;
    Button buttonCalcularTMB;
    EditText editTextTextPeso, editTextTextIdade, editTextTextAltura;
    RadioButton radioButtonSexoM, radioButtonSexoF;
    TextView textViewResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        View view = binding.getRoot();

        setContentView(view);



        editTextTextAltura = (EditText)findViewById(R.id.editTextTextAltura);
        editTextTextPeso = (EditText)findViewById(R.id.editTextTextPeso);
        editTextTextIdade = (EditText)findViewById(R.id.editTextTextIdade);
        radioButtonSexoF = (RadioButton) findViewById(R.id.radioButtonSexoF);
        radioButtonSexoM = (RadioButton) findViewById(R.id.radioButtonSexoM);
        buttonCalcularTMB = (Button) findViewById(R.id.buttonCalcularTMB);
        textViewResultado = (TextView) findViewById(R.id.textViewResultado);
    }


    public void clickButtonCalcular(View view){
        if(editTextTextIdade.getText().length()>0
                && editTextTextPeso.getText().length()>0
                && editTextTextAltura.getText().length()>0){
            if(radioButtonSexoF.isChecked()){
                calculaTMBFeminino();
            } else if(radioButtonSexoM.isChecked()){
                calculaTMBMasculino();
            }

        }
    }

    private void calculaTMBFeminino() {
        int altura = Integer.parseInt(String.valueOf(editTextTextAltura.getText()));
        double peso = Double.parseDouble(String.valueOf(editTextTextPeso.getText()));
        int idade = Integer.parseInt(String.valueOf(editTextTextIdade.getText()));

        double resultado = 665 + 9.6 * peso + 1.8 * altura - 4.7 * idade;
        textViewResultado.setText(String.valueOf(resultado));
    }

    private void calculaTMBMasculino() {
        int altura = Integer.parseInt(String.valueOf(editTextTextAltura.getText()));
        double peso = Double.parseDouble(String.valueOf(editTextTextPeso.getText()));
        int idade = Integer.parseInt(String.valueOf(editTextTextIdade.getText()));

        double resultado = 66 + 13.7 * peso + 5 * altura - 6.8 * idade;

        textViewResultado.setText(String.valueOf(resultado));
    }


}