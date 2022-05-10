package com.example.cyclosens.activities;

import android.content.Intent;
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

import com.example.cyclosens.R;
import com.example.cyclosens.Welcome;
import com.example.cyclosens.classes.Activity;
import com.example.cyclosens.classes.Position;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivitiesFragment extends Fragment {
    private static final String TAG = ActivitiesFragment.class.getSimpleName(); //POUR LES LOG
    private ActivitiesAdapter adapter;
    private ArrayList<Activity> activities;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_activities, container, false);

        activities = new ArrayList<Activity>();
        progressBar = v.findViewById(R.id.progressBar);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            getActivities(userId);
        } else {
            Log.e(TAG,"uid user not found");
        }

        initRecycleView(v);
        return v;
    }

    private void getActivities(String userId) {
        Query post = FirebaseDatabase.getInstance().getReference("user").child(userId).child("Activities");
        post.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                Log.i(TAG,"test : " + snapshot);
                for (DataSnapshot data : snapshot.getChildren()) {
                    ArrayList<Position> location = new ArrayList<>();
                    String key = data.getKey();
                    String name = data.child("name").getValue(String.class);
                    String date = data.child("date").getValue(String.class);
                    long duration = data.child("duration").getValue(long.class);
                    int bpmAv = data.child("bpmAv").getValue(int.class);
                    int strengthAv = data.child("strengthAv").getValue(int.class);
                    int speedAv = data.child("speedAv").getValue(int.class);
                    int distance = data.child("distance").getValue(int.class);

                    for (DataSnapshot data2 : data.child("positionList").getChildren()) {
                        location.add(new Position(data2.child("lat").getValue(Double.class),data2.child("lng").getValue(Double.class)));
                    }

                    /*//Tranforlm object into Json
                    final GsonBuilder builder = new GsonBuilder();
                    final Gson gson = builder.create();
                    final String json = gson.toJson(location);
                    Log.i(TAG,"Resultat = " + json);*/

                    if (key != null) {
                        activities.add(new Activity(key, name, date, duration, bpmAv, strengthAv,speedAv,distance,location));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //Recycler view initializer
    private  void initRecycleView(View v){
        Log.d(TAG,"initRecycleView: init recyclerview");
        RecyclerView recyclerView = v.findViewById(R.id.activitiesRecycler);
        adapter = new ActivitiesAdapter(R.layout.activity_activities_adapter,activities);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

}