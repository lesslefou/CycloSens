package com.example.cyclosens;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.cyclosens.databinding.ActivityDevicesBinding;

public class Devices extends AppCompatActivity {
    private ActivityDevicesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDevicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}