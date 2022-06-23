package com.example.sporttrucker20.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
    boolean firstFix = true;
    double distanciaAcumulada;
    long initialTime, currentTime, elapsedTime;

    private GoogleMap mMap;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //Iniciar view
        //View view = inflater.inflate(R.layout.fragment_maps, container, false);

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
                //Mapa for carregado
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

        //Retornar view
        return root;
    }



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


    public void atualizaPosicaoNoMapa(Location location) {

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

        System.out.println("Distância percorrida (m): "+distanciaAcumulada);
        System.out.println("Tempo Transcorrido (s): "+elapsedTime/1000);
        LatLng userPosition = new LatLng(location.getLatitude(), location.getLongitude());

        if(mMap != null){
            if (userMarker == null) {
                userMarker = mMap.addMarker(new MarkerOptions().position(userPosition));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userPosition,15f));
            }else{
                userMarker.setPosition(userPosition);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userPosition));
            }
        }
    }



}