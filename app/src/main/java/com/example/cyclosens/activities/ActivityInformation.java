package com.example.cyclosens.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
        binding.duration.setText("" + getString(R.string.duration) + " " + activity.getDuration() + "m");
        binding.bpmAv.setText("" + getString(R.string.bpmAv) + " " + activity.getBpmAv());
        binding.strenghAv.setText("" + getString(R.string.strengthAv) + " " + activity.getStrengthAv());
        binding.speedAv.setText("" + getString(R.string.speedAv) + " " + activity.getSpeedAv() + "m/s");
        binding.totalDistance.setText("" + getString(R.string.distanceTotal) + " " + activity.getDistance() + "m");
        binding.settings.setOnClickListener(view -> updateNameActivity());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        binding.backBtn.setOnClickListener(view -> finish());
        binding.btnDelete.setOnClickListener(view -> deleteActivity());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        ArrayList<Position> locationDouble = activity.getPositionList();
        ArrayList<LatLng> location = new ArrayList<>();

        PolylineOptions line = new PolylineOptions();
        for (int i = 0; i < locationDouble.size(); i++) {
            location.add(new LatLng(locationDouble.get(i).getLat(), locationDouble.get(i).getLng()));

            googleMap.addPolyline(line.add( //Add to line
                    location.get(i)). //Actual array list element
                            width(5) //specify the width of poly line
                    .color(Color.GREEN) //add color to our poly line.
                    .geodesic(true)); //make our poly line geodesic
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location.get(i), 14));
        }

        //Add a marker at the beginning of the track
        googleMap.addMarker(new MarkerOptions().position(location.get(0)).title("Start"));
        //Catch the size of the locations array
        int size = location.size();
        //Add a marker at the end of the track
        googleMap.addMarker(new MarkerOptions().position(location.get(size-1)).title("End"));
    }


    //Delete the activity in case the user want to
    private void deleteActivity() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            updateActivitiesResume(userId);
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("Activities");
            Log.i(TAG, activity.getKey());
            mRef.child(activity.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mRef.child(activity.getKey()).removeValue();
                    onBackPressed();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


    //Update the activity when one is delete
    private void updateActivitiesResume(String userId) {
        final int[] cpt = {0};
        final int[] totalDistance = {0};
        final int[] avDistance = {0};
        final int[] avBPM = {0};
        final int[] avStrength = {0};
        final int[] avSpeed = {0};
        final int[] nbActivities = {0};

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("ResumeActivities");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                if (cpt[0] == 0 ) {
                    if (data.exists()) {
                        nbActivities[0] = Integer.parseInt(Objects.requireNonNull(data.child("nbActivities").getValue()).toString());
                        totalDistance[0] = Integer.parseInt(Objects.requireNonNull(data.child("totalDistance").getValue()).toString());
                        avDistance[0] = Integer.parseInt(Objects.requireNonNull(data.child("avDistance").getValue()).toString());
                        avBPM[0] = Integer.parseInt(Objects.requireNonNull(data.child("avBPM").getValue()).toString());
                        avStrength[0] = Integer.parseInt(Objects.requireNonNull(data.child("avStrength").getValue()).toString());
                        avSpeed[0] = Integer.parseInt(Objects.requireNonNull(data.child("avSpeed").getValue()).toString());
                    }


                    int totalDistanceActivity = retrievedTotalDistance(activity.getPositionList());
                    Map<String, Object> activitiesResume = new HashMap<>();
                    activitiesResume.put("nbActivities",nbActivities[0] - 1);
                    activitiesResume.put("totalDistance",totalDistance[0] - totalDistanceActivity);
                    activitiesResume.put("avDistance", (avDistance[0] * (nbActivities[0])) - totalDistanceActivity);
                    if (nbActivities[0] == 1) { activitiesResume.put("avBPM", avBPM[0] - activity.getBpmAv()); } else { activitiesResume.put("avBPM",(avBPM[0]*2) - activity.getBpmAv()); }
                    if (nbActivities[0] == 1) { activitiesResume.put("avStrength", avStrength[0] - activity.getStrengthAv()); } else { activitiesResume.put("avStrength", (avStrength[0]*2) - activity.getStrengthAv()); }
                    if (nbActivities[0] == 1) { activitiesResume.put("avSpeed", avSpeed[0] - activity.getSpeedAv()); } else { activitiesResume.put("avSpeed", (avSpeed[0]*2) - activity.getSpeedAv()); }
                    mRef.updateChildren(activitiesResume);

                    cpt[0]++;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {  }
        });
    }

    //Change the value of the total distance
    private int retrievedTotalDistance(ArrayList<Position> positionList) {
        int totalDistance = 0;
        for (int i=1; i<positionList.size(); i++) {
            totalDistance += getDistanceBetweenTwoLocation(i);
        }

        return totalDistance;
    }

    //Measure the distance between two points
    private float getDistanceBetweenTwoLocation(int i) {
        Location locationA = new Location("point A");
        locationA.setLatitude(activity.getPositionList().get(i).getLat());
        locationA.setLongitude(activity.getPositionList().get(i).getLng());

        Location locationB = new Location("point B");
        locationB.setLatitude(activity.getPositionList().get(i-1).getLat());
        locationB.setLongitude(activity.getPositionList().get(i-1).getLng());

        return locationA.distanceTo(locationB);
    }

    //Change the activity name
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

    //Change the datas in the data base
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
            activityUpdate.put("strengthAv", activity.getStrengthAv());
            activityUpdate.put("speedAv", activity.getSpeedAv());
            activityUpdate.put("distance", activity.getDistance());
            activityUpdate.put("positionList", activity.getPositionList());
            mRef.updateChildren(activityUpdate);
        }
    }

    //Change to the previous page
    @Override
    public void onBackPressed() {
        finish();
        Intent i = new Intent(ActivityInformation.this, Welcome.class);
        i.putExtra("fragment", "activities");
        startActivity(i);
    }

}