package com.example.cyclosens;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.cyclosens.classes.Position;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


@SuppressLint("SetTextI18n")
public class StartFragment extends Fragment {
    private static final String TAG = StartFragment.class.getSimpleName(); //For the logs
    private ArrayList<Position> positionsLastTrack;
    int cpt = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_start, container, false);

        positionsLastTrack = new ArrayList<>();

        //Set all the information of the user from the database on the screen
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            ProgressBar progressBar = v.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);

            DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("user").child(userId);
            mReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (cpt == 0) {

                        if (dataSnapshot.child("ResumeActivities").exists()) {
                            initializeActivitiesResume(dataSnapshot.child("ResumeActivities"), v);
                        }

                        if (dataSnapshot.child("Activities").exists()) {
                            for (DataSnapshot data : dataSnapshot.child("Activities").getChildren()) {
                                Log.i(TAG, "lastTrack : " + data);
                                initializeLastTrackResume(data, v);
                                break;
                            }
                        }
                        cpt++;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {  }
            });
        }

        addBorderToRelativeLayout(v);

        return v;
    }

    //Summary of all the activity
    private void initializeActivitiesResume(DataSnapshot data, View v) {
        Log.i(TAG,"initializeActivitiesResume");

        String nbActivity = Objects.requireNonNull(data.child("nbActivities").getValue()).toString();
        String totalDistance = Objects.requireNonNull(data.child("totalDistance").getValue()).toString();
        String avDistance = Objects.requireNonNull(data.child("avDistance").getValue()).toString();
        String avBPM = Objects.requireNonNull(data.child("avBPM").getValue()).toString();
        String avStrength = Objects.requireNonNull(data.child("avStrength").getValue()).toString();

        TextView nbActivityTV = v.findViewById(R.id.edit_nb_activities);
        TextView totalDistanceTV = v.findViewById(R.id.edit_distance_total);
        TextView avDistanceTV = v.findViewById(R.id.edit_distance_av);
        TextView avBPMTV = v.findViewById(R.id.edit_bpm_av);
        TextView avStrengthTV = v.findViewById(R.id.edit_strength_av);

        nbActivityTV.setText(nbActivity);
        totalDistanceTV.setText("" + totalDistance + "m");
        avDistanceTV.setText("" + avDistance + "m");
        avBPMTV.setText((avBPM));
        avStrengthTV.setText(avStrength);
    }

    //Last track resume
    private void initializeLastTrackResume(DataSnapshot data, View v) {
        Log.i(TAG,"initializeLastTrackResume");
        String distanceLastTrack = Objects.requireNonNull(data.child("distance").getValue()).toString();
        String durationLastTrack = Objects.requireNonNull(data.child("duration").getValue()).toString();

        TextView distanceLastTrackTV = v.findViewById(R.id.edit_distance);
        TextView durationLastTrackTV = v.findViewById(R.id.edit_duration);

        distanceLastTrackTV.setText("" + distanceLastTrack + "m");
        durationLastTrackTV.setText("" + durationLastTrack + "m");

        for (DataSnapshot data2 : data.child("positionList").getChildren()) {
            positionsLastTrack.add(new Position(data2.child("lat").getValue(Double.class),data2.child("lng").getValue(Double.class)));
        }

        initializeMapLastTrack();
    }

    private void initializeMapLastTrack()  {
        // Initialize map fragment
        SupportMapFragment supportMapFragment=(SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                Log.i(TAG,"onMapReady");
                ArrayList<LatLng> location = new ArrayList<>();

                if (positionsLastTrack.size() > 0) {
                    PolylineOptions line = new PolylineOptions();
                    for (int i = 0; i < positionsLastTrack.size(); i++) {
                        location.add(new LatLng(positionsLastTrack.get(i).getLat(), positionsLastTrack.get(i).getLng()));

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
            }
        });
    }

    private void addBorderToRelativeLayout(View v) {
        RelativeLayout layoutResume = v.findViewById(R.id.resumeActivities);
        RelativeLayout layoutLastTrack = v.findViewById(R.id.lastTrack);
        ShapeDrawable rectShapeDrawable = new ShapeDrawable(); //pre defined class

        //get paint
        Paint paint = rectShapeDrawable.getPaint();

        //set border color, stroke and stroke width
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        layoutResume.setBackgroundDrawable(rectShapeDrawable);
        layoutLastTrack.setBackgroundDrawable(rectShapeDrawable);
    }
}