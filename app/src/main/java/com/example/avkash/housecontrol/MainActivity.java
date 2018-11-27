package com.example.avkash.housecontrol;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity
{
    protected Button login_button = null;
    protected Button sign_up_button = null;
    protected EditText email_editText;
    protected EditText password_editText;


    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null)
        {
            finish();
            sendMessageToControl();
        }

        login_button = (Button) findViewById(R.id.login_button);
        sign_up_button = (Button) findViewById(R.id.sign_up_button);
        email_editText = (EditText) findViewById(R.id.email_editText);
        password_editText = (EditText) findViewById(R.id.password_editText);

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

    private void userLogin ()
    {
        String email = email_editText.getText().toString().trim();
        String password = password_editText.getText().toString().trim();
        if(TextUtils.isEmpty(email))
        {
            //email is empty
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(password))
        {
            //password is empty
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                            sendMessageToControl();

                        }
                        else
                        {

                            Toast.makeText(MainActivity.this, "Cannot Log In: Incorrect Email/Password", Toast.LENGTH_SHORT).show();
                            progressDialog.cancel();

                        }
                    }
                });
    }

    private View.OnClickListener onClicklogin_button = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            userLogin();
            //sendMessageToControl();
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




