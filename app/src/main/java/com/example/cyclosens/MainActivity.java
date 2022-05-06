package com.example.cyclosens;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.cyclosens.databinding.ActivityMainBinding;

public class MainActivity extends Activity  {
    private static final String TAG = MainActivity.class.getSimpleName(); //POUR LES LOG

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());

        View view = binding.getRoot();
        setContentView(view);

        binding.btnSign.setOnClickListener(view1 -> {
            Intent sign = new Intent(MainActivity.this, Sign_Up.class);
            startActivity(sign);
        });
        binding.btnLog.setOnClickListener(view1 -> {
            Intent log = new Intent(MainActivity.this, Log_In.class);
            startActivity(log);
        });
    }

}