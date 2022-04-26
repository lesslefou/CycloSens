package com.example.cyclosens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.cyclosens.classes.Activity;
import com.example.cyclosens.activities.ActivityInformation;
import com.example.cyclosens.databinding.ActivityOnGoingBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OnGoingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = OnGoingActivity.class.getSimpleName();
    private static final String KEY_LOCATION = "location";
    private ActivityOnGoingBinding binding;

    private boolean ghost;
    private GoogleMap map;
    private static final int DEFAULT_ZOOM = 18;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private Date timeBegin, timeEnd;

    private BluetoothDevice cardiacDevice, pedalDevice;
    private BluetoothGatt bluetoothGatt = null;

    @SuppressLint("MissingPermission") //PERMISSION CHECKER EN AMONT
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnGoingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ghost = getIntent().getExtras().getBoolean("ghost");
        cardiacDevice = getIntent().getParcelableExtra("cardiac");
        //pedalDevice = getIntent().getParcelableExtra("pedal");

        connectToDevice();

        timeBegin = Calendar.getInstance().getTime();

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        binding.stop.setOnClickListener(view -> {
            timeEnd = Calendar.getInstance().getTime();
            long duration = (timeEnd.getTime() - timeBegin.getTime()) / (60 * 1000) % 60; //minute

            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String strDate = dateFormat.format(date);

            String key = createActivity(duration, strDate); //SAVE THE DATA ON THE DATABASE

            //GERER GESTION ERROR GetUID User

            Activity activityOnGoing = new Activity(key, getString(R.string.activity),strDate,duration,100,50); //CHANGER BPM ET STRENGH

            Log.i(TAG, "deconnection du device");
            bluetoothGatt.disconnect();
            Intent i = new Intent(OnGoingActivity.this, ActivityInformation.class);
            i.putExtra("activity",activityOnGoing);
            startActivity(i);
            finish();
        });

    }

    private void connectToDevice() {
        bluetoothGatt = cardiacDevice.connectGatt(this, true, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                //Gérer la déconnexion du device au millieu de l'activité
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
            }
        });
        bluetoothGatt.connect();
    }

    private String createActivity(long duration, String date) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("Activities");
            String key = mRef.push().getKey();
            mRef = mRef.child(key);

            Map<String, Object> activity = new HashMap<>();
            activity.put("name",getString(R.string.activity));
            activity.put("date", date);
            activity.put("duration", duration);
            activity.put("bpmAv", 100); //METTRE A JOUR
            activity.put("strenghAv", 50); //METTRE A JOUR
            mRef.updateChildren(activity);

            return key;

        }
        return getString(R.string.error);
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