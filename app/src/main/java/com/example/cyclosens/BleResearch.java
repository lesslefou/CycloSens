package com.example.cyclosens;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.Intent;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.cyclosens.activities.ActivitiesAdapter;
import com.example.cyclosens.databinding.ActivityBleResearchBinding;
import com.google.firebase.components.Lazy;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.M)
public class BleResearch extends AppCompatActivity {
    private static final String TAG = BleResearch.class.getSimpleName(); //POUR LES LOG
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;
    private ActivityBleResearchBinding binding;
    private RecyclerView monRecycler;
    private BleResearchAdapter bleResearchAdapter;
    private ArrayList<BluetoothDevice> bleDevices;
    private BluetoothAdapter bluetoothAdapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBleResearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String device = getIntent().getExtras().getString("device");
        bleDevices = new ArrayList<BluetoothDevice>();

        monRecycler = binding.recycleViewBle;
        bleResearchAdapter = new BleResearchAdapter(bleDevices, device, this);
        monRecycler.setAdapter(bleResearchAdapter);
        monRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        if(bleResearchAdapter != null){
            Log.i(TAG, "bleResearchAdapter not null");
            askBluetoothPermission();
        }

    }

    private void startLEBle(){
        if (bluetoothAdapter.isEnabled()){
            Log.i(TAG, "bluetoothAdapter enabled");
            bluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
            list();
        } else {
            Log.i(TAG, "bluetoothAdapter not enabled");
        }
    }
    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            int index = bleDevices.indexOf(result.getDevice());
            Log.i("index", String.valueOf(index));
            if (index != -1) {
                bleDevices.set(index, result.getDevice());
            } else {
                bleDevices.add(result.getDevice());
            }
            Log.i("ble",result.getDevice().toString());
            bleResearchAdapter.notifyDataSetChanged();
        }
    };

    private void list() {

        Set<BluetoothDevice> bleBonded = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice bt : bleBonded) {
            Log.i("ble list",bt.toString());
            bleDevices.add(bt);
        }
        bleResearchAdapter.notifyDataSetChanged();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE_ENABLE_BLUETOOTH)
            return;
        if (resultCode == RESULT_OK)
        {
            Toast.makeText(getApplicationContext(), "Bluetooth activé", Toast.LENGTH_SHORT).show();
            startLEBle();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Bluetooth non activé !", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Demande acceptation des permissions
     */
    private void askBluetoothPermission() {
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null)
        {
            Toast.makeText(getApplicationContext(), "Bluetooth non supporté !", Toast.LENGTH_SHORT).show();
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
                startLEBle();
            }
        }

    }

}