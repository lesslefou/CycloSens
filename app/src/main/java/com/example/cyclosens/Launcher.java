package com.example.cyclosens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.cyclosens.databinding.ActivityLauncherBinding;

public class Launcher extends AppCompatActivity {
    private ActivityLauncherBinding binding;
    private boolean ghost,gps,cardiac,pedal;

    private boolean locationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLauncherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ghostSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> ghost = isChecked);
        binding.gpsSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> gps = isChecked);
        binding.cardiacSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> cardiac = isChecked);
        binding.pedalSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> pedal = isChecked);

        binding.btnLaunch.setOnClickListener(view -> {
            if (gps && cardiac && pedal) {
                getLocationPermission();
                //CHECK BLUETOOTH
                checkIfOnGoingPossible();
            } else {
                Toast.makeText(Launcher.this,R.string.toastLauncher, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void checkIfOnGoingPossible() {
        if (locationPermissionGranted) { //AJOUTER LES CONNECTIONS BLUETOOTH
            Intent i = new Intent(Launcher.this, OnGoingActivity.class);
            i.putExtra("ghost",ghost);
            startActivity(i);
        } else {
            Toast.makeText(Launcher.this,"PROBLEME APPARAILLAGE", Toast.LENGTH_SHORT).show();
        }
    }

}