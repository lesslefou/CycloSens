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

import com.example.cyclosens.databinding.ActivityLauncherBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Constants;

@RequiresApi(api = Build.VERSION_CODES.M)
public class Launcher extends AppCompatActivity {
    private ActivityLauncherBinding binding;
    private boolean ghost,gps,cardiac,pedal;
    private String cardiacAddress, pedalAddress;
    private BluetoothDevice cardiacDevice, pedalDevice;
    private boolean beltFound, pedalFound;
    private BluetoothAdapter bluetoothAdapter;
    private boolean locationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;


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
                    bluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
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
        if (locationPermissionGranted && checkIfBleSupported() && beltFound ) {
            if (beltFound /* && pedalFound*/ ) {
                bluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                Intent i = new Intent(Launcher.this, OnGoingActivity.class);
                i.putExtra("ghost",ghost);
                i.putExtra("cardiac", cardiacDevice);
                //i.putExtra("pedal", pedalDevice);
                startActivity(i);
                finish();
            }
            else {
                Toast.makeText(Launcher.this,"One or more sensors not found", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(Launcher.this,"Pairing probleme", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Check if the phone support the ble
      * @return
     */
    private boolean checkIfBleSupported() {
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
                return true;
            }
            return true;
        }
        return false;
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result.getDevice().getAddress().equals(cardiacAddress)) {
                cardiacDevice = result.getDevice();
                beltFound = true;
            } else if (result.getDevice().getAddress().equals(pedalAddress)) {
                pedalDevice = result.getDevice();
                pedalFound = true;
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