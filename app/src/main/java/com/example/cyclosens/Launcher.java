package com.example.cyclosens;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.cyclosens.classes.Utils;
import com.example.cyclosens.databinding.ActivityLauncherBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

@RequiresApi(api = Build.VERSION_CODES.M)
public class Launcher extends AppCompatActivity {
    private static final String TAG = Launcher.class.getSimpleName(); //POUR LES LOG
    private ActivityLauncherBinding binding;
    private boolean ghost,gps,cardiac,pedal;
    private String cardiacAddress, pedalAddress;
    private BluetoothDevice cardiacDevice, pedalDevice;
    private boolean beltFound, pedalFound;
    private BluetoothAdapter bluetoothAdapter = null;
    private boolean locationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;
    private int secs = 5; // Delay in seconds


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLauncherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        beltFound = false;
        pedalFound = false;

        binding.ghostSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> ghost = isChecked);
        binding.gpsSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> gps = isChecked);
        binding.cardiacSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> cardiac = isChecked);
        binding.pedalSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> pedal = isChecked);


        //Check if the user has already paired his 2 devices
        if (retrieveDevicesNameSaved()) {
            binding.btnLaunch.setOnClickListener(view -> {
                if (gps && cardiac && pedal) {
                    getLocationPermission();
                    checkIfOnGoingPossible();
                } else {
                    Toast.makeText(Launcher.this,R.string.toastLauncher, Toast.LENGTH_SHORT).show();
                }
            });
        }
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
        if (locationPermissionGranted && askBluetoothPermission() ) {

            //5 secondes waits for searching devices before connexion
            Utils.delay(secs, () -> {
                if (beltFound /* && pedalFound*/) {
                    Log.i(TAG, "devices found");
                    if (bluetoothAdapter.isEnabled()) {
                        Intent i = new Intent(Launcher.this, OnGoingActivity.class);
                        i.putExtra("ghost",ghost);
                        i.putExtra("cardiac", cardiacDevice);
                        //i.putExtra("pedal", pedalDevice);
                        startActivity(i);
                        finish();
                    }else {
                        Log.i(TAG, "bluetoothAdapter not enabled");
                    }
                }
                else {
                    bluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                    Toast.makeText(Launcher.this,"One or more sensors not found", Toast.LENGTH_SHORT).show();
                }

            });
        } else {
            Toast.makeText(Launcher.this,"Pairing probleme", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Check if the phone support the ble
     * Ask the bluetooth permission
     * @return
     */
    private Boolean askBluetoothPermission() {
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null)
        {
            Toast.makeText(getApplicationContext(), "Bluetooth non supporté !", Toast.LENGTH_SHORT).show();
            return false;
        }
        else
        {
            if (!bluetoothAdapter.isEnabled())
            {
                Toast.makeText(getApplicationContext(), "Bluetooth non activé !", Toast.LENGTH_SHORT).show();
                Intent activeBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(activeBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH);
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Bluetooth activé", Toast.LENGTH_SHORT).show();
                bluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
            }
            return true;
        }

    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i(TAG, result.getDevice().getAddress());
            if (result.getDevice().getAddress().equals(cardiacAddress)) {
                Log.i(TAG, "cardiac device found");
                cardiacDevice = result.getDevice();
                beltFound = true;
                bluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback); //A SUPPRIMER
            } else if (result.getDevice().getAddress().equals(pedalAddress)) {
                pedalDevice = result.getDevice();
                pedalFound = true;
            }
            /*if (pedalFound && beltFound) {
                bluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
            }*/
        }
    };

    private boolean retrieveDevicesNameSaved () {
        final int[] cpt = {0};
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("bleDevices");

            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child("cardiac").child("address").exists()) {
                        cardiacAddress = snapshot.child("cardiac").child("address").getValue().toString();
                    } else {
                        Toast.makeText(Launcher.this, "You need to pair the cardiac belt before launching an activity", Toast.LENGTH_SHORT).show();
                        cpt[0] += 1;
                    }
                    /*if (snapshot.child("pedal").child("address").exists()) {
                        cardiacAddress = snapshot.child("pedal").child("address").getValue().toString();
                    } else {
                        Toast.makeText(Launcher.this, "You need to pair the pedal before launching an activity", Toast.LENGTH_SHORT).show();
                        cpt[0] += 1;
                    }*/
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        if (cpt[0] > 0) {
            return false;
        } else {
            return true;
        }
    }
}