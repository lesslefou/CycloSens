package com.example.cyclosens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Log_In extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private EditText editTextEmail, editTextPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        editTextEmail = findViewById(R.id.edit_email);
        editTextPassword = findViewById(R.id.edit_password);

        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        //If the user didn't log out he will see this page, he will directly go on the welcome page
        if (mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),Welcome.class));
            finish();
        }

        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.btn_log).setOnClickListener(this);
        findViewById(R.id.forgotPassword).setOnClickListener(this);
    }


    private void userLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        //Sent error is it empty
        if (email.isEmpty()) {
            editTextEmail.setError(getString(R.string.notEmail));
            editTextEmail.requestFocus();
            return;
        }

        //Sent error is it empty
        if (password.isEmpty()) {
            editTextPassword.setError(getString(R.string.notPassword));
            editTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        //Sent error is the information are wrong or go in the welcome page
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(Log_In.this, R.string.welcomeUser, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(),Welcome.class));
                }else {
                    Toast.makeText(Log_In.this, R.string.error_log_in, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    //Go on the good page
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;

            case R.id.btn_log:
                userLogin();
                break;

            case R.id.forgotPassword:
                startActivity(new Intent(this, ResetPassword.class));
        }
    }
}