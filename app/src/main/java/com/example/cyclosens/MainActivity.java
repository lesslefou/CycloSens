package com.example.cyclosens;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View signUp = findViewById(R.id.btn_sign);
        signUp.setOnClickListener(this);
        View logIn = findViewById(R.id.btn_log);
        logIn.setOnClickListener(this);

    }

    @Override
    //Go to the good activity
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign:
                Intent sign = new Intent(MainActivity.this, Sign_Up.class);
                startActivity(sign);
                break;
            case R.id.btn_log:
                Intent log = new Intent(MainActivity.this, Log_In.class);
                startActivity(log);
                break;
        }
    }
}