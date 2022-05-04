package com.example.cyclosens;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.example.cyclosens.classes.Position;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OnGoingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = OnGoingActivity.class.getSimpleName();
    private static final String KEY_LOCATION = "location";
    private static final int GATT_INTERNAL_ERROR = 129;
    private ActivityOnGoingBinding binding;

    private boolean ghost;
    private GoogleMap map;
    private static final int DEFAULT_ZOOM = 18;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private Date timeBegin, timeEnd;
    private ArrayList<Position> positionList;
    private LocationListener locationListener;
    private BluetoothDevice cardiacDevice, pedalDevice;
    private BluetoothGatt bluetoothGatt = null;

    private int nbOfBat = 0;
    private int bpmValue = 0;

    @SuppressLint("MissingPermission") //PERMISSION CHECKER EN AMONT
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnGoingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ghost = getIntent().getExtras().getBoolean("ghost");
        cardiacDevice = getIntent().getParcelableExtra("cardiac");
        //pedalDevice = getIntent().getParcelableExtra("pedal");

        positionList = new ArrayList<>();

        positionList.add(new Position(43.117030,5.932195));
        positionList.add(new Position(43.118030,5.933195));
        positionList.add(new Position(43.119030,5.934195));

        locationListener = location -> updateLocation(location);

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

            String key = createActivity(duration, strDate, positionList); //SAVE THE DATA ON THE DATABASE

            //GERER GESTION ERROR GetUID User
            Activity activityOnGoing = new Activity(key, getString(R.string.activity),strDate,duration,bpmValue/nbOfBat,50,positionList); //CHANGER STRENGH

            Log.i(TAG, "deconnection du device");
            bluetoothGatt.disconnect();
            Intent i = new Intent(OnGoingActivity.this, ActivityInformation.class);
            i.putExtra("activity",activityOnGoing);
            startActivity(i);
            finish();
        });
    }

    private void updateLocation(Location location) {
        positionList.add(new Position(location.getLatitude(), location.getLongitude()));
    }


    private static final UUID SERVICE_UUID = UUID.fromString("00001234-cc7a-482a-984a-7f2ed5b3e58f");
    private static final UUID CHARACTERISTIC_NOTIFY_UUID =  UUID.fromString("0000dead-8e22-4541-9d4c-21edae82ed19");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902);

    private static UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    private void connectToDevice() {
        bluetoothGatt = cardiacDevice.connectGatt(this, true, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if(status == GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i(TAG, "Connected to GATT server.");
                        // Attempts to discover services after successful connection.
                        Log.i(TAG, "Attempting to start service discovery:" +
                        gatt.discoverServices());
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        // We successfully disconnected on our own request
                        Log.i(TAG, "Disconnected from GATT server.");
                        gatt.close();
                    } else {
                        // We're CONNECTING or DISCONNECTING, ignore for now
                    }
                } else {
                    // An error happened...figure out what happened!
                    gatt.close();
                }
                //Gérer la déconnexion du device au millieu de l'activité
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                // Check if the service discovery succeeded. If not disconnect
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.w(TAG, "BluetoothGatt.GATT_SUCCESS");
                    final List<BluetoothGattService> lBLEservices = gatt.getServices();
                    for(BluetoothGattService test: lBLEservices){
                        Log.i(TAG, "UUID: " + test.getUuid());
                    }
                    Log.i(TAG, "Découverte de " + lBLEservices.size() + " Caracs.");
                    BluetoothGattService lService = gatt.getService(SERVICE_UUID);
                    if(lService != null){
                        BluetoothGattCharacteristic lCharacteristic = lService.getCharacteristic(CHARACTERISTIC_NOTIFY_UUID);
                        if(lCharacteristic != null){
                            gatt.setCharacteristicNotification(lCharacteristic, true);
                            BluetoothGattDescriptor lDescriptor = lCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
                            lDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(lDescriptor);
                        }
                        else{
                            Log.i(TAG, "Characteristic specified not found");
                        }
                    }
                    else{
                        Log.i(TAG, "Service specified not found");
                    }
                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                    bluetoothGatt.disconnect();
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt pGatt, BluetoothGattCharacteristic pCharacteristic) {
                Log.i(TAG, "onCharacteristicChanged: " + Arrays.toString(pCharacteristic.getValue())); //look like an address
                byte[] data = pCharacteristic.getValue(); //Tableau de byte contenant les données mis dans la charactéristic

                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    for(byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));

                    int actualBpm = Integer.parseInt(stringBuilder.toString().trim());
                    bpmValue += actualBpm;
                    Log.i(TAG, "bpmValue : " + bpmValue);
                    nbOfBat ++;

                    runOnUiThread(() -> binding.bpm.setText(actualBpm + " bpm"));

                    Log.i(TAG, stringBuilder.toString()); //Reel Value
                }

                //Faire un traitement sur les données reçues

                //Si on veut ecrire quelque chose en retour dans la charac
                //pCharacteristic.setValue(lToSend);
                //pGatt.writeCharacteristic(pCharacteristic);
            }
        });

        bluetoothGatt.connect();
    }

    private String createActivity(long duration, String date, ArrayList<Position> positionList) {
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
            activity.put("bpmAv", bpmValue/nbOfBat);
            activity.put("strenghAv", 50); //METTRE A JOUR
            activity.put("positionList", positionList);
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