package com.example.avkash.housecontrol;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UpdatePhoneNoFragment extends DialogFragment //update phone fragment class
{
    private static final String TAG = "UpdatePhoneNumber";

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databasePhoneNo;
    private FirebaseUser user;
    private String phoneNoOnBase = null;

    EditText phoneText;
    Button cancel_but;
    Button phone_button;
    TextView phoneNoView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflator, @Nullable ViewGroup container, Bundle savedInstanceState) { //onCreate function

        View view = inflator.inflate(R.layout.phone_number, container, false);

        phoneText = view.findViewById(R.id.phoneText);
        cancel_but = view.findViewById(R.id.cancel_but);
        phone_button = view.findViewById(R.id.phone_button);
        phoneNoView = view.findViewById(R.id.phoneNoView);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databasePhoneNo = database.getReference();

        cancel_but.setOnClickListener(new Button.OnClickListener() { //cancel button
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onclick: cancel button");
                getDialog().dismiss();
            }
        });
        phone_button.setOnClickListener(new Button.OnClickListener() { //save button
            @Override
            public void onClick(View view) {
                String phoneNo = phoneText.getText().toString().trim();

                if(!((phoneNo.length() < 11)))//assuming not blank and not below 11 digits
                {
                    FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();
                    Toast.makeText(getActivity(), "Phone Number Saved Successfully", Toast.LENGTH_LONG).show();
                    databasePhoneNo.child(user.getUid()).child("Phone Number").setValue(phoneNo);
                    getDialog().dismiss();

                }

            }
        });
        phoneUpdate();



return view;
    }


    public void phoneUpdate() //update phone function
    {
        user = firebaseAuth.getInstance().getCurrentUser();
        DatabaseReference phoneReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child("Phone Number");
        phoneReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                phoneNoOnBase = dataSnapshot.getValue(String.class);
                phoneNoView.setText("Phone Number: " +phoneNoOnBase);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

}
