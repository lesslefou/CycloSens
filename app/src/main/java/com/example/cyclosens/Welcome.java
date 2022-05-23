package com.example.cyclosens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.cyclosens.activities.ActivitiesFragment;
import com.example.cyclosens.databinding.ActivityWelcomeBinding;
import com.google.firebase.auth.FirebaseAuth;

public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWelcomeBinding binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLaunch.setOnClickListener(view -> {
            Intent launch = new Intent(Welcome.this, Launcher.class);
            startActivity(launch);
        });

        //Set the first fragment
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if (getIntent().getExtras().getString("fragment").equals("start")) {
            StartFragment startFragment = new StartFragment();
            transaction.add(R.id.fragment_place, startFragment);
        } else {
            ActivitiesFragment activitiesFragment = new ActivitiesFragment();
            transaction.add(R.id.fragment_place, activitiesFragment);
        }
        transaction.commit();
    }
    /**
     * Display the fragment that the user had selected with the button
     */
    @SuppressLint("NonConstantResourceId")
    public void onSelectFragment(View view) {
        Fragment newFragment = new Fragment();

        switch (view.getId()) {
            case R.id.btnActivities:
                newFragment = new ActivitiesFragment();
                break;
            case R.id.btnItineraries:
                newFragment = new ItinerariesFragment();
                break;
        }

        getSupportFragmentManager().popBackStack();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_place, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menumain,menu);
        return true;
    }

    /**
     * Redirects the user to the good activity
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                return true;
            case R.id.setting:
                Intent i = new Intent(Welcome.this, Setting.class);
                startActivity(i);
                return true;
            case R.id.devices:
                Intent i1 = new Intent(Welcome.this, Devices.class);
                startActivity(i1);
                return true;
            case R.id.aboutUs:
                Intent i2 = new Intent(Welcome.this, AboutUs.class);
                startActivity(i2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Allows the disconnection of the user on the application
     */
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(getApplicationContext(),MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        getSupportFragmentManager().popBackStack();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_place, new StartFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}