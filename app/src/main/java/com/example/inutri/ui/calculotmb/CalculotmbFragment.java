package com.example.inutri.ui.calculotmb;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.inutri.R;
import com.example.inutri.databinding.FragmentCalculotmbBinding;
import com.example.inutri.databinding.FragmentCameraBinding;

public class CalculotmbFragment extends Fragment {

    Button buttonCalcularTMB;
    EditText editTextTextPeso, editTextTextIdade, editTextTextAltura;
    RadioButton radioButtonSexoM, radioButtonSexoF;
    TextView textViewResultado;


    private FragmentCalculotmbBinding binding;
    private View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCalculotmbBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        binding.buttonCalcularTMB.setOnClickListener(v -> clickButtonCalcular());

        return root;
    }


    public void clickButtonCalcular(){
        if(binding.editTextTextIdade.getText().length()>0
                && binding.editTextTextPeso.getText().length()>0
                && binding.editTextTextAltura.getText().length()>0){
            if(binding.radioButtonSexoF.isChecked()){
                calculaTMBFeminino();
            } else if(binding.radioButtonSexoM.isChecked()){
                calculaTMBMasculino();
            }

        }
    }

    private void calculaTMBFeminino() {
        int altura = Integer.parseInt(String.valueOf(binding.editTextTextAltura.getText()));
        double peso = Double.parseDouble(String.valueOf(binding.editTextTextPeso.getText()));
        int idade = Integer.parseInt(String.valueOf(binding.editTextTextIdade.getText()));

        double resultado = 665 + 9.6 * peso + 1.8 * altura - 4.7 * idade;
        binding.textViewResultado.setText(String.valueOf(resultado));
    }

    private void calculaTMBMasculino() {
        int altura = Integer.parseInt(String.valueOf(binding.editTextTextAltura.getText()));
        double peso = Double.parseDouble(String.valueOf(binding.editTextTextPeso.getText()));
        int idade = Integer.parseInt(String.valueOf(binding.editTextTextIdade.getText()));

        double resultado = 66 + 13.7 * peso + 5 * altura - 6.8 * idade;

        binding.textViewResultado.setText(String.valueOf(resultado));
    }


}