package com.example.sporttrucker20.Fragments;


import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.sporttrucker20.R;
import com.example.sporttrucker20.databinding.FragmentSettingsBinding;
import com.google.android.material.snackbar.Snackbar;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    public SettingsFragment() {
        // Required empty public constructor
    }

    private SettingsFragment settingsFragment;
    View viewAux;
    Button salvar, voltar;
    RadioButton caminhada, corrida, pedalada, km, ms, north, course, vetorial, satelite;
    RadioGroup groupExercicio, groupVelocidade, groupOrientacao, groupMapa;
    String exercicio, velocidade, orientacao, mapa;

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewAux = getView();

        salvar = binding.btnSalvar;
        caminhada = viewAux.findViewById(R.id.caminhada);
        corrida = viewAux.findViewById(R.id.corrida);
        pedalada = viewAux.findViewById(R.id.pedalada);
        km = viewAux.findViewById(R.id.km_h);
        ms = viewAux.findViewById(R.id.m_s);
        north = viewAux.findViewById(R.id.north_up);
        course = viewAux.findViewById(R.id.course_up);
        vetorial = viewAux.findViewById(R.id.t_vetorial);
        satelite = viewAux.findViewById(R.id.t_satelite);
        groupExercicio = viewAux.findViewById(R.id.exercicioGroup);
        groupVelocidade = viewAux.findViewById(R.id.veloGroup);
        groupOrientacao = viewAux.findViewById(R.id.orientGroup);
        groupMapa = viewAux.findViewById(R.id.mapaGroup);

        VerificaExercicio();
        VerificaVelocidade();
        VerificaOrientacao();
        VerificaMapa();

        binding.btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getActivity().getSharedPreferences("chaveGeral", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                Exercicio(v);
                Velocidade(v);
                Orientacao(v);
                Mapa(v);

                editor.putString("exercicio", exercicio);
                editor.putString("velocidade", velocidade);
                editor.putString("orientacao", orientacao);
                editor.putString("mapa", mapa);
                editor.commit();
            }
        });
    }

    private void Exercicio(View v){
        if(binding.caminhada.isChecked()){
            exercicio = "caminhada";
            Toast.makeText(getContext(), "Salvo com Sucesso", Toast.LENGTH_SHORT).show();
        }if(binding.corrida.isChecked()){
            exercicio = "corrida";
            Toast.makeText(getContext(), "Salvo com Sucesso", Toast.LENGTH_SHORT).show();
        }if (binding.pedalada.isChecked()){
            exercicio = "pedalada";
            Toast.makeText(getContext(), "Salvo com Sucesso", Toast.LENGTH_SHORT).show();
        }else{
            Snackbar.make(v,"Selecione o tipo de Exercício", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void Velocidade(View v){
        if(binding.kmH.isChecked()){
            velocidade = "km";
            Toast.makeText(getContext(), "Salvo com Sucesso", Toast.LENGTH_SHORT).show();
        }if (binding.mS.isChecked()){
            velocidade = "ms";
            Toast.makeText(getContext(), "Salvo com Sucesso", Toast.LENGTH_SHORT).show();
        }else{
            Snackbar.make(v,"Selecione o tipo de Medida de Velocidade", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void Orientacao(View v) {
        if(binding.northUp.isChecked()){
            orientacao = "north";
            Toast.makeText(getContext(), "Salvo com Sucesso", Toast.LENGTH_SHORT).show();
        }if (binding.courseUp.isChecked()){
            orientacao = "course";
            Toast.makeText(getContext(), "Salvo com Sucesso", Toast.LENGTH_SHORT).show();
        }else{
            Snackbar.make(v,"Selecione o tipo de Orientação", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void Mapa(View v){

        if(binding.tSatelite.isChecked()){
            mapa = "satelite";
            Toast.makeText(getContext(), "Salvo com Sucesso", Toast.LENGTH_SHORT).show();
        }if (binding.tVetorial.isChecked()){
            mapa = "vetorial";
            Toast.makeText(getContext(), "Salvo com Sucesso", Toast.LENGTH_SHORT).show();
        }else{
            Snackbar.make(v,"Selecione o tipo de Mapa", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void VerificaExercicio(){
        SharedPreferences prefs = getActivity().getSharedPreferences("chaveGeral", MODE_PRIVATE);
        if (prefs.getString("exercicio", "").equals("caminhada")){
            binding.exercicioGroup.check(R.id.caminhada);
        }if(prefs.getString("exercicio", "").equals("pedalada")){
            binding.exercicioGroup.check(R.id.pedalada);
        }if(prefs.getString("exercicio", "").equals("corrida")){
            binding.exercicioGroup.check(R.id.corrida);
        }
    }

    private void VerificaVelocidade(){

        SharedPreferences prefs = getActivity().getSharedPreferences("chaveGeral", MODE_PRIVATE);
        if (prefs.getString("velocidade", "").equals("km")){
            binding.veloGroup.check(R.id.km_h);
        }if (prefs.getString("velocidade", "").equals("ms")){
            binding.veloGroup.check(R.id.m_s);
        }
    }

    private void VerificaOrientacao(){

        SharedPreferences prefs = getActivity().getSharedPreferences("chaveGeral", MODE_PRIVATE);
        if (prefs.getString("orientacao", "").equals("north")){
            binding.orientGroup.check(R.id.north_up);
        }if (prefs.getString("orientacao", "").equals("course")){
            binding.orientGroup.check(R.id.course_up);
        }
    }

    private void VerificaMapa(){

        SharedPreferences prefs = getActivity().getSharedPreferences("chaveGeral", MODE_PRIVATE);
        if (prefs.getString("mapa", "").equals("vetorial")){
            binding.mapaGroup.check(R.id.t_vetorial);
        }if (prefs.getString("orientacao", "").equals("satelite")){
            binding.mapaGroup.check(R.id.t_satelite);
        }
    }
}