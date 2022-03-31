package com.example.cyclosens.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.cyclosens.R;
import com.example.cyclosens.databinding.ActivityInformationBinding;

public class ActivityInformation extends AppCompatActivity {
    private ActivityInformationBinding binding;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activity = getIntent().getExtras().getParcelable("activity");

        binding.name.setText(activity.getNameActivity());
        binding.date.setText(activity.getDateActivity());


    }
}