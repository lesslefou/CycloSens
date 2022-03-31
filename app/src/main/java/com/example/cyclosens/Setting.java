package com.example.cyclosens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cyclosens.databinding.ActivitySettingBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Setting extends AppCompatActivity {
    private ActivitySettingBinding binding;

    DatabaseReference mReference;
    String userId;
    TextView nameT,surnameT,emailT,ageT,sizeT,weighT;
    Button back,unsubscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //Set all the information of the user from the database on the screen
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();

            mReference = FirebaseDatabase.getInstance().getReference("user").child(userId);
            mReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String surname = dataSnapshot.child("surname").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();
                    String age = dataSnapshot.child("age").getValue().toString();
                    String size = dataSnapshot.child("email").getValue().toString();
                    String weigh = dataSnapshot.child("weigh").getValue().toString();

                    binding.editName.setText(name);
                    binding.editSurname.setText(surname);
                    binding.editEmail.setText(email);
                    binding.editAge.setText(age);
                    binding.editSize.setText(size);
                    binding.editWeight.setText(weigh);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //Go to the previous activity and close this page
            binding.btnBack.setOnClickListener(v -> finish());

            //Display dialog information
            binding.btnUnsubscribe.setOnClickListener(v -> showInformationSavedDialog());
        }


    }

    protected void showInformationSavedDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this,R.style.MyDialogTheme);
        builder.setMessage(R.string.dialogue_message_unsubscribe);
        builder.setCancelable(false);
        builder.setNegativeButton(R.string.no_answer, (dialog, which) -> dialog.cancel());
        builder.setPositiveButton(R.string.yes_answer, (dialog, which) -> {
            deleteUSer();
            dialog.cancel();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    //Delete the user and its information from the database
    protected void deleteUSer() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    deleteUserInformation();
                    Toast.makeText(Setting.this,R.string.account_deleted,Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(Setting.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
                else {
                    Toast.makeText(Setting.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void deleteUserInformation(){
        DatabaseReference databaseReferenceUser = FirebaseDatabase.getInstance().getReference("user").child(userId);
        databaseReferenceUser.removeValue();
    }
}
