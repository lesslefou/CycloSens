package com.example.cyclosens;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.Intent;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.cyclosens.activities.ActivitiesAdapter;
import com.example.cyclosens.databinding.ActivityBleResearchBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.components.Lazy;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.M)
public class BleResearch extends AppCompatActivity {
    private static final String TAG = BleResearch.class.getSimpleName(); //POUR LES LOG
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;
    private BleResearchAdapter bleResearchAdapter;
    private ArrayList<BluetoothDevice> bleDevices;
    private BluetoothAdapter bluetoothAdapter = null;
    private String device;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.cyclosens.databinding.ActivityBleResearchBinding binding = ActivityBleResearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        device = getIntent().getExtras().getString("device");
        bleDevices = new ArrayList<BluetoothDevice>();

        RecyclerView monRecycler = binding.recycleViewBle;
        bleResearchAdapter = new BleResearchAdapter(bleDevices);
        monRecycler.setAdapter(bleResearchAdapter);
        monRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        String[] PERMISSIONS = new String[]{
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        if(bleResearchAdapter != null){
            if (!hasPermission(PERMISSIONS)) {
                ActivityCompat.requestPermissions(BleResearch.this, PERMISSIONS, 1);
            } else {
                //Ask for BLE permission
                askBluetoothPermission();
                Log.i(TAG, "bleResearchAdapter not null");

                //Set the ble device to the database in order to connect later
                bleResearchAdapter.setOnItemClickListener(bluetoothDevice -> {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser != null) {
                        String userId = firebaseUser.getUid();
                        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("bleDevices").child(device);
                        mRef.child("name").setValue(bluetoothDevice.getName());
                        mRef.child("address").setValue(bluetoothDevice.getAddress());
                        Log.d("adapter", mRef.toString());

                        bluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                        Intent i = new Intent(BleResearch.this, Devices.class);
                        startActivity(i);
                        finish();
                    }
                });
            }
        }



        binding.btnBack.setOnClickListener(v-> finish());
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
        boolean connect=false, scan=false, location=false;

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                connect=true;
                //Toast.makeText(this, "BLUETOOTH_CONNECT Permission is granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "BLUETOOTH_CONNECT Permission is denied", Toast.LENGTH_SHORT).show();
            }

            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                scan=true;
                //Toast.makeText(this, "BLUETOOTH_SCAN Permission is granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "BLUETOOTH_SCAN Permission is denied", Toast.LENGTH_SHORT).show();
            }

            if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                location=true;
                //Toast.makeText(this, "ACCESS_FINE_LOCATION Permission is granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "ACCESS_FINE_LOCATION Permission is denied", Toast.LENGTH_SHORT).show();
            }

            if (connect && scan && location) {
                askBluetoothPermission();
                bleResearchAdapter.setOnItemClickListener(bluetoothDevice -> {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser != null) {
                        String userId = firebaseUser.getUid();
                        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("bleDevices").child(device);
                        mRef.child("name").setValue(bluetoothDevice.getName());
                        mRef.child("address").setValue(bluetoothDevice.getAddress());
                        Log.d("adapter", mRef.toString());

                        bluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                        Intent i = new Intent(BleResearch.this, Devices.class);
                        startActivity(i);
                        finish();
                    }
                });
            }
        }
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            int index = bleDevices.indexOf(result.getDevice());
            if (index != -1) {
                bleDevices.set(index, result.getDevice());
            } else {
                Log.i("ble",result.getDevice().toString());
                bleDevices.add(result.getDevice());
            }
            bleResearchAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i(TAG, "error");
        }
    };

    //List of devices
    private void list() {
        Set<BluetoothDevice> bleBonded = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice bt : bleBonded) {
            Log.i("ble list",bt.toString());
            bleDevices.add(bt);
        }
        bleResearchAdapter.notifyDataSetChanged();

    }

    /**
     * BLE acceptation
     */
    private void askBluetoothPermission() {
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null)
        {
            Toast.makeText(getApplicationContext(), getString(R.string.bleNotSupported), Toast.LENGTH_SHORT).show();
        }
        else
        {
            if (!bluetoothAdapter.isEnabled())
            {
                Toast.makeText(getApplicationContext(), getString(R.string.bleDisabled), Toast.LENGTH_SHORT).show();
                Intent activeBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(activeBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH);
            }
            else
            {
                Toast.makeText(getApplicationContext(), getString(R.string.bleEnabled), Toast.LENGTH_SHORT).show();
                startLEBle();
            }
        }

    }

    /**
     * Start BLE research
     */
    private void startLEBle() {
        if (bluetoothAdapter.isEnabled()) {
            Log.i(TAG, getString(R.string.bleNotSupported));
            bluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
            list();
        } else {
            Log.i(TAG, getString(R.string.bleEnabled));
        }
    }

}