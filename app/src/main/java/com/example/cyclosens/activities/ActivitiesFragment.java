package com.example.cyclosens.activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cyclosens.R;
import com.example.cyclosens.databinding.FragmentActivitiesBinding;

import java.util.ArrayList;

public class ActivitiesFragment extends Fragment {
    private static final String TAG = "ActivitiesFragment";

    private RecyclerView recyclerView;
    private ActivitiesAdapter adapter;
    private ArrayList<Activity> activities;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_activities, container, false);

        activities = new ArrayList<Activity>();

        activities.add(new Activity("Piste1","14jan"));
        activities.add(new Activity("Piste2","14fev"));

        initRecycleView(v);
        return v;
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