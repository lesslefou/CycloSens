package com.example.cyclosens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cyclosens.classes.User;
import com.example.cyclosens.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Objects;

public class Sign_Up extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private static final String TAG = Sign_Up.class.getSimpleName(); //POUR LES LOG

    private FirebaseAuth mAuth;
    private User user;
    private DatabaseReference mReference;

    public static final String NOTIFICATION_CHANNEL_ID = "CycloSens";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();

        binding.btnSign.setOnClickListener(v -> registerUser());
        binding.btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void registerUser() {
        final String name = binding.editName.getText().toString().trim();
        final String surname = binding.editSurname.getText().toString().trim();
        final String email = binding.editEmail.getText().toString().trim();
        final String password = binding.editPassword.getText().toString().trim();

        /*Check if the user filled in all the fields*/
        if (name.isEmpty() ){
            binding.editName.setError(getString(R.string.notName));
            binding.editName.requestFocus();
        }
        if (surname.isEmpty() ){
            binding.editSurname.setError(getString(R.string.notSurname));
            binding.editSurname.requestFocus();
        }

        if (email.isEmpty()) {
            binding.editEmail.setError(getString(R.string.notSurname));
            binding.editEmail.requestFocus();
        }
        else {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.editEmail.setError(getString(R.string.wrongEmail));
                binding.editEmail.requestFocus();
            }
        }

        /*Check if the user respects the password constraints*/
        boolean validPassword = false;
        boolean hasNumbers = false;
        boolean hasLowerCase = false;
        boolean hasUpperCase = false;
        for (int i = 0; i < password.length(); i++) {
            if (Character.isDigit(password.charAt(i))) {
                hasNumbers = true;
            } else if (Character.isLowerCase(password.charAt(i))) {
                hasLowerCase = true;
            } else if (Character.isUpperCase(password.charAt(i))) {
                hasUpperCase = true;
            }
        }
        if (hasNumbers && hasLowerCase && hasUpperCase && (password.length() >= 8)) {
            validPassword = true;
        }

        if (!validPassword) {
            binding.editPassword.setError((getString(R.string.wrongPassword)));
            binding.editPassword.requestFocus();
        }


        if (!name.isEmpty() || !surname.isEmpty() || !email.isEmpty() || !password.isEmpty()) {

            binding.progressBar.setVisibility(View.VISIBLE);

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser fuser = mAuth.getCurrentUser();
                        /*Send a verification mail to be sure the email of the user is his own*/
                        Objects.requireNonNull(fuser).sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Sign_Up.this,R.string.emailSent,Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Email not sent" + e.getMessage());
                            }
                        });

                        /*Creation of the user database*/
                        String userId = mAuth.getCurrentUser().getUid();
                        mReference = FirebaseDatabase.getInstance().getReference("user").child(userId).child("Details");
                        user = new User();
                        user.setName(name);
                        user.setSurname(surname);
                        user.setEmail(email);

                        mReference.setValue(user);
                        notificationDialog();
                        logout();
                    } else {
                        Toast.makeText(Sign_Up.this, R.string.error + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        binding.progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
        else {
            Toast.makeText(Sign_Up.this,R.string.notInfo,Toast.LENGTH_LONG).show();
        }

    }


    /**
     * Allows the disconnection of the user on the application in order to force in to click on his email validation
     */
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(getApplicationContext(),MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private void notificationDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant")
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "My Notifications",
                    NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            channel.enableVibration(true);


            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);


        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Welcome on our superApp !!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(1, notification);

    }
}