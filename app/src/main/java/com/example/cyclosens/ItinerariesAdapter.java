package com.example.cyclosens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cyclosens.activities.ActivitiesAdapter;
import com.example.cyclosens.activities.ActivityInformation;
import com.example.cyclosens.classes.Activity;
import com.example.cyclosens.classes.Itinerary;
import com.example.cyclosens.classes.Position;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Objects;

public class ItinerariesAdapter extends RecyclerView.Adapter<ItinerariesAdapter.ViewHolder>{
    private static final String TAG = ItinerariesAdapter.class.getSimpleName(); //POUR LES LOG
    private final ArrayList<Itinerary> itineraries;
    private final int itemResource;

    public static class ViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        private final TextView durationActivity;
        private final TextView distanceActivity;
        private final MapView map;
        private Itinerary itinerary;

        public ViewHolder(View view) {
            super(view);
            this.durationActivity = (TextView) itemView.findViewById(R.id.durationI);
            this.distanceActivity = (TextView) itemView.findViewById(R.id.distanceI);
            this.map =  (MapView) itemView.findViewById(R.id.mapI);
            if (map != null) {
                map.onCreate(null);
                map.onResume();
                map.getMapAsync(this);
            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            Log.i(TAG,"onMapReady : " + itinerary + "  " + itinerary.getPositionList().size());
            MapsInitializer.initialize(itemView.getContext());
            ArrayList<Position> locationDouble = itinerary.getPositionList();
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

    }

    public ItinerariesAdapter(int itemResource, ArrayList<Itinerary> itineraries) {
        this.itineraries = itineraries;
        this.itemResource = itemResource;
    }

    @NonNull
    @Override
    public ItinerariesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemResource,parent,false);
        return new ItinerariesAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    //Print the distance and duration of the activity
    public void onBindViewHolder(ItinerariesAdapter.ViewHolder holder, int position) {
        holder.itinerary = itineraries.get(position);
        Log.i(TAG, "onBindViewHolder " + holder.itinerary);
        holder.distanceActivity.setText(holder.itemView.getContext().getString(R.string.distance) + holder.itinerary.getDistance() + "m");
        holder.durationActivity.setText(holder.itemView.getContext().getString(R.string.duration) + holder.itinerary.getDuration() + "m");
    }

    @Override
    public int getItemCount() {
        return this.itineraries.size();
    }

}
