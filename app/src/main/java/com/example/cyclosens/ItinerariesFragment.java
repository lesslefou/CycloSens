package com.example.cyclosens;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cyclosens.activities.ActivitiesAdapter;
import com.example.cyclosens.classes.Activity;
import com.example.cyclosens.classes.Itinerary;
import com.example.cyclosens.classes.Position;

import java.util.ArrayList;

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

        ArrayList<Position> positions = new ArrayList<>();
        positions.add(new Position(43.117030,5.932195));
        positions.add(new Position(43.118030,5.933195));
        positions.add(new Position(43.119030,5.934195));


        itineraries = new ArrayList<>();
        itineraries.add(new Itinerary(30, 1500F, positions));
        itineraries.add(new Itinerary(20, 1000F, positions));
        itineraries.add(new Itinerary(10, 500F, positions));

        initRecycleView(v);
        return v;
    }

    //Initialise the recyclerView
    private  void initRecycleView(View v){
        Log.d(TAG,"initRecycleView: init recyclerview");
        RecyclerView recyclerView = v.findViewById(R.id.itinerariesRecycler);
        ItinerariesAdapter adapter = new ItinerariesAdapter(R.layout.activity_itineraries_adapter, itineraries);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}