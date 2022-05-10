package com.example.cyclosens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.cyclosens.databinding.ActivityDevicesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Devices extends AppCompatActivity {
    private ActivityDevicesBinding binding;
    private String cardiacDevice;
    private String pedalDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDevicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cardiacDevice = "";
        pedalDevice = "";

        binding.addCardiacDevice.setOnClickListener(v-> goSearchDevice("cardiac"));
        binding.addPedalDevice.setOnClickListener(v-> goSearchDevice("pedal"));

        //Get the device from the database
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("bleDevices");
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child("cardiac").child("name").exists()) {
                        cardiacDevice = Objects.requireNonNull(snapshot.child("cardiac").child("name").getValue()).toString();
                        Log.i("device", cardiacDevice);
                        binding.cardiacDeviceName.setText(cardiacDevice);
                    }
                    if (snapshot.child("pedal").child("name").exists()) {
                        pedalDevice = Objects.requireNonNull(snapshot.child("pedal").child("name").getValue()).toString();
                        binding.pedalDeviceName.setText(pedalDevice);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        //Go back on the previous page and close this activity
        binding.btnBack.setOnClickListener((View.OnClickListener) v -> finish());
    }

    private void goSearchDevice(String type){
        Intent i = new Intent(Devices.this, BleResearch.class);
        i.putExtra("device",type);
        startActivity(i);
    }

}