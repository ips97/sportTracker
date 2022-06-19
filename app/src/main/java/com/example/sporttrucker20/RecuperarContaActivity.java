package com.example.sporttrucker20;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.sporttrucker20.databinding.ActivityRecuperarContaBinding;
import com.google.firebase.auth.FirebaseAuth;

public class RecuperarContaActivity extends AppCompatActivity {

    private ActivityRecuperarContaBinding binding;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecuperarContaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btRecuperar.setOnClickListener(v -> validaDados());
    }

    private void validaDados(){
        //Recuperando os dados de usuário
        String email = binding.editEmail.getText().toString().trim();


        if (!email.isEmpty()) {
            binding.progressbar.setVisibility(View.VISIBLE);
            recuperaContaFirebase(email);

        }else {
            Toast.makeText(this, "Informe seu e-mail!", Toast.LENGTH_SHORT).show();
        }


    }

    private void recuperaContaFirebase(String email){
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast.makeText(this, "Email enviado, verifique sua caixa de mensagens", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Ocorreu um erro", Toast.LENGTH_SHORT).show();
            }
            binding.progressbar.setVisibility(View.GONE);
        });
    }
}