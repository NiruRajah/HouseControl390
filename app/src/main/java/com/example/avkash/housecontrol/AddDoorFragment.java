package com.example.avkash.housecontrol;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;

public class AddDoorFragment extends DialogFragment { //Insert Assignment Dialog class
    private static final String TAG = "InsertDoorDialog";

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseDoorsX;
    private List<Door> doorListX;
    private String humidityX = null;
    private String temperatureCX = null;
    private String temperatureFX = null;
    private String doorStatus1 = null;
    private String doorStatus2 = null;
    private String doorStatus3 = null;

    Switch toggleSwitch;
    EditText optionText;
    Button cancelButton2;
    Button saveButton;
    TextView addDoorTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflator, @Nullable ViewGroup container, Bundle savedInstanceState)
    { //onCreate function

        View view = inflator.inflate(R.layout.add_door_layout, container, false);

        optionText = view.findViewById(R.id.optionText);
        toggleSwitch = view.findViewById(R.id.toggleSwitch);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton2 = view.findViewById(R.id.cancelButton2);
        addDoorTextView = view.findViewById(R.id.addDoorTextView);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseDoorsX = database.getReference("doors").child(user.getUid());
        doorListX = new ArrayList<>();

        setSensorInfo();

        cancelButton2.setOnClickListener(new Button.OnClickListener() { //cancel button
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onclick: cancel button");
                getDialog().dismiss();
            }
        });
        saveButton.setOnClickListener(new Button.OnClickListener() { //save button
            @Override
            public void onClick(View view) {
                String doorName = optionText.getText().toString().trim();
                boolean doorStatus = toggleSwitch.isChecked();

                if(!(doorName.equals("")))//assuming not blank
                {
                    Toast.makeText(getActivity(), "Door Added Successfully", Toast.LENGTH_LONG).show();
                    addDoor(doorName, doorStatus);
                    getDialog().dismiss();

                }

            }
        });
        doorListX.clear();
        return view;
    }

    public boolean addDoor(String namex, boolean togg)
    {
        String name = namex;
        boolean switchState = togg;

        int size = getDoorListFromDatabase().size();

        String id = databaseDoorsX.push().getKey();

        if(size == 0)
        {
            Door door = new Door (id, name, switchState, doorStatus1, humidityX, temperatureCX, temperatureFX);
            databaseDoorsX.child(id).setValue(door);
        }
        else if (size == 1)
        {
            Door door = new Door (id, name, switchState, doorStatus2, humidityX, temperatureCX, temperatureFX);
            databaseDoorsX.child(id).setValue(door);
        }
        else if(size == 2)
        {
            Door door = new Door (id, name, switchState, doorStatus3, humidityX, temperatureCX, temperatureFX);
            databaseDoorsX.child(id).setValue(door);
        }
        else
        {
            Door door = new Door (id, name, switchState, "Not Available", humidityX, temperatureCX, temperatureFX);
            databaseDoorsX.child(id).setValue(door);
        }

        //Log.d(TAG, "Door Added Successfully");

        return true;
    }


    public List<Door> getDoorListFromDatabase()
    {

        databaseDoorsX.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                doorListX.clear();
                for(DataSnapshot doorSnapshot : dataSnapshot.getChildren())
                {
                    Door door = (Door) doorSnapshot.getValue(Door.class);
                    doorListX.add(door);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
        return doorListX;
    }

    public void setSensorInfo()
    {

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference databaseReferenceHumidity = database.getReference().child("Humidity");
        databaseReferenceHumidity.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                humidityX = dataSnapshot.getValue(Float.class).toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        DatabaseReference databaseReferenceTempC = database.getReference().child("TemperatureC");
        databaseReferenceTempC.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                temperatureCX = dataSnapshot.getValue(Float.class).toString();

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        DatabaseReference databaseReferenceTempF = database.getReference().child("TemperatureF");
        databaseReferenceTempF.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                temperatureFX = dataSnapshot.getValue(Float.class).toString();

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        DatabaseReference databaseReferenceDoor1 = database.getReference().child("Door1");
        databaseReferenceDoor1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                float temp = dataSnapshot.getValue(float.class);
                if (temp == 0)
                {
                    doorStatus1 = "Closed";
                }
                else
                {
                    doorStatus1 = "Open";
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        DatabaseReference databaseReferenceDoor2 = database.getReference().child("Door2");
        databaseReferenceDoor2.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                float temp = dataSnapshot.getValue(float.class);
                if (temp == 0)
                {
                    doorStatus2 = "Closed";
                }
                else
                {
                    doorStatus2 = "Open";
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        DatabaseReference databaseReferenceDoor3 = database.getReference().child("Door3");
        databaseReferenceDoor3.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                float temp = dataSnapshot.getValue(float.class);
                if (temp == 0)
                {
                    doorStatus3 = "Closed";
                }
                else
                {
                    doorStatus3 = "Open";
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

}