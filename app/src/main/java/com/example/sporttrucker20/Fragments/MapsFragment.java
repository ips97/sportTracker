package com.example.sporttrucker20.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.sporttrucker20.Coordenada;
import com.example.sporttrucker20.R;
import com.example.sporttrucker20.databinding.FragmentMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsFragment extends Fragment {

    private FragmentMapsBinding binding;

    // Atributos para o Fused Location (gps)
    private static final int REQUEST_LOCATION_UPDATES = 2;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    // Atributos p/ manipulação do mapa
    Marker userMarker;
    Location currentPosition, lastPosition;
    boolean firstFix = true, started = false, cron = false;
    double distanciaAcumulada, distanciaAcumuladaKm;
    long initialTime, currentTime, elapsedTime, pauseOffset;
    DecimalFormat df = null;
    private SharedPreferences prefs;
    private GoogleMap mMap;
    private UiSettings mUiSettings;

    private ImageView mPlay, mPause, mClear;
    private AppCompatButton btnSalvar;
    private Chronometer cronometro;
    private TextView inputDistance, inputSpeed;

    List<Coordenada> coords = new ArrayList<>();
    int count = 0;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(inflater, container, false);

        //Iniciar fragmento de mapa
        SupportMapFragment supportMapFragment = (SupportMapFragment)
                this.getChildFragmentManager().findFragmentById(R.id.google_map);

        // inicia processo de localização
        iniciaColetaLocalizacao();
        initialTime = System.currentTimeMillis();

        //Sincronizar mapa
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                mMap = googleMap;
                verficaMapaTipo();
                verficaMapaOrientacao();

                //Introduzindo bússula no mapa
                UiSettings mapUi = mMap.getUiSettings();
                mapUi.setCompassEnabled(true);

                //Mapa for clicado
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
                        //Clique no mapa
                        //Iniciar opções de marcação
                        MarkerOptions markerOptions = new MarkerOptions();
                        //Indicar posição marcada
                        markerOptions.position(latLng);
                        //Indicar texto da marcação
                        markerOptions.title(latLng.latitude +" : "+ latLng.longitude);
                        //Remover todas as marcações
                        mMap.clear();
                        //Animando o zoom
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                latLng, 10
                        ));
                        //Adicionar marcarção no mapa
                        mMap.addMarker(markerOptions);
                    }
                });
            }
        });
        prefs = getActivity().getSharedPreferences("chaveGeral", MODE_PRIVATE);


        cronometro = binding.chronometer;
        btnSalvar = binding.btnSalvar;
        mPlay = binding.btnPlay;
        mPause = binding.btnPause;
        mClear = binding.btnClear;
        inputSpeed = binding.inputSpeed;
        inputDistance = binding.inputDistance;
        limparBtn();
        startBtn();
        pauseBtn();
        saveHistoric();

        //Retornar view
        return binding.getRoot();
        //return root;
    }

    // funcao p/ iniciar a coleta de localização
    private void iniciaColetaLocalizacao() {

        // Se a app já possui a permissão, ativa a camada de localização
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED)  {
            // A permissão foi dada
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
            mLocationRequest = LocationRequest.create();

            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(5*1000);
            mLocationRequest.setFastestInterval(1*1000);

            // Programa o evento a ser chamado em intervalo regulares de tempo
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location location = locationResult.getLastLocation();

                    atualizaPosicaoNoMapa(location);
                }
            };

            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);

        } else {
            // Solicite a permissão
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_UPDATES);
        }
    }

    // funcao p/ solicitar a permissão da verificação de Localização
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode == REQUEST_LOCATION_UPDATES) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // O usuário acabou de dar a permissão
                iniciaColetaLocalizacao();
            }
            else {
                // O usuário não deu a permissão solicitada
                Toast.makeText(getContext(),"Sem permissão para mostrar atualizações da sua localização", Toast.LENGTH_SHORT).show();

            }
        }
    }

    // funcao para atualizar a localização no mapa
    public void atualizaPosicaoNoMapa(Location location) {
        verficaMapaOrientacao();
        currentTime = System.currentTimeMillis();
        elapsedTime = currentTime - initialTime;

        if(firstFix){
            firstFix = false;
            currentPosition = lastPosition = location;
            distanciaAcumulada = 0;
        }else{
            lastPosition = currentPosition;
            currentPosition = location;
            distanciaAcumulada+=currentPosition.distanceTo(lastPosition);
        }

        setDistanciaTempoEVelocidade();
        if (count == 2){
            Coordenada dto = new Coordenada();
            dto.setLng(location.getLongitude());
            dto.setLat(location.getLatitude());
            coords.add(dto);
            count=0;
        }else{
            count++;
        }

        if(prefs.getString("orientacao","").equals("course")){
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition(ll, 18, 0, location.getBearing())));
        }

        LatLng userPosition = new LatLng(location.getLatitude(), location.getLongitude());

        if(mMap != null){
            if (userMarker == null) {
                userMarker = mMap.addMarker(new MarkerOptions().position(userPosition));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userPosition,17f));
            }else{
                userMarker.setPosition(userPosition);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userPosition));
            }
        }

    }

    // funcao para mostrar o tempo e a distancia de movimentação
    // quando se dá o play
  private void setDistanciaTempoEVelocidade(){

        if(prefs.getString("velocidade","").equals("ms") && distanciaAcumulada > 0 && started == true){

            distanciaAcumuladaKm = distanciaAcumulada / 1000;
            cron = true;
            df =  new DecimalFormat("0.00");
            df.setRoundingMode(RoundingMode.HALF_UP);
            double time = distanciaAcumulada / ((SystemClock.elapsedRealtime() - cronometro.getBase()) /1000);
            binding.inputSpeed.setText(df.format(time) + " m/s");
            binding.inputDistance.setText(df.format(distanciaAcumuladaKm) + " km");
        }
        if(prefs.getString("velocidade","").equals("km") && distanciaAcumulada> 0 && started == true){

            distanciaAcumuladaKm = distanciaAcumulada / 1000;
            cron = true;
            df =  new DecimalFormat("0.00");
            DecimalFormat sp =  new DecimalFormat("0.0");
            df.setRoundingMode(RoundingMode.HALF_UP);
            double time = distanciaAcumulada / ((SystemClock.elapsedRealtime() - cronometro.getBase()) /1000);
            time = time * 3.6;
            binding.inputSpeed.setText(sp.format(time) + " km/h");
            binding.inputDistance.setText(df.format(distanciaAcumuladaKm) + " km");
        }
    }

    // funcao p/ mostrar o monitoramento de tempo/velocidade/distancia
   private void startBtn(){
        mPlay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                cronometro.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                cronometro.start();
                Toast.makeText(getContext(), "Started", Toast.LENGTH_SHORT).show();
                mPause.setEnabled(true);
                btnSalvar.setEnabled(false);
                mPlay.setEnabled(false);
                mClear.setEnabled(false);
                started = true;
            }
        });
    }

    // funcao p/ limpar o monitoramento de tempo/velocidade/distancia
    private void limparBtn(){

        mClear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                binding.inputDistance.setText("");
                distanciaAcumulada = 0;
                distanciaAcumuladaKm = 0;
                cronometro.getText();
                binding.inputSpeed.setText("");
                cronometro.setBase(SystemClock.elapsedRealtime());
                pauseOffset = 0;
                started = false;

            }
        });
    }

    // funcao p/ dar pause no monitoramento de tempo/velocidade/distancia
    private void pauseBtn(){

        mPause.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                cronometro.stop();
                pauseOffset = SystemClock.elapsedRealtime() - cronometro.getBase();
                Toast.makeText(getContext(), "Paused", Toast.LENGTH_SHORT).show();
                btnSalvar.setEnabled(true);
                mPlay.setEnabled(true);
                mClear.setEnabled(true);
                started = false;
            }
        });
    }

    private void saveHistoric(){
        binding.btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, Object> exercicio = new HashMap<>();
                exercicio.put("distancia", binding.inputDistance.getText().toString());
                exercicio.put("tempo", cronometro.getText().toString());
                exercicio.put("velocidade", binding.inputSpeed.getText().toString());
                exercicio.put("tipoExercicio", prefs.getString("exercicio",""));
                exercicio.put("orientacaoMapa", prefs.getString("orientacao", ""));
                exercicio.put("coordenadas", coords);

                firestore.collection("historico_Tracker")
                        .add(exercicio)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                               Toast.makeText(getContext(), "Salvo", Toast.LENGTH_SHORT).show();
                               String salvoDTO = documentReference.getId();
                               deleteTodosAntesDeSalvar(salvoDTO);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Houve um erro", Toast.LENGTH_SHORT).show();
                                System.out.println("Erro ao salvar: "+ e.getMessage());
                            }
                        });

                binding.inputDistance.setText("");
                distanciaAcumulada = 0;
                distanciaAcumuladaKm = 0;
                cronometro.getText();
                binding.inputSpeed.setText("");
                cronometro.setBase(SystemClock.elapsedRealtime());
                pauseOffset = 0;
                started = false;
                binding.btnClear.setEnabled(false);

            }
        });
    }

    private void deleteTodosAntesDeSalvar(String salvoDTO){
        firestore.collection("historicTracker").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                if (!document.getId().equalsIgnoreCase(salvoDTO)){
                                    firestore.collection("historicTracker")
                                            .document(document.getId())
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    System.out.println("Excluído");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    System.out.println("Erro ao Excluir");
                                                }
                                            });
                                }
                            }
                        }else{
                            Toast.makeText(getContext(), "Erro ao salvar!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    // método para verificar tipo de mapa
    private void verficaMapaTipo(){
        mUiSettings = mMap.getUiSettings();

        if(prefs.getString("mapa","").equals("vetorial")){
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }
        if(prefs.getString("mapa","").equals("satelite")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        if(prefs.getString("mapa","").equals("")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }
    }

    // metodo p/ verificar orientacao do mapa
    private void verficaMapaOrientacao(){
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setCompassEnabled(false);


        if(prefs.getString("orientacao","").equals("north")){
            mUiSettings.setCompassEnabled(false);
            mUiSettings.setRotateGesturesEnabled(false);
        }
        if(prefs.getString("orientacao","").equals("course")){
            mUiSettings.setCompassEnabled(false);
            mUiSettings.setRotateGesturesEnabled(false);
        }
        if(prefs.getString("orientacao","").equals("")){
            mUiSettings.setCompassEnabled(true);
            mUiSettings.setRotateGesturesEnabled(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mFusedLocationProviderClient!=null)
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }
}
