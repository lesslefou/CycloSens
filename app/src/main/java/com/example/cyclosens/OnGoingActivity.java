package com.example.cyclosens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.example.cyclosens.activities.Activity;
import com.example.cyclosens.activities.ActivityInformation;
import com.example.cyclosens.databinding.ActivityOnGoingBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class OnGoingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = OnGoingActivity.class.getSimpleName();
    private static final String KEY_LOCATION = "location";
    private ActivityOnGoingBinding binding;

    private boolean ghost;
    private GoogleMap map;
    private static final int DEFAULT_ZOOM = 18;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;

    @SuppressLint("MissingPermission") //PERMISSION CHECKER EN AMONT
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnGoingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ghost = getIntent().getExtras().getBoolean("ghost");

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        binding.stop.setOnClickListener(view -> {
            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            String strDate = dateFormat.format(date);
            Activity activityOnGoing = new Activity(getString(R.string.activity),strDate); //METTRE PLUTOT LA DUREE QUE L'HEURE
            //SAVE THE DATA ON THE DATABASE
            Intent i = new Intent(OnGoingActivity.this, ActivityInformation.class);
            i.putExtra("activity",activityOnGoing);
            startActivity(i);
            finish();
        });
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        updateLocationUI();
        getDeviceLocation();
    }

    @SuppressLint("MissingPermission") //PERMISSION CHECKER EN AMONT
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     * which may be null in rare cases when a location is not available
     */
    @SuppressLint("MissingPermission") //PERMISSION CHECKER EN AMONT
    private void getDeviceLocation() {
        Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Set the map's camera position to the current location of the device.
                lastKnownLocation = task.getResult();
                if (lastKnownLocation != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(lastKnownLocation.getLatitude(),
                                    lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                }
            } else {
                Log.d(TAG, "Current location is null. Using defaults.");
                map.getUiSettings().setMyLocationButtonEnabled(false);
            }
        });
    }
}