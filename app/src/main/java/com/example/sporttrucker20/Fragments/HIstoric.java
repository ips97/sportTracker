    package com.example.sporttrucker20.Fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sporttrucker20.R;
import com.example.sporttrucker20.databinding.FragmentHIstoricBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

    public class HIstoric extends Fragment {

    private FragmentHIstoricBinding binding;
    private GoogleMap mMap;
    private List<HashMap<String, Double>> tra;
    String speedUnit, speed, mapOrientation, mapType, exerciseType, distance, totalTime;

       //Firebase
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHIstoricBinding.inflate(inflater, container, false);

        //Iniciar fragmento de mapa
        SupportMapFragment supportMapFragment = (SupportMapFragment)
                this.getChildFragmentManager().findFragmentById(R.id.google_map);



        //Sincronizar mapa
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                mMap = googleMap;
                consultaUltimoRegistro();
            }
        });

        //Retornar view
        return binding.getRoot();

    }



    private void consultaUltimoRegistro(){

            firestore.collection("historico_Tracker")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Map<String, Object> dados = document.getData();
                                    System.out.println("DADOS >>>>>>> " + dados);
                                    setFieldsInformationsSaved(dados);
                                }
                            } else {
                                Toast.makeText(getContext(), "Erro ao salvar", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

    }

        private void setFieldsInformationsSaved(Map<String, Object> dadosMap) {

            exerciseType = (String) dadosMap.get("tipoExercicio");
            speedUnit = (String) dadosMap.get("unidadeVelocidade");
            mapOrientation = (String) dadosMap.get("orientacaoMapa");
            mapType = (String) dadosMap.get("tipoMapa");
            speed = (String) dadosMap.get("velocidade");
            distance = (String) dadosMap.get("distancia");
            totalTime = (String) dadosMap.get("tempo");
           // Double calorias = (Double) dadosMap.get("calorias");
            tra = (List<HashMap<String, Double>>) dadosMap.get("coordenadas");

            List<LatLng> listLat = new ArrayList<>();

            for(int i = 0; i < tra.size(); i++){
                LatLng ll = new LatLng(tra.get(i).get("lat"), tra.get(i).get("lon"));
                listLat.add(ll);
            }

            System.out.println("LATLONG >>>>>>> " + tra.toString());

            String distanceType = speedUnit.equalsIgnoreCase("km/h") ? " km" : " m";

            binding.inputDistanceHistoric.setText(distance  + distanceType);
            binding.chronometerHistoric.setText(totalTime);
            binding.inputSpeedHistoric.setText(speed + " " + speedUnit);
            //binding.inputDate_historic.setText(String.valueOf(calorias));


            mMap.addPolyline(new PolylineOptions().addAll(listLat));

            double latmin = tra.get(0).get("lat");
            double latmax = tra.get(0).get("lat");
            double lgnmin = tra.get(0).get("lon");
            double lgnmax = tra.get(0).get("lon");

            for(int i = 1; i < tra.size(); i++ ){
                double lat, lon;
                lat = tra.get(i).get("lat");
                lon = tra.get(i).get("lon");

                latmin = latmin < lat ? latmin : lat;
                latmax = latmax > lat ? latmax : lat;
                lgnmin = lgnmin < lon ? lgnmin : lat;
                lgnmax = lgnmax > lon ? lgnmax : lat;
            }

            LatLng southWest = new LatLng(latmin, lgnmin);
            LatLng northEast = new LatLng(latmax, lgnmax);

            LatLngBounds bound = new LatLngBounds(southWest, northEast);

            //gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bound, 50));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(tra.get(0).get("lat"), tra.get(tra.size() - 1).get("lon")), 12));


        }

}