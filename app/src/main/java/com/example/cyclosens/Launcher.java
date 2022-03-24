package com.example.cyclosens;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class Launcher extends AppCompatActivity {
    private Switch ghostSwitch, gpsSwitch, cardiacSwitch, pedalSwitch;
    private boolean ghost,gps,cardiac,pedal;
    private Button btnLaunch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        ghostSwitch = findViewById(R.id.ghostSwitch);
        gpsSwitch = findViewById(R.id.gpsSwitch);
        cardiacSwitch = findViewById(R.id.cardiacSwitch);
        pedalSwitch = findViewById(R.id.pedalSwitch);
        btnLaunch = findViewById(R.id.btnLaunch);

        ghostSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                ghost = isChecked;
            }
        });
        gpsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                gps = isChecked;
            }
        });
        cardiacSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                cardiac = isChecked;
            }
        });
        pedalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                pedal = isChecked;
            }
        });

        btnLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gps && cardiac && pedal) {
                    Intent i = new Intent(Launcher.this, Sign_Up.class);
                    i.putExtra("ghost",true);
                    startActivity(i);
                }
                else {
                    Toast.makeText(Launcher.this,R.string.toastLauncher, Toast.LENGTH_SHORT);
                }
            }
        });
    }
}