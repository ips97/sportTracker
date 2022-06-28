package com.example.sporttrucker20.Fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sporttrucker20.R;
import com.example.sporttrucker20.databinding.FragmentHistoricBinding;
import com.example.sporttrucker20.databinding.FragmentMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class historic extends Fragment {

    FragmentHistoricBinding binding;
    GoogleMap mMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoricBinding.inflate(inflater, container, false);

        SupportMapFragment supportMapFragment = (SupportMapFragment)
                this.getChildFragmentManager().findFragmentById(R.id.map);


        //Sincronizar mapa
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                mMap = googleMap;
            }
        });


        //Retornar view
        return binding.getRoot();
    }
}