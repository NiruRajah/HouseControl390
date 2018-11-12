package com.example.avkash.housecontrol;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class signUpActivity extends AppCompatActivity implements View.OnClickListener
{
protected Button submit_button;
protected EditText first_name_editText;
protected EditText last_name_editText;
protected EditText email_editText2;
protected EditText password_EditText2;

private ProgressDialog progressDialog;
private FirebaseAuth firebaseAuth;

private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().setHomeButtonEnabled(true); //to toggle back to main page

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null)
        {
            finish();
            sendMessageToControl();
        }




        submit_button = (Button) findViewById(R.id.submit_button);




        progressDialog = new ProgressDialog(this);



        first_name_editText = (EditText) findViewById(R.id.first_name_editText);
        last_name_editText = (EditText) findViewById(R.id.last_name_editText);
        email_editText2 = (EditText) findViewById(R.id.email_editText2);
        password_EditText2 = (EditText) findViewById(R.id.password_EditText2);

        submit_button.setOnClickListener(this);



    }

    private void saveUserInformation()
    {

        String fname = first_name_editText.getText().toString().trim();
        String lname = last_name_editText.getText().toString().trim();
        String tempName = fname.concat(" ");
        String name = tempName.concat(lname);

        //UserInformation userInformation = new UserInformation(fname,lname);

        FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            sendMessageToControl();
                        }
                    }
                });

    }

    private void registerUser()
    {
        String email = email_editText2.getText().toString().trim();
        String password = password_EditText2.getText().toString().trim();

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

        progressDialog.setMessage("Registering User...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(task.isSuccessful())
                        {

                            Toast.makeText(signUpActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                            saveUserInformation();


                        }
                        else
                        {
                            Toast.makeText(signUpActivity.this, "Cannot Register, Try Again Please", Toast.LENGTH_SHORT).show();
                            progressDialog.cancel();
                        }
                    }
                });


    }

    private void sendMessageToControl()
    {

        Intent intent = new Intent(this, controlActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        registerUser();

    }
}
