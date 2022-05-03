package com.example.cyclosens.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cyclosens.R;
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

    private RecyclerView recyclerView;
    private ActivitiesAdapter adapter;
    private ArrayList<Activity> activities;
    private DatabaseReference mRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_activities, container, false);

        activities = new ArrayList<Activity>();

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
        mRef =  FirebaseDatabase.getInstance().getReference("user").child(userId).child("Activities");
        Query post = mRef;
        post.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    ArrayList<Position> location = new ArrayList<>();
                    String key = data.getKey();
                    String name = data.child("name").getValue(String.class);
                    String date = data.child("date").getValue(String.class);
                    long duration = data.child("duration").getValue(long.class);
                    int bpmAv = data.child("bpmAv").getValue(int.class);
                    int strenghAv = data.child("bpmAv").getValue(int.class);

                    for (DataSnapshot data2 : data.child("positionList").getChildren()) {
                        location.add(new Position(data2.child("latitude").getValue(Double.class),data2.child("longitude").getValue(Double.class)));
                    }

                    //TRANSFORME UN OBJET EN JSON
                    final GsonBuilder builder = new GsonBuilder();
                    final Gson gson = builder.create();
                    final String json = gson.toJson(location);
                    Log.i(TAG,"Resultat = " + json);

                    Log.d(TAG, "key :" + key );
                    if (key != null) {
                        Log.d(TAG, "key : " + key + " name : "  + name + " date : " + date + " duration : " + duration);
                        activities.add(new Activity(key, name, date, duration, bpmAv, strenghAv,location));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //Initialise the recyclerView
    private  void initRecycleView(View v){
        Log.d(TAG,"initRecycleView: init recyclerview");
        recyclerView = v.findViewById(R.id.gameRecycler);
        adapter = new ActivitiesAdapter(R.layout.activity_activities_adapter,activities);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

}