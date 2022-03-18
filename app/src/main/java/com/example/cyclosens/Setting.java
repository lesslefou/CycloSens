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

    DatabaseReference mReference;
    String userId;
    TextView nameT,surnameT,emailT,ageT,sizeT,weighT;
    Button back,unsubscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        nameT = findViewById(R.id.edit_name);
        surnameT = findViewById(R.id.edit_surname);
        emailT = findViewById(R.id.edit_email);
        ageT = findViewById(R.id.edit_age);
        sizeT = findViewById(R.id.edit_size);
        weighT = findViewById(R.id.edit_weight);


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

                    nameT.setText(name);
                    surnameT.setText(surname);
                    emailT.setText(email);
                    ageT.setText(age);
                    sizeT.setText(size);
                    weighT.setText(weigh);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //Go to the previous activity and close this page
            back = findViewById(R.id.btn_back);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            //Display dialog information
            unsubscribe = findViewById(R.id.btn_unsubscribe);
            unsubscribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showInformationSavedDialog();
                }
            });
        }


    }

    protected void showInformationSavedDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this,R.style.MyDialogTheme);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setMessage(R.string.dialogue_message_unsubscribe);
        builder.setCancelable(false);
        builder.setNegativeButton(R.string.no_answer, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton(R.string.yes_answer, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUSer();
                dialog.cancel();
            }
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
