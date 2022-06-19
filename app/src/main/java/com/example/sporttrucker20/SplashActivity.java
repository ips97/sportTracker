package com.example.sporttrucker20;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(getMainLooper()).postDelayed(()->{
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            //startActivity(new Intent(this, MainActivity.class));
        }, 3000);
    }
}