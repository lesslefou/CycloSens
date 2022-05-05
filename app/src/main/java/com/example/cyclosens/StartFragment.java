package com.example.cyclosens;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class StartFragment extends Fragment implements OnMapReadyCallback {
    DatabaseReference mReference;
    String userId;
    TextView ditanceLastTrack, durationLastTrack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_start, container, false);

        ProgressBar progressBar = v.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        //Set all the information of the user from the database on the screen
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();

            mReference = FirebaseDatabase.getInstance().getReference("user").child(userId).child("Activities"); //RECUPERER LA DERNIERE TRACK
            mReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //String distance = dataSnapshot.child("name").getValue().toString();
                    String duration = dataSnapshot.child("duration").getValue().toString();

                    //ditanceLastTrack = v.findViewById(R.id.distance);
                    durationLastTrack = v.findViewById(R.id.duration);

                    //ditanceLastTrack.setText((distance));
                    durationLastTrack.setText(duration);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        return v;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }
}