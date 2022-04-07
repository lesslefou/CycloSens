package com.example.cyclosens.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.example.cyclosens.R;
import com.example.cyclosens.classes.Activity;
import com.example.cyclosens.databinding.ActivityInformationBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ActivityInformation extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = ActivityInformation.class.getSimpleName(); //POUR LES LOG
    private ActivityInformationBinding binding;
    private Activity activity;

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
        //Set the location of Isen Toulon Campus on a map
        LatLng isenToulon = new LatLng(43.120562,5.939687);
        googleMap.addMarker(new MarkerOptions().position(isenToulon).title("Isen Toulon"));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(isenToulon, 13f));
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