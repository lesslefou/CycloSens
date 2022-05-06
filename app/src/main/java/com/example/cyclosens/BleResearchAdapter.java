package com.example.cyclosens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cyclosens.activities.ActivitiesAdapter;
import com.example.cyclosens.activities.ActivityInformation;
import com.example.cyclosens.classes.Activity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BleResearchAdapter extends RecyclerView.Adapter<BleResearchAdapter.ViewHolder> {
    private static ClickListener clickListener;
    private final ArrayList<BluetoothDevice> bleDevices;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView uuid;
        private final ConstraintLayout itemLayout;

        public ViewHolder(View view) {
            super(view);
            this.name = (TextView) itemView.findViewById(R.id.name);
            this.uuid = (TextView) itemView.findViewById(R.id.uuid);
            this.itemLayout =  itemView.findViewById(R.id.itemView);

        }
    }

    public BleResearchAdapter(ArrayList<BluetoothDevice> bleDevices) {
        this.bleDevices = bleDevices;
    }

    @NonNull
    @Override
    public BleResearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_ble_research_adapter,parent,false);
        return new BleResearchAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BleResearchAdapter.ViewHolder holder, int position) {
        BluetoothDevice device = bleDevices.get(position);
        if(device.getName() == null) {
            holder.name.setText(R.string.deviceUnknown);
        } else {
            holder.name.setText(device.getName());
        }

        holder.uuid.setText(device.getAddress());

        holder.itemLayout.setOnClickListener(v-> clickListener.onItemClick(device));

    }

    @Override
    public int getItemCount() {
        return bleDevices.size();
    }

    public interface ClickListener {
        void onItemClick(BluetoothDevice bluetoothDevice);
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        BleResearchAdapter.clickListener = clickListener;
    }
}