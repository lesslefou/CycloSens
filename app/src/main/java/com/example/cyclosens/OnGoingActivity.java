package com.example.cyclosens;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


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
    private ArrayList<Position> positionList;
    private BluetoothDevice cardiacDevice, pedalDevice;
    private BluetoothGatt bluetoothCardiacGatt = null;
    private BluetoothGatt bluetoothPedalGatt = null;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private int nbOfBat = 0;
    private int bpmValue = 0;
    private int nbOfStrengthCalculation = 0;
    private int strengthValue = 0;
    private int nbOfSpeedCalculation = 0;
    private int speedValue = 0;

    private int speedAv = 0;
    private int strengthAv = 0;
    private int bpmAv = 0;

    private int cptBpm = 0;
    private int cptStrength = 0;


    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnGoingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ghost = getIntent().getExtras().getBoolean("ghost");
        cardiacDevice = getIntent().getParcelableExtra("cardiac");
        pedalDevice = getIntent().getParcelableExtra("pedal");

        if (ghost) {
            binding.temps.setVisibility(View.VISIBLE);
        }

        positionList = new ArrayList<>();

        locationListener = this::updateLocation;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                50,
                locationListener
        );

        connectToCardiacDevice();
        connectToPedalDevice();

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
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("US"));
            String strDate = dateFormat.format(date);

            if (nbOfBat == 0) {
                bpmAv = 0;
            } else {
                bpmAv = bpmValue / nbOfBat;
            }
            if (nbOfStrengthCalculation == 0) {
                strengthAv = 0;
            } else {
                strengthAv = strengthValue / nbOfStrengthCalculation;
            }
            if (nbOfSpeedCalculation == 0) {
                speedAv = 0;
            } else {
                speedAv = speedValue / nbOfSpeedCalculation;
            }

            String key = createActivity(duration, strDate, positionList); //SAVE THE DATA ON THE DATABASE

            if (nbOfBat == 0) {
                nbOfBat = 1;
            }
            if (nbOfSpeedCalculation == 0) {
                nbOfSpeedCalculation = 1;
            }
            if (nbOfStrengthCalculation == 0) {
                nbOfStrengthCalculation = 1;
            }
            Activity activityOnGoing = new Activity(key, getString(R.string.activity), strDate, duration, bpmAv, strengthAv, speedAv, retrievedTotalDistance(positionList), positionList); //CHANGER STRENGH

            Log.i(TAG, "deconnection du device");
            bluetoothCardiacGatt.disconnect();
            bluetoothPedalGatt.disconnect();
            Intent i = new Intent(OnGoingActivity.this, ActivityInformation.class);
            i.putExtra("activity", activityOnGoing);
            startActivity(i);
            finish();
        });
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateLocation(Location location) {
        positionList.add(new Position(location.getLatitude(), location.getLongitude()));
        float speedActual = location.getSpeedAccuracyMetersPerSecond();
        runOnUiThread(() -> binding.vitesse.setText(speedActual + getString(R.string.speedUnity)));

        speedValue += speedActual;
        nbOfSpeedCalculation++;
    }

    private static final UUID CARDIAC_SERVICE_UUID = UUID.fromString("00001234-cc7a-482a-984a-7f2ed5b3e58f");
    private static final UUID CARDIAC_CHARACTERISTIC_NOTIFY_UUID = UUID.fromString("0000dead-8e22-4541-9d4c-21edae82ed19");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902);

    private static UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    //Connect to the bpm device
    private void connectToCardiacDevice() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        bluetoothCardiacGatt = cardiacDevice.connectGatt(this, true, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                connectionStateChange(gatt, status, newState);

                //Gérer la déconnexion du device au millieu de l'activité
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                // Check if the service discovery succeeded. If not disconnect
                serviceDiscovered(gatt, status, CARDIAC_SERVICE_UUID, CARDIAC_CHARACTERISTIC_NOTIFY_UUID);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt pGatt, BluetoothGattCharacteristic pCharacteristic) {
                Log.i(TAG, "onCharacteristicChangedCardiac: " + Arrays.toString(pCharacteristic.getValue())); //look like an address
                byte[] data = pCharacteristic.getValue(); //Tableau de byte contenant les données mis dans la charactéristic
                characteristicChanged(data, "cardiac");
            }
        });
        bluetoothCardiacGatt.connect();
    }


    private static final UUID PEDAL_SERVICE_UUID = UUID.fromString("00005678-cc7a-482a-984a-7f2ed5b3e58f");
    private static final UUID PEDAL_CHARACTERISTIC_NOTIFY_UUID =  UUID.fromString("0000beef-8e22-4541-9d4c-21edae82ed19");

    //Connect to the pedal device
    private void connectToPedalDevice() {
        bluetoothPedalGatt = pedalDevice.connectGatt(this, true, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                connectionStateChange(gatt,status,newState);
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                // Check if the service discovery succeeded. If not disconnect
                serviceDiscovered(gatt,status,PEDAL_SERVICE_UUID,PEDAL_CHARACTERISTIC_NOTIFY_UUID);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt pGatt, BluetoothGattCharacteristic pCharacteristic) {
                Log.i(TAG, "onCharacteristicChangedPedal: " + Arrays.toString(pCharacteristic.getValue()));
                byte[] data = pCharacteristic.getValue(); //Byte array with the data from the characteristic
                characteristicChanged(data,"pedal");
            }
        });
        bluetoothPedalGatt.connect();
    }

    @SuppressLint("SetTextI18n")
    //When we receive a new value on our phone
    private void characteristicChanged(byte[] data, String device) {
        if (data != null && data.length > 0) {
            //Convert the data to have access to his value
            int actualValue = 0;
            for (byte datum : data) {
                actualValue = actualValue << 8;
                actualValue = actualValue | (datum & 0xFF);
            }

            int finalActualValue = actualValue;
            if (device.equals("pedal")) {
                runOnUiThread(() -> binding.puissanceMoyenne.setText(getString(R.string.strength) + finalActualValue));

                //If the value is lower than the old one
                if (actualValue < strengthValue) { cptStrength ++; }

                strengthValue += actualValue;
                nbOfStrengthCalculation ++;
            } else {
                runOnUiThread(() -> binding.bpm.setText(finalActualValue + getString(R.string.cardiacUnity)));

                //If the value is lower than the old one
                if (actualValue < bpmValue) { cptBpm ++; }

                bpmValue += actualValue;
                nbOfBat ++;
            }
        }

        //If the cyclist asked for the cheers "extension"
        if (ghost) {
            //If it's lower 5 times in row, then print the motivation
            if (cptStrength == 5 || cptBpm == 5) {
                runOnUiThread(() -> binding.temps.setText(getString(R.string.negEncouragement)));
                cptBpm = 0;
                cptStrength = 0;
            } else {
                //Otherwise, print the cheers
                runOnUiThread(() -> binding.temps.setText(getString(R.string.posEncouragement)));
            }
        }

        //Si on veut ecrire quelque chose en retour dans la charac
        /*pCharacteristic.setValue(lToSend);
        pGatt.writeCharacteristic(pCharacteristic);*/
    }

    private void connectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if(status == GATT_SUCCESS) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + gatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // We successfully disconnected on our own request
                Log.i(TAG, "Disconnected from GATT server.");
                gatt.close();
            }
        } else {
            // An error happened...figure out what happened!
            gatt.close();
        }
    }

    private void serviceDiscovered(BluetoothGatt gatt, int status, UUID ServiceUuid, UUID CharacteristicNotifyUuid) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.w(TAG, "BluetoothGatt.GATT_SUCCESS");
            final List<BluetoothGattService> lBLEservices = gatt.getServices();
            for(BluetoothGattService test: lBLEservices){
                Log.i(TAG, "UUID: " + test.getUuid());
            }
            Log.i(TAG, "Découverte de " + lBLEservices.size() + " Caracs.");
            BluetoothGattService lService = gatt.getService(ServiceUuid);
            if(lService != null){
                BluetoothGattCharacteristic lCharacteristic = lService.getCharacteristic(CharacteristicNotifyUuid);
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
            gatt.disconnect();
        }
    }

    @Override
    //Launch the map and get the device location
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        updateLocationUI();
        getDeviceLocation();
    }

    @SuppressLint("MissingPermission") //Already checked
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
    @SuppressLint("MissingPermission") //Already checked
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


    protected void onPause() {
        super.onPause();
        if(locationListener!=null) {
            if (locationManager == null) {
                locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            }
            locationManager.removeUpdates(locationListener);
            locationManager=null;
            locationListener=null;
        }
    }

    //create an activity on the database
    private String createActivity(long duration, String date, ArrayList<Position> positionList) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            String key = updateActivities(duration, date, positionList, userId);
            updateActivitiesResume(userId);
            return key;

        }
        return getString(R.string.error);
    }

    //Update the activity results and stock them
    private void updateActivitiesResume(String userId) {
        final int[] cpt = {0};
        final int[] totalDistance = {0};
        final int[] avDistance = {0};
        final int[] avBPM = {0};
        final int[] avStrength = {0};
        final int[] avSpeed = {0};
        final int[] nbActivities = {0};

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("ResumeActivities");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {

                if (cpt[0] == 0) {
                    if (data.exists()) {
                        nbActivities[0] = Integer.parseInt(Objects.requireNonNull(data.child("nbActivities").getValue()).toString());
                        Log.i(TAG, "nb activity " + nbActivities[0]);
                        totalDistance[0] = Integer.parseInt(Objects.requireNonNull(data.child("totalDistance").getValue()).toString());
                        avDistance[0] = Integer.parseInt(Objects.requireNonNull(data.child("avDistance").getValue()).toString());
                        avBPM[0] = Integer.parseInt(Objects.requireNonNull(data.child("avBPM").getValue()).toString());
                        avStrength[0] = Integer.parseInt(Objects.requireNonNull(data.child("avStrength").getValue()).toString());
                        avSpeed[0] = Integer.parseInt(Objects.requireNonNull(data.child("avSpeed").getValue()).toString());
                    }

                    int totalDistanceActivity = retrievedTotalDistance(positionList);
                    Map<String, Object> activitiesResume = new HashMap<>();
                    Log.i(TAG, "nb activityy " + nbActivities[0]);
                    activitiesResume.put("nbActivities",nbActivities[0] + 1);
                    activitiesResume.put("totalDistance",totalDistance[0] + totalDistanceActivity);
                    activitiesResume.put("avDistance", (avDistance[0] + totalDistanceActivity)/(nbActivities[0] + 1));
                    if (avBPM[0] == 0) { activitiesResume.put("avBPM", bpmAv); } else { activitiesResume.put("avBPM",(avBPM[0] + bpmAv)/2); }
                    if (avStrength[0] == 0) { activitiesResume.put("avStrength", strengthAv); } else { activitiesResume.put("avStrength", (avStrength[0] + strengthAv)/2); }
                    if (avSpeed[0] == 0) { activitiesResume.put("avSpeed", speedAv); } else { activitiesResume.put("avSpeed", (avSpeed[0] + speedAv)/2); }
                    mRef.updateChildren(activitiesResume);

                    cpt[0]++;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {  }
        });
    }

    //Update the activity on screen
    private String updateActivities(long duration, String date, ArrayList<Position> positionList, String userId) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("Activities");
        String key = mRef.push().getKey();
        mRef = mRef.child(key);

        Map<String, Object> activity = new HashMap<>();
        activity.put("name",getString(R.string.activity));
        activity.put("date", date);
        activity.put("duration", duration);
        activity.put("bpmAv", bpmAv);
        activity.put("strengthAv", strengthAv);
        activity.put("speedAv", speedAv);
        activity.put("distance", retrievedTotalDistance(positionList));
        activity.put("positionList", positionList);
        mRef.updateChildren(activity);
        return key;
    }

    //Get the total distance with all tracks
    private int retrievedTotalDistance(ArrayList<Position> positionList) {
        int totalDistance = 0;
        for (int i=1; i<positionList.size(); i++) {
            totalDistance += getDistanceBetweenTwoLocation(i);
        }

        return totalDistance;
    }

    private float getDistanceBetweenTwoLocation(int i) {
        Location locationA = new Location("point A");
        locationA.setLatitude(positionList.get(i).getLat());
        locationA.setLongitude(positionList.get(i).getLng());

        Location locationB = new Location("point B");
        locationB.setLatitude(positionList.get(i-1).getLat());
        locationB.setLongitude(positionList.get(i-1).getLng());

        return locationA.distanceTo(locationB);
    }


}

