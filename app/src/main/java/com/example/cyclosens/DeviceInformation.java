package com.example.cyclosens;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.cyclosens.classes.Activity;
import com.example.cyclosens.databinding.ActivityDeviceInformationBinding;
import com.example.cyclosens.databinding.ActivityDevicesBinding;

public class DeviceInformation extends AppCompatActivity {
    private ActivityDeviceInformationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeviceInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String device = getIntent().getExtras().getString("device");

        binding.deviceTitle.setText(device);



    }
}