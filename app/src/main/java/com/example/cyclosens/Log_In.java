package com.example.cyclosens;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.cyclosens.activities.ActivitiesFragment;
import com.example.cyclosens.activities.ActivityInformation;
import com.example.cyclosens.classes.User;
import com.example.cyclosens.databinding.ActivityLogInBinding;
import com.example.cyclosens.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Log_In extends AppCompatActivity {

    private ActivityLogInBinding binding;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        //If the user didn't log out he will not see this page, he will directly go on the welcome page
        if (mAuth.getCurrentUser() != null){
            Intent i = new Intent(Log_In.this, Welcome.class);
            i.putExtra("fragment", "start");
            startActivity(i);
            finish();
        }

        //Go on the good page
        binding.btnBack.setOnClickListener(view -> {
            finish();
            Intent i = new Intent(Log_In.this, MainActivity.class);
            startActivity(i);

        });
        binding.btnLog.setOnClickListener(v-> userLogin());
        binding.forgotPassword.setOnClickListener(v -> {
            finish();
            Intent j = new Intent(Log_In.this, ResetPassword.class);
            startActivity(j);
        });
    }


    private void userLogin() {
        String email = binding.editEmail.getText().toString().trim();
        String password = binding.editPassword.getText().toString().trim();

        //Sent error is it empty
        if (email.isEmpty()) {
            binding.editEmail.setError(getString(R.string.notEmail));
            binding.editEmail.requestFocus();
            return;
        }

        //Sent error is it empty
        if (password.isEmpty()) {
            binding.editPassword.setError(getString(R.string.notPassword));
            binding.editPassword.requestFocus();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        //Sent error is the information are wrong or go in the welcome page
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    if (checkIfEmailConfirm()) {
                        Toast.makeText(Log_In.this, R.string.welcomeUser, Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(Log_In.this, Welcome.class);
                        i.putExtra("fragment", "start");
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(Log_In.this, R.string.emailConfirmation, Toast.LENGTH_SHORT).show();
                        binding.progressBar.setVisibility(View.GONE);
                    }
                }else {
                    Toast.makeText(Log_In.this, R.string.error_log_in, Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private boolean checkIfEmailConfirm() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.isEmailVerified();
    }


}