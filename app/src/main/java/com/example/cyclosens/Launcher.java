package com.example.cyclosens;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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

import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.M)
public class Launcher extends AppCompatActivity {
    private static final String TAG = Launcher.class.getSimpleName(); //POUR LES LOG
    private boolean ghost,gps,cardiac,pedal;
    private String cardiacAddress, pedalAddress;
    private BluetoothDevice cardiacDevice, pedalDevice;
    private boolean beltFound, pedalFound;
    private BluetoothAdapter bluetoothAdapter = null;
    private boolean locationPermissionGranted = false, blePermissionGranted = false;
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.cyclosens.databinding.ActivityLauncherBinding binding = ActivityLauncherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        beltFound = false;
        pedalFound = false;

        binding.ghostSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> ghost = isChecked);
        binding.gpsSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> gps = isChecked);
        binding.cardiacSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> cardiac = isChecked);
        binding.pedalSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> pedal = isChecked);

        String[] PERMISSIONS = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN
        };

        //Check if the user has already paired his 2 devices
        if (retrieveDevicesNameSaved()) {
            binding.btnLaunch.setOnClickListener(view -> {
                if (gps && cardiac && pedal) {
                    if (!hasPermission(PERMISSIONS)) {
                        ActivityCompat.requestPermissions(Launcher.this, PERMISSIONS, 1);
                    } else {
                        locationPermissionGranted = true;
                        blePermissionGranted = true;
                        askBluetoothPermission();
                        checkIfOnGoingPossible();
                    }
                } else {
                    Toast.makeText(Launcher.this,R.string.toastLauncher, Toast.LENGTH_SHORT).show();
                }
            });
        }

        binding.backBtn.setOnClickListener(view -> finish());
    }


    private boolean hasPermission(String... PERMISSIONS) {
        if (getApplicationContext() != null && PERMISSIONS != null) {
            for (String permission: PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    return  false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "ACCESS_FINE_LOCATION Permission is granted", Toast.LENGTH_SHORT).show();
                locationPermissionGranted = true;
            } else {
                Toast.makeText(this, "ACCESS_FINE_LOCATION Permission is denied", Toast.LENGTH_SHORT).show();
            }

            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "BLUETOOTH_SCAN Permission is granted", Toast.LENGTH_SHORT).show();
                blePermissionGranted= true;
            } else {
                Toast.makeText(this, "BLUETOOTH_SCAN Permission is denied", Toast.LENGTH_SHORT).show();
            }

            if (locationPermissionGranted && blePermissionGranted) {
                askBluetoothPermission();
                checkIfOnGoingPossible();
            }
        }
    }

    private void checkIfOnGoingPossible() {
        if (locationPermissionGranted && blePermissionGranted) {
            //5 secondes waits for searching devices before connexion
            // Delay in seconds
            int secs = 5;
            Utils.delay(secs, () -> {
                if (beltFound && pedalFound) {
                    Log.i(TAG, "devices found");
                    if (bluetoothAdapter.isEnabled()) {
                        Intent i = new Intent(Launcher.this, OnGoingActivity.class);
                        i.putExtra("ghost",ghost);
                        i.putExtra("cardiac", cardiacDevice);
                        i.putExtra("pedal", pedalDevice);
                        startActivity(i);
                        finish();
                    }else {
                        Log.i(TAG, "bluetoothAdapter not enabled");
                    }
                }
                else {
                    bluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                    Toast.makeText(Launcher.this, getString(R.string.sensorsNotFound), Toast.LENGTH_SHORT).show();
                }

            });
        } else {
            bluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
            Toast.makeText(Launcher.this, R.string.pairingProblem, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), getString(R.string.bleNotSupported), Toast.LENGTH_SHORT).show();
            return false;
        }
        else
        {
            if (!bluetoothAdapter.isEnabled())
            {
                Toast.makeText(getApplicationContext(), getString(R.string.bleNotSupported), Toast.LENGTH_SHORT).show();
                Intent activeBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(activeBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH);
            }
            else
            {
                Toast.makeText(getApplicationContext(), getString(R.string.bleEnabled), Toast.LENGTH_SHORT).show();
                blePermissionGranted = true;
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
            } else if (result.getDevice().getAddress().equals(pedalAddress)) {
                pedalDevice = result.getDevice();
                Log.i(TAG, "pedal device found");
                pedalFound = true;
            }
            if (pedalFound && beltFound) {
                bluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
            }
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
                        cardiacAddress = Objects.requireNonNull(snapshot.child("cardiac").child("address").getValue()).toString();
                    } else {
                        Toast.makeText(Launcher.this, getString(R.string.pairingBeltNecessary), Toast.LENGTH_SHORT).show();
                        cpt[0] += 1;
                    }
                    if (snapshot.child("pedal").child("address").exists()) {
                        pedalAddress = Objects.requireNonNull(snapshot.child("pedal").child("address").getValue()).toString();
                    } else {
                        Toast.makeText(Launcher.this, getString(R.string.pairingPedalNecessary), Toast.LENGTH_SHORT).show();
                        cpt[0] += 1;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        return cpt[0] <= 0;
    }
}