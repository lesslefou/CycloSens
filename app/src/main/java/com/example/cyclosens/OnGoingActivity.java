package com.example.cyclosens;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
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
    private float speedValue = 0.0F;

    @SuppressLint("MissingPermission") //PERMISSION CHECKER EN AMONT
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

        positionList.add(new Position(43.117030,5.932195));
        positionList.add(new Position(43.118030,5.933195));
        positionList.add(new Position(43.119030,5.934195));

        locationListener = location -> updateLocation(location);
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
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String strDate = dateFormat.format(date);

            String key = createActivity(duration, strDate, positionList); //SAVE THE DATA ON THE DATABASE

            //GERER GESTION ERROR GetUID User

            if (nbOfBat == 0) { nbOfBat = 1; }
            if (nbOfSpeedCalculation == 0 ) { nbOfSpeedCalculation = 1; }
            if (nbOfStrengthCalculation == 0) { nbOfStrengthCalculation = 1; }
            Activity activityOnGoing = new Activity(key, getString(R.string.activity),strDate,duration,bpmValue/nbOfBat,strengthValue/nbOfStrengthCalculation,speedValue/nbOfSpeedCalculation,retrievedTotalDistance(positionList),positionList); //CHANGER STRENGH

            Log.i(TAG, "deconnection du device");
            bluetoothCardiacGatt.disconnect();
            bluetoothPedalGatt.disconnect();
            Intent i = new Intent(OnGoingActivity.this, ActivityInformation.class);
            i.putExtra("activity",activityOnGoing);
            startActivity(i);
            finish();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateLocation(Location location) {
        positionList.add(new Position(location.getLatitude(), location.getLongitude()));
        float speedActual = location.getSpeedAccuracyMetersPerSecond();
        Log.i(TAG, "vitesse : " + speedActual);
        runOnUiThread(() -> binding.vitesse.setText(speedActual + " m/s"));

        speedValue += speedActual;
        nbOfSpeedCalculation ++;
    }


    private static final UUID CARDIAC_SERVICE_UUID = UUID.fromString("00001234-cc7a-482a-984a-7f2ed5b3e58f");
    private static final UUID CARDIAC_CHARACTERISTIC_NOTIFY_UUID =  UUID.fromString("0000dead-8e22-4541-9d4c-21edae82ed19");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902);

    private static UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    private void connectToCardiacDevice() {
        bluetoothCardiacGatt = cardiacDevice.connectGatt(this, true, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                connectionStateChange(gatt,status,newState);

                //Gérer la déconnexion du device au millieu de l'activité
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                // Check if the service discovery succeeded. If not disconnect
                serviceDiscovered(gatt,status,CARDIAC_SERVICE_UUID,CARDIAC_CHARACTERISTIC_NOTIFY_UUID);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt pGatt, BluetoothGattCharacteristic pCharacteristic) {
                Log.i(TAG, "onCharacteristicChangedCardiac: " + Arrays.toString(pCharacteristic.getValue())); //look like an address
                byte[] data = pCharacteristic.getValue(); //Tableau de byte contenant les données mis dans la charactéristic
                characteristicChanged(data,"cardiac");
            }
        });
        bluetoothCardiacGatt.connect();
    }


    private static final UUID PEDAL_SERVICE_UUID = UUID.fromString("00005678-cc7a-482a-984a-7f2ed5b3e58f");
    private static final UUID PEDAL_CHARACTERISTIC_NOTIFY_UUID =  UUID.fromString("0000beef-8e22-4541-9d4c-21edae82ed19");

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
                Log.i(TAG, "onCharacteristicChangedPedal: " + Arrays.toString(pCharacteristic.getValue())); //look like an address
                byte[] data = pCharacteristic.getValue(); //Tableau de byte contenant les données mis dans la charactéristic
                characteristicChanged(data,"pedal");
            }
        });
        bluetoothPedalGatt.connect();
    }

    private void characteristicChanged(byte[] data, String device) {
        if (data != null && data.length > 0) {
            //Convert the data to have access to his value
            /*final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));

            Log.i(TAG, "stringbuilder" + stringBuilder);*/

            //int actualValue = Integer.parseInt(stringBuilder.toString().trim());

            int actualValue = 0;
            for (int i = 0; i < data.length; i++) {
                actualValue=actualValue<<8;
                actualValue=actualValue|(data[i] & 0xFF);
            }

            int finalActualValue = actualValue;
            if (device.equals("pedal")) {
                runOnUiThread(() -> binding.puissanceMoyenne.setText("Puissance :" + finalActualValue));
                strengthValue += actualValue;
                nbOfStrengthCalculation ++;
            } else {
                runOnUiThread(() -> binding.bpm.setText(finalActualValue + " bpm"));
                bpmValue += actualValue;
                nbOfBat ++;
            }
        }

        //Si on veut ecrire quelque chose en retour dans la charac
        //pCharacteristic.setValue(lToSend);
        //pGatt.writeCharacteristic(pCharacteristic);
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
            gatt.disconnect(); //lequel mettre ???
            //bluetoothCardiacGatt.disconnect();
            //bluetoothPedalGatt.disconnect();
        }
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

    private void updateActivitiesResume(String userId) {
        final int[] cpt = {0};
        final Float[] totalDistance = new Float[1];
        final Float[] avDistance = new Float[1];
        final int[] avBPM = new int[1];
        final int[] avStrength = new int[1];
        final int[] nbActivities = new int[1];

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("ResumeActivities");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                if (cpt[0] == 0 ) {
                    nbActivities[0] = Integer.parseInt(data.child("nbActivities").getValue().toString());
                    totalDistance[0] = Float.parseFloat(data.child("totalDistance").getValue().toString());
                    avDistance[0] = Float.parseFloat(data.child("avDistance").getValue().toString());
                    avBPM[0] = Integer.parseInt(data.child("avBPM").getValue().toString());
                    avStrength[0] = Integer.parseInt(data.child("avStrength").getValue().toString());

                    cpt[0]++;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {  }
        });

        Float totalDistanceActivity = retrievedTotalDistance(positionList);
        Map<String, Object> activitiesResume = new HashMap<>();
        activitiesResume.put("nbActivities",nbActivities[0] ++);
        activitiesResume.put("totalDistance",totalDistance[0] + totalDistanceActivity);
        activitiesResume.put("avDistance", (avDistance[0] + totalDistanceActivity)/(nbActivities[0] + 1));
        activitiesResume.put("avBPM",(avBPM[0] + bpmValue/nbOfBat)/2);
        activitiesResume.put("avStrength", (avStrength[0] + strengthValue/nbOfStrengthCalculation)/2);
        mRef.updateChildren(activitiesResume);
    }

    private String updateActivities(long duration, String date, ArrayList<Position> positionList, String userId) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("Activities");
        String key = mRef.push().getKey();
        mRef = mRef.child(key);

        Map<String, Object> activity = new HashMap<>();
        activity.put("name",getString(R.string.activity));
        activity.put("date", date);
        activity.put("duration", duration);
        activity.put("bpmAv", bpmValue/nbOfBat);
        activity.put("strengthAv", strengthValue/nbOfStrengthCalculation);
        activity.put("speedAv", speedValue/nbOfSpeedCalculation);
        activity.put("distance", retrievedTotalDistance(positionList));
        activity.put("positionList", positionList);
        mRef.updateChildren(activity);
        return key;
    }

    private float retrievedTotalDistance(ArrayList<Position> positionList) {
        float totalDistance = 0.0F;
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

