package com.example.cyclosens;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.cyclosens.activities.ActivitiesAdapter;
import com.example.cyclosens.classes.Activity;
import com.example.cyclosens.classes.Itinerary;
import com.example.cyclosens.classes.Position;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class ItinerariesFragment extends Fragment {

    private static final String TAG = ItinerariesFragment.class.getSimpleName(); //POUR LES LOG;
    private ArrayList<Itinerary> itineraries;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_itineraries, container, false);

        getAvSpeedUser(v);
        return v;
    }

    private void getAvSpeedUser(View v) {
        final int[] cpt = {0};
        final int[] avSpeed = {0};
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("ResumeActivities");
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot data) {
                    if (cpt[0] == 0 ) {
                        if(data.exists()) {  avSpeed[0] = Integer.parseInt(Objects.requireNonNull(data.child("avSpeed").getValue()).toString()); }

                        creationBddItineraries(avSpeed[0], v);

                        cpt[0]++;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {  }
            });
        }
    }

    //Create the itineraries
    private void creationBddItineraries(int speedUser, View v) {
        int distanceParcours = 0;
        int duration=0;
        itineraries = new ArrayList<>();

        ArrayList<Position> positions = new ArrayList<>();
        positions.add(new Position(43.115047,5.944390));
        positions.add(new Position(43.112681,5.949723));
        positions.add(new Position(43.114443,5.950406));

        distanceParcours = getDistanceFromItineraries(positions);
        if (speedUser == 0) { duration = 0; } else { duration = distanceParcours/speedUser;}
        itineraries.add(new Itinerary(duration/60, distanceParcours, positions));

        ArrayList<Position> positions1 = new ArrayList<>();
        positions1.add(new Position(43.121386,5.938508));
        positions1.add(new Position(43.121504,5.937977));
        positions1.add(new Position(43.121747,5.936700));
        positions1.add(new Position(43.122741,5.936770));
        distanceParcours= getDistanceFromItineraries(positions1);
        if (speedUser == 0) { duration = 0; } else { duration = distanceParcours/speedUser;}
        itineraries.add(new Itinerary(duration/60, distanceParcours, positions1));

        ArrayList<Position> positions2 = new ArrayList<>();
        positions2.add(new Position(43.118963,5.934800));
        positions2.add(new Position(43.121966,5.929008));
        positions2.add(new Position(43.122954,5.929459));
        positions2.add(new Position(43.122646,5.931870));
        distanceParcours= getDistanceFromItineraries(positions2);
        if (speedUser == 0) { duration = 0; } else { duration = distanceParcours/speedUser;}
        itineraries.add(new Itinerary(duration/60, distanceParcours, positions2));

        initRecycleView(v);
    }

    //RecyclerView initializer
    private  void initRecycleView(View v){
        ProgressBar progressBar = v.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        Log.d(TAG,"initRecycleView: init recyclerview");
        RecyclerView recyclerView = v.findViewById(R.id.itinerariesRecycler);
        ItinerariesAdapter adapter = new ItinerariesAdapter(R.layout.activity_itineraries_adapter, itineraries);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    //Know the whole distance of an itinerary
    private int getDistanceFromItineraries(ArrayList<Position> positions) {
        int distanceParcours = 0;
        for (int i=1; i< positions.size(); i++) {
            distanceParcours += getDistanceBetweenTwoLocation(positions,i);
        }
        return distanceParcours;
    }


    private float getDistanceBetweenTwoLocation(ArrayList<Position> positionList, int i) {
        Location locationA = new Location("point A");
        locationA.setLatitude(positionList.get(i).getLat());
        locationA.setLongitude(positionList.get(i).getLng());

        Location locationB = new Location("point B");
        locationB.setLatitude(positionList.get(i-1).getLat());
        locationB.setLongitude(positionList.get(i-1).getLng());

        return locationA.distanceTo(locationB);
    }
}