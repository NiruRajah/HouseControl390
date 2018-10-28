package com.example.avkash.housecontrol;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity
{
    protected Button login_button = null;
    protected Button sign_up_button = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI(); //calling function setupUI
    }


    protected void setupUI() //setupUI function for opening grades page. NOT USED however. Here for additional future functionilities
    {

        login_button = (Button) findViewById(R.id.login_button);
        login_button.setOnClickListener(onClicklogin_button);

        sign_up_button = (Button) findViewById(R.id.sign_up_button);
        sign_up_button.setOnClickListener(onClicksign_up_button);


    }


    private void sendMessageToSignUp()
    {
        Intent intent = new Intent(this, signUpActivity.class);
        startActivity(intent);
    }
    private void sendMessageToControl()
    {
        Intent intent = new Intent(this, controlActivity.class);
        startActivity(intent);
    }
    private View.OnClickListener onClicklogin_button = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            sendMessageToControl();
        }
    };
    private View.OnClickListener onClicksign_up_button = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            sendMessageToSignUp();
        }
    };

}




