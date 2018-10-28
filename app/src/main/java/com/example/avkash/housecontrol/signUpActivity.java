package com.example.avkash.housecontrol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class signUpActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().setHomeButtonEnabled(true); //to toggle back to main page

    }
}
