package com.example.avkash.housecontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
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

public class controlActivity extends AppCompatActivity implements View.OnClickListener
{
    private FirebaseAuth firebaseAuth;

    private TextView textViewUserEmail;
    private Button logOutButton;
    private Button saveButton;
    private EditText optionText;
    private Switch toggleSwitch;

    DatabaseReference databaseDoors;
    ListView listViewDoors;
    ArrayList<Door> arrayList = new ArrayList<>();
    List<Door> doorList;
    String humidityX = null;
    String temperatureCX = null;
    String temperatureFX = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null)
        {
            finish();
            sendMessageToMainActivity();
        }

        FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseDoors = database.getReference("doors").child(user.getUid());

        logOutButton = (Button) findViewById(R.id.logOutButton);
        textViewUserEmail = (TextView) findViewById(R.id.textViewUserEmail);
        textViewUserEmail.setText("Welcome " + user.getDisplayName() + "\n" + user.getEmail());

        logOutButton.setOnClickListener(this);

        optionText = (EditText) findViewById(R.id.optionText);
        toggleSwitch =(Switch) findViewById(R.id.toggleSwitch);
        saveButton = (Button) findViewById(R.id.saveButton);

        listViewDoors = (ListView) findViewById(R.id.listViewDoors);

        doorList = new ArrayList<>();

        saveButton.setOnClickListener(new View.OnClickListener()
        {
           @Override
           public void onClick(View view)
           {
               addDoor();
           }

        });

        listViewDoors.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                doorList = getDoorListFromDatabase();
                Door door = doorList.get(i);
                showUpdateDialog(door.getId(), door.getName());
                return false;
            }
        });

    }




    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseDoors = database.getReference("doors").child(user.getUid());
        databaseDoors.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                doorList.clear();
                for(DataSnapshot doorSnapshot : dataSnapshot.getChildren())
                {
                    Door door = (Door) doorSnapshot.getValue(Door.class);
                    doorList.add(door);
                }
                DoorList arrayAdapter = new DoorList(controlActivity.this, doorList);
                //arrayAdapter.notifyDataSetChanged();
                listViewDoors.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
        setSensorInfo();
    }

    private void showUpdateDialog(final String doorID, final String doorName)
    {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = (EditText)  dialogView.findViewById(R.id.editTextName);
        final Button updateButton = (Button)  dialogView.findViewById(R.id.updateButton);
        final Switch statusSwitch = (Switch) dialogView.findViewById(R.id.statusSwitch);
        final Button cancelButton = (Button)  dialogView.findViewById(R.id.cancelButton);
        final Button deleteButton = (Button)  dialogView.findViewById(R.id.deleteButton);

        dialogBuilder.setTitle("Updating " + doorName + " Info");

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        updateButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {
                String name = editTextName.getText().toString().trim();
                boolean togg = statusSwitch.isChecked();

                if(TextUtils.isEmpty(name))
                {
                    name = doorName;
                }

                updateDoor(doorID, name, togg);
                alertDialog.dismiss();

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                alertDialog.dismiss();

            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                deleteDoor(doorID);
                alertDialog.dismiss();

            }
        });
    }

    private boolean updateDoor(String id, String name, boolean togg)
    {
        FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        int index = 0;
        setSensorInfo();

        if(getDoorListFromDatabase().get(index).getId().equals(id))
        {
            Door door = new Door(id, name, togg, humidityX, temperatureCX, temperatureFX);
            databaseDoors.child(id).setValue(door);
            Toast.makeText(this, "Door Updated Successfully", Toast.LENGTH_LONG).show();
        }
        else
        {
            Door door = new Door(id, name, togg, null, null, null);
            databaseDoors.child(id).setValue(door);
            Toast.makeText(this, "Door Updated Successfully", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    public List<Door> getDoorListFromDatabase()
    {
        FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseDoors = database.getReference("doors").child(user.getUid());
        databaseDoors.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                doorList.clear();
                for(DataSnapshot doorSnapshot : dataSnapshot.getChildren())
                {
                    Door door = (Door) doorSnapshot.getValue(Door.class);
                    doorList.add(door);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
        return doorList;
    }

    public void setSensorInfo()
    {
        FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference databaseReferenceHumidity = database.getReference().child("Humidity");
        databaseReferenceHumidity.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                // Door door = (Door) doorSnapshot.getValue(Door.class);
                humidityX = dataSnapshot.getValue(Float.class).toString();
                //door.setHumidity(humidityX);

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

                // Door door = (Door) doorSnapshot.getValue(Door.class);
                temperatureCX = dataSnapshot.getValue(Float.class).toString();
                //door.setTemperatureC(temperatureCX);

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

                // Door door = (Door) doorSnapshot.getValue(Door.class);
                temperatureFX = dataSnapshot.getValue(Float.class).toString();
                //door.setTemperatureF(temperatureFX);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }


    private boolean deleteDoor(String id)
    {
        FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();

        final int index = 0;
        DatabaseReference databaseDoorDelete = FirebaseDatabase.getInstance().getReference("doors").child(user.getUid()).child(id);
        if(getDoorListFromDatabase().get(index).getId() == id)
        {
            Toast.makeText(this, "Cannot be deleted. This door is auto connected to sensor(s).", Toast.LENGTH_LONG).show();
            return false;
        }
        else
        {
            databaseDoorDelete.removeValue();
            Toast.makeText(this, "Door Deleted Successfully", Toast.LENGTH_LONG).show();
            return true;
        }

    }

    private boolean addDoor()
    {
        String name = optionText.getText().toString().trim();
        boolean switchState = toggleSwitch.isChecked();
        FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseDoors = database.getReference("doors").child(user.getUid());

        setSensorInfo();

        if(!TextUtils.isEmpty(name))
        {
            String id = databaseDoors.push().getKey();

            if(getDoorListFromDatabase().size() == 0)
            {
                Door door = new Door (id, name, switchState, humidityX, temperatureCX, temperatureFX);
                databaseDoors.child(id).setValue(door);
            }
            else
            {
                Door door = new Door (id, name, switchState, null, null, null);
                databaseDoors.child(id).setValue(door);
            }

            Toast.makeText(this, "Door Added Successfully", Toast.LENGTH_LONG).show();

        }
        else
        {
            Toast.makeText(this, "You should enter a name", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    public void onClick(View view)
    {
        if (view == logOutButton)
        {
            firebaseAuth.signOut();
            finish();
            sendMessageToMainActivity();
        }
    }

    private void sendMessageToMainActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
