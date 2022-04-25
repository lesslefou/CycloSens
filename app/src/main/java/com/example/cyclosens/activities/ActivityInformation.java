package com.example.cyclosens.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cyclosens.classes.MapData;
import com.example.cyclosens.R;
import com.example.cyclosens.classes.Activity;
import com.example.cyclosens.databinding.ActivityInformationBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ActivityInformation extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = ActivityInformation.class.getSimpleName(); //POUR LES LOG
    private ActivityInformationBinding binding;
    private Activity activity;
    private GoogleMap mMap;
    private Double originLatitude = 28.5021359;
    private Double originLongitude = 77.4054901;
    private Double destinationLatitude = 28.5151087;
    private Double destinationLongitude = 77.3932163;



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activity = (Activity) getIntent().getExtras().getSerializable("activity");

        binding.name.setText(activity.getNameActivity());
        binding.date.setText(activity.getDateActivity());
        binding.duration.setText("" + getString(R.string.duration) + activity.getDuration());
        binding.bpmAv.setText("" + getString(R.string.bpm) + activity.getBpmAv());
        binding.strenghAv.setText("" + getString(R.string.strengh) + activity.getStrenghAv());
        binding.settings.setOnClickListener(view -> updateNameActivity());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng originLocation = new LatLng(originLatitude, originLongitude);
        mMap.addMarker(new MarkerOptions().position(originLocation));

        LatLng destinationLocation = new LatLng(originLatitude, originLongitude);
        mMap.addMarker(new MarkerOptions().position(destinationLocation));


        String apiKey ="AIzaSyAA_vRbbWvsLYO-axNqR7pYSFSJz2IxypE";
        if (!Places.isInitialized()) {
            Places.initialize(this, apiKey);
        }

        String urll = getDirectionURL(originLocation, destinationLocation, apiKey);
        GetDirection getDirection = new GetDirection(urll);
        getDirection.execute();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 13f));
    }

    private String getDirectionURL (LatLng origin, LatLng dest, String secret) {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&sensor=false" +
                "&mode=driving" +
                "&key=$secret";
    }


    private class GetDirection extends AsyncTask<Void, Void, List<List<LatLng>>> {
        String url = "";

        public GetDirection (String url) {
            this.url = url;
        }

        @Override
        protected List<List<LatLng>> doInBackground (Void... params) {
            Log.i(TAG, "doInBackground");
            OkHttpClient client = new OkHttpClient();
            Request request =  new Request.Builder().url(url).build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String data = null;
            try {
                data = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayList<List<LatLng>> result = new ArrayList<List<LatLng>>();
            try {
                MapData respObj = new Gson().fromJson(data, MapData.class);
                ArrayList<LatLng> path = new ArrayList<LatLng>();
                for (int i = 0; i< respObj.routes.get(0).legs.get(0).steps.size(); i++){
                    path.addAll(decodePolyline(respObj.routes.get(0).legs.get(0).steps.get(i).polyline.points));
                }
                result.add(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onPostExecute(List<List<LatLng>> result) {
            Log.i(TAG, "onPostExecute");
            PolylineOptions lineoption = new PolylineOptions();
            for (int i=0; i<result.size(); i++){
                lineoption.addAll(result.get(i));
                lineoption.width(10f);
                lineoption.color(Color.GREEN);
                lineoption.geodesic(true);
            }
            mMap.addPolyline(lineoption);
        }

    }


    private List<LatLng> decodePolyline (String encoded) {
        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat;
            if ((result & 1) != 0) {
                dlat = ~(result >> 1);
            } else {
                dlat = result >> 1;
            }

            lat += dlat;
            shift = 0;
            result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlng;
            if ((result & 1) != 0) {
                dlng = ~(result >> 1);
            } else {
                dlng = result >> 1;
            }

            lng += dlng;
            LatLng latLng = new LatLng((lat / 1E5),(lng/ 1E5));
            poly.add(latLng);
        }
        return poly;
    }

    private void updateNameActivity() {
        runOnUiThread(new Thread(() -> {
            EditText edittTxt = new EditText(ActivityInformation.this);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams (
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(16,0,16,0);
            edittTxt.setLayoutParams(params);

            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(ActivityInformation.this),R.style.MyDialogTheme);
            builder.setTitle(R.string.changeName)
                    .setView(edittTxt)
                    .setPositiveButton(R.string.validate, (dialogInterface, i) -> {
                        activity.setNameActivity(edittTxt.getText().toString());
                        //MODIFIER AFFICHAGE
                        databaseUpdate();
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
            mRef.updateChildren(activityUpdate);
        }
    }

}