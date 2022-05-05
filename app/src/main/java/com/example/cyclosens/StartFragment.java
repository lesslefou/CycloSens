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


@SuppressLint("SetTextI18n")
public class StartFragment extends Fragment {
    private static final String TAG = StartFragment.class.getSimpleName(); //POUR LES LOG
    DatabaseReference mReference;
    String userId;
    TextView distanceLastTrackTV, durationLastTrackTV, totalDistanceTV, avDistanceTV, avBPMTV, avStrengthTV, nbActivityTV;
    String durationLastTrack,distanceLastTrack, totalDistance, avDistance, avBPM, avStrength,nbActivity;
    ArrayList<Position> positionsLastTrack;
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
            userId = firebaseUser.getUid();

            ProgressBar progressBar = v.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);

            mReference = FirebaseDatabase.getInstance().getReference("user").child(userId);
            mReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (cpt == 0 ) {
                        initializeActivitiesResume(dataSnapshot.child("ResumeActivities"),v);

                        for (DataSnapshot data : dataSnapshot.child("Activities").getChildren()) {
                            Log.i(TAG,"lastTrack : " + data);
                            initializeLastTrackResume(data,v);
                            break;
                        }
                        cpt ++;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {  }
            });
        }

        addBorderToRelativeLayout(v);

        return v;
    }

    private void initializeActivitiesResume(DataSnapshot data, View v) {
        Log.i(TAG,"initializeActivitiesResume");

        nbActivity = data.child("nbActivities").getValue().toString();
        totalDistance = data.child("totalDistance").getValue().toString();
        avDistance = data.child("avDistance").getValue().toString();
        avBPM = data.child("avBPM").getValue().toString();
        avStrength = data.child("avStrength").getValue().toString();

        nbActivityTV = v.findViewById(R.id.edit_nb_activities);
        totalDistanceTV = v.findViewById(R.id.edit_distance_total);
        avDistanceTV = v.findViewById(R.id.edit_distance_av);
        avBPMTV = v.findViewById(R.id.edit_bpm_av);
        avStrengthTV = v.findViewById(R.id.edit_strength_av);

        nbActivityTV.setText(nbActivity);
        totalDistanceTV.setText("" + totalDistance + "m");
        avDistanceTV.setText("" + avDistance + "m");
        avBPMTV.setText((avBPM));
        avStrengthTV.setText(avStrength);
    }

    private void initializeLastTrackResume(DataSnapshot data, View v) {
        Log.i(TAG,"initializeLastTrackResume");
        distanceLastTrack = data.child("distance").getValue().toString();
        durationLastTrack = data.child("duration").getValue().toString();

        distanceLastTrackTV = v.findViewById(R.id.edit_distance);
        durationLastTrackTV = v.findViewById(R.id.edit_duration);

        distanceLastTrackTV.setText("" + distanceLastTrack + "m");
        durationLastTrackTV.setText("" + durationLastTrack + "s");

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

                        googleMap.addPolyline(line.add( //On rajoute a notre ligne
                                location.get(i)). //L'element actuel de l'array list
                                width(5) //specify the width of poly line
                                .color(Color.GREEN) //add color to our poly line.
                                .geodesic(true)); //make our poly line geodesic
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location.get(i), 15));
                    }

                    //On rajoute un marqueur au début du track
                    googleMap.addMarker(new MarkerOptions().position(location.get(0)).title("Start"));
                    //On recupere la taille totale de l'array liste
                    int size = location.size();
                    //On rajoute un marqueur à la fin du track
                    googleMap.addMarker(new MarkerOptions().position(location.get(size-1)).title("End"));
                }
            }
        });
    }

    private void addBorderToRelativeLayout(View v) {
        RelativeLayout layoutResume= v.findViewById(R.id.resumeActivities);
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