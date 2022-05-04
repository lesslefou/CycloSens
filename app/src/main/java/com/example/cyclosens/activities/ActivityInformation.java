package com.example.cyclosens.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cyclosens.Log_In;
import com.example.cyclosens.MainActivity;
import com.example.cyclosens.R;
import com.example.cyclosens.Welcome;
import com.example.cyclosens.classes.Activity;
import com.example.cyclosens.classes.Position;
import com.example.cyclosens.databinding.ActivityInformationBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.firebase.database.ValueEventListener;

public class ActivityInformation extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = ActivityInformation.class.getSimpleName(); //POUR LES LOG
    private Activity activity;
    private ActivityInformationBinding binding;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         binding = ActivityInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activity = (Activity) getIntent().getExtras().getSerializable("activity");

        binding.name.setText(activity.getNameActivity());
        binding.date.setText(activity.getDateActivity());
        binding.duration.setText("" + getString(R.string.duration) + " " + activity.getDuration());
        binding.bpmAv.setText("" + getString(R.string.bpmAv) + " " + activity.getBpmAv());
        binding.strenghAv.setText("" + getString(R.string.strenghAv) + " " + activity.getStrenghAv());
        binding.speedAv.setText("" + getString(R.string.speedAv) + " " + activity.getSpeedAv());
        binding.settings.setOnClickListener(view -> updateNameActivity());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        binding.btnDelete.setOnClickListener(view -> deleteActivity());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        ArrayList<Position> locationDouble = activity.getPositionList();
        ArrayList<LatLng> location = new ArrayList<>();

        PolylineOptions line = new PolylineOptions();
        for (int i = 0; i < locationDouble.size(); i++) {
            location.add(new LatLng(locationDouble.get(i).getLat(), locationDouble.get(i).getLng()));

            googleMap.addPolyline(line.add( //On rajoute a notre ligne
                    location.get(i)). //L'element actuel de l'array list
                            width(5) //specify the width of poly line
                    .color(Color.GREEN) //add color to our poly line.
                    .geodesic(true)); //make our poly line geodesic
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location.get(i), 14));
        }

        //On rajoute un marqueur au début du track
        googleMap.addMarker(new MarkerOptions().position(location.get(0)).title("Start"));
        //On recupere la taille totale de l'array liste
        int size = location.size();
        //On rajoute un marqueur à la fin du track
        googleMap.addMarker(new MarkerOptions().position(location.get(size-1)).title("End"));
    }

    private void deleteActivity() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("Activities");
            Log.i(TAG, activity.getKey());
            mRef.child(activity.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mRef.child(activity.getKey()).removeValue();
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    private void updateNameActivity() {
        runOnUiThread(new Thread(() -> {
            EditText edittTxt = new EditText(ActivityInformation.this);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams (
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(16,0,16,0);
            edittTxt.setLayoutParams(params);

            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityInformation.this,R.style.MyDialogTheme);
            builder.setTitle(R.string.changeName)
                    .setView(edittTxt)
                    .setPositiveButton(R.string.validate, (dialogInterface, i) -> {
                        activity.setNameActivity(edittTxt.getText().toString());
                        //MODIFIER AFFICHAGE
                        databaseUpdate();
                        runOnUiThread(() -> binding.name.setText(activity.getNameActivity()));
                    })
                    .setNegativeButton(R.string.annulation, (dialogInterface, i) -> {
                       dialogInterface.cancel();
                    })
                    .create()
                    .show();
        }));
    }

    private void databaseUpdate() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("Activities").child(activity.getKey());

            Map<String, Object> activityUpdate = new HashMap<>();
            activityUpdate.put("name",activity.getNameActivity());
            activityUpdate.put("date", activity.getDateActivity());
            activityUpdate.put("duration", activity.getDuration());
            activityUpdate.put("bpmAv", activity.getBpmAv());
            activityUpdate.put("strenghAv", activity.getStrenghAv());
            activityUpdate.put("speedAv", activity.getSpeedAv());
            activityUpdate.put("positionList", activity.getPositionList());
            mRef.updateChildren(activityUpdate);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent i = new Intent(ActivityInformation.this, Welcome.class);
        i.putExtra("fragment", "activities");
        startActivity(i);
    }

}