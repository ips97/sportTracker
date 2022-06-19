package com.example.sporttrucker20;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sporttrucker20.databinding.ActivityCadastroBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class CadastroActivity extends AppCompatActivity {

    private ActivityCadastroBinding binding;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btCadastrar.setOnClickListener(v -> validaDados());
    }

    private void validaDados(){
        //Recuperando os dados de usuário
        String nome = binding.editNome.getText().toString().trim();
        String email = binding.editEmail.getText().toString().trim();
        String senha = binding.editPassword.getText().toString().trim();

        if (!nome.isEmpty()){
            if (!email.isEmpty()) {
                if (!senha.isEmpty()){

                    binding.progressbar.setVisibility(View.VISIBLE);
                    criarContaFirebase(email, senha);

                }else{
                    Toast.makeText(this, "Informe uma senha.", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(this, "Informe seu e-mail!", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Informe seu nome!", Toast.LENGTH_SHORT).show();
        }

    }

    private void criarContaFirebase(String email, String senha){
        mAuth.createUserWithEmailAndPassword(
                email, senha
        ).addOnCompleteListener(task -> {
            if (task.isSuccessful()){

                finish();
                startActivity(new Intent(this, MainActivity.class));

            }else{
                binding.progressbar.setVisibility(View.GONE);
                Toast.makeText(this, "Ocorreu um erro", Toast.LENGTH_SHORT).show();
            }
        });
    }
}