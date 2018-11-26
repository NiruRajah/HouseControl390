package com.example.avkash.housecontrol;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class controlActivity extends AppCompatActivity implements View.OnClickListener {


    private TextView textViewUserEmail;
    private Button logOutButton;
    private FloatingActionButton floatingAddButton;
    private TextView humidityText;
    private TextView temperatureText;
    private Switch alertSwitch;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference databaseDoors;
    private FirebaseDatabase database;
    private ListView listViewDoors;
    private List<Door> doorList;
    private String humidityX = null;
    private String temperatureCX = null;
    private String temperatureFX = null;
    private String doorStatus1 = null;
    private String doorStatus2 = null;
    private String doorStatus3 = null;
    private boolean tempType = true;
    private boolean motionDetection = false;
    private String userID = null;
    private boolean tempAlert = false;
    private boolean fireAlert = false;
    private boolean tempMessage = false;
    private boolean fireMessage = false;
    private boolean textMsgAlert = false;
    private boolean MsgCounter = false;
    //boolean door1Alert = false;
    //boolean door2Alert = false;
    //boolean door3Alert = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        firebaseAuth = FirebaseAuth.getInstance();


        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            sendMessageToMainActivity();
        }

        user = firebaseAuth.getInstance().getCurrentUser();
        userID = firebaseAuth.getInstance().getCurrentUser().toString().trim();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseDoors = database.getReference("doors").child(user.getUid());
        doorList = new ArrayList<>();
        logOutButton = (Button) findViewById(R.id.logOutButton);
        textViewUserEmail = (TextView) findViewById(R.id.textViewUserEmail);
        textViewUserEmail.setText("Welcome " + user.getDisplayName() + "\n " + user.getEmail());
        floatingAddButton = (FloatingActionButton) findViewById(R.id.floatingAddButton);
        listViewDoors = (ListView) findViewById(R.id.listViewDoors);
        humidityText = (TextView) findViewById(R.id.humidityText);
        temperatureText = (TextView) findViewById(R.id.temperatureText);
        alertSwitch = (Switch) findViewById(R.id.alertSwitch);


        logOutButton.setOnClickListener(this);

        floatingAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AddDoorFragment dialog = new AddDoorFragment();
                dialog.show(getSupportFragmentManager(), "Add Door");

            }

        });


        listViewDoors.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                doorList = getDoorListFromDatabase();
                Door door = doorList.get(i);
                showUpdateDialog(door.getId(), door.getName(), i);
                return false;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getInstance().getCurrentUser();

        setSensorInfo();
        updateAllDoors();
        DatabaseReference databaseDoors = FirebaseDatabase.getInstance().getReference("doors").child(user.getUid());

        DatabaseReference databaseChangeDetection = FirebaseDatabase.getInstance().getReference();
        databaseChangeDetection.addValueEventListener(new ValueEventListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                checkMotion();
                temperatureUpdate();
                doorAlert();
                updateAllDoors();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseDoors.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                doorList.clear();
                for (DataSnapshot doorSnapshot : dataSnapshot.getChildren()) {
                    Door door = (Door) doorSnapshot.getValue(Door.class);
                    doorList.add(door);
                }

                DoorList arrayAdapter = new DoorList(controlActivity.this, doorList);

                listViewDoors.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMsg(String message)
    {

        if (textMsgAlert) {
            SmsManager sms = SmsManager.getDefault();
            TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            //String phoneNo = "15148030253";
            String phoneNo = sms.toString().trim();
            //phoneNo = tManager.getLine1Number();
            sms.sendTextMessage(phoneNo, null, message, null, null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void pushNotification(String subject, String body)
    {
        String title="Home Control";
        //String subject="Alert";
        //String body="Door Open";

        Intent intent = new Intent(this, controlActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationManager notif=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int importance = NotificationManager.IMPORTANCE_HIGH;

        Notification notify=new Notification.Builder
                (getApplicationContext()).setContentTitle(title).setContentText(body).
                setContentTitle(subject).setSmallIcon(R.drawable.main_activity_emblem).setContentIntent(pendingIntent).setVibrate(new long[]{400, 400, 400}).build();
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        notif.notify(0, notify);


        //NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        //notificationBuilder.setContentTitle("FCM NOTIFICATION");
        //notificationBuilder.setContentText(remoteMessage.getNotification().getBody());
        //notificationBuilder.setAutoCancel(true);
        //notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        //notificationBuilder.setContentIntent(pendingIntent);
        //NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //notificationManager.notify(0,notificationBuilder.build());
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void temperatureUpdate()
    {
        doorList = getDoorListFromDatabase();
        if (tempType)
        {
            humidityText.setText("Humidity:\n" + humidityX + " %");
            temperatureText.setText("Temperature:\n" + temperatureCX + " °C");
            if (tempAlert && (!(tempMessage)))
            {
                //Toast.makeText(this, "HEAT WARNING!!! Temperature Above 35 °C", Toast.LENGTH_LONG).show();
                tempAlert = false;
                tempMessage = true;
                pushNotification("HEAT WARNING","Temperature Above 35 °C");
                sendMsg("HOME CONTROL:\nHEAT WARNING!!! Temperature Above 35 °C");
            }
            if (fireAlert && (!(fireMessage)))
            {
                //Toast.makeText(this, "POTENTIAL FIRE WARNING!!! Temperature Above 80 °C", Toast.LENGTH_LONG).show();
                fireAlert = false;
                tempAlert = false;
                tempMessage = true;
                fireMessage = true;
                pushNotification("POTENTIAL FIRE WARNING","Temperature Above 80 °C");
                sendMsg("HOME CONTROL:\nPOTENTIAL FIRE WARNING!!! Temperature Above 80 °C");
            }

        }
        else
        {
            humidityText.setText("Humidity:\n" + humidityX + " %");
            temperatureText.setText("Temperature:\n" + temperatureFX + " °F");
            if (tempAlert && (!(tempMessage)))
            {
                //Toast.makeText(this, "HEAT WARNING!!! Temperature Above 95 °F", Toast.LENGTH_LONG).show();
                tempAlert = false;
                tempMessage = true;
                pushNotification("HEAT WARNING","Temperature Above 95 °F");
                sendMsg("HOME CONTROL:\nHEAT WARNING!!! Temperature Above 95 °F °C");
            }
            if (fireAlert && (!(fireMessage)))
            {
                //Toast.makeText(this, "POTENTIAL FIRE WARNING!!! Temperature Above 176 °F", Toast.LENGTH_LONG).show();
                fireAlert = false;
                tempAlert = false;
                tempMessage = true;
                fireMessage = true;
                pushNotification("POTENTIAL FIRE WARNING","Temperature Above 176 °F");
                sendMsg("HOME CONTROL:\nPOTENTIAL FIRE WARNING!!! Temperature Above 176 °F");
            }
        }
    }


    private void checkMotion()
    {
        DatabaseReference motionReference = FirebaseDatabase.getInstance().getReference().child("PIRMotion");
        motionReference.addValueEventListener(new ValueEventListener()
        {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                float motion = dataSnapshot.getValue(float.class);
                setMotionDetection(motion);
                motionDetector();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void setMotionDetection(float motion)
    {
        if (motion == 1)
        {
            motionDetection = true;
        }
        else
        {
            motionDetection = false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void motionDetector()
    {
        boolean alert = alertSwitch.isChecked();
        if ((alert) && (motionDetection))
        {
            //Toast.makeText(this, "SECURITY ALERT!!! MOTION DETECTED!!!", Toast.LENGTH_LONG).show();
            sendMsg("HOME CONTROL:\nSECURITY ALERT!!! MOTION DETECTED!!!");
            pushNotification("SECURITY ALERT","MOTION DETECTED!!!");

            alertSwitch.setChecked(false);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void doorAlert()
    {
        String door1 = null;
        String door2 = null;
        String door3 = null;


        doorList = getDoorListFromDatabase();
        int size = doorList.size();
        for (int i = 0; i < size; i++)
        {
            if(i==0)
            {
                door1 = doorList.get(i).getDoorStatus();
            }
            if(i==1)
            {
                door2 = doorList.get(i).getDoorStatus();
            }
            if(i==2)
            {
                door3 = doorList.get(i).getDoorStatus();
            }

        }
       //setSensorInfo();

        boolean door1Alert = false;
        boolean door2Alert = false;
        boolean door3Alert =false;

        if(!(doorStatus1.equals(door1)))
        {
            door1Alert = true;
        }
        if(!(doorStatus2.equals(door2)))
        {
            door2Alert = true;
        }
        if(!(doorStatus3.equals(door3)))
        {
            door3Alert = true;
        }

        //doorList = getDoorListFromDatabase();
        size = doorList.size();

        for (int i = 0; i < size; i++)
        {
            if((door1Alert) && i ==0 && (doorList.get(i).getToggle()))
            {
                door1Alert = false;
                //Toast.makeText(this, "SECURITY ALERT!!! " + doorList.get(0).getName() + " just changed position to " + doorStatus1, Toast.LENGTH_SHORT).show();
                pushNotification("SECURITY ALERT",doorList.get(0).getName() + " just changed position to " + doorStatus1);
                sendMsg("HOME CONTROL:\nSECURITY ALERT!!! " + doorList.get(0).getName() + " just changed position to " + doorStatus1);
            }
            if((door2Alert) && i==1 && (doorList.get(i).getToggle()))
            {
                door2Alert = false;
                //Toast.makeText(this, "SECURITY ALERT!!! " + doorList.get(1).getName() + " just changed position to " + doorStatus2, Toast.LENGTH_SHORT).show();
                pushNotification("SECURITY ALERT",doorList.get(1).getName() + " just changed position to " + doorStatus2);
                sendMsg("HOME CONTROL:\nSECURITY ALERT!!! " + doorList.get(1).getName() + " just changed position to " + doorStatus2);
            }
            if((door3Alert) && i==2 && (doorList.get(i).getToggle()))
            {
                door3Alert = false;
                //Toast.makeText(this, "SECURITY ALERT!!! " + doorList.get(2).getName() + " just changed position to " + doorStatus3, Toast.LENGTH_SHORT).show();
                pushNotification("SECURITY ALERT",doorList.get(2).getName() + " just changed position to " + doorStatus3);
                sendMsg("HOME CONTROL:\nSECURITY ALERT!!! " + doorList.get(2).getName() + " just changed position to " + doorStatus3);
            }
        }


    }


    private void showUpdateDialog(final String doorID, final String doorName, final int position)
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
                updateDoor(doorID, name, position, togg);
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

                deleteDoor(doorID, position);
                doorList = getDoorListFromDatabase();
                alertDialog.dismiss();

            }
        });
    }

    private boolean updateDoor(String id, String name, int position, boolean togg)
    {
        DatabaseReference databaseDoorsX = FirebaseDatabase.getInstance().getReference("doors").child(user.getUid());
        setSensorInfo();

        if(position == 0)
        {
            Door door = new Door (id, name, togg, doorStatus1, humidityX, temperatureCX, temperatureFX);
            databaseDoorsX.child(id).setValue(door);

        }
        else if (position == 1)
        {
            Door door = new Door (id, name, togg, doorStatus2, humidityX, temperatureCX, temperatureFX);
            databaseDoorsX.child(id).setValue(door);
        }
        else if(position == 2)
        {
            Door door = new Door (id, name, togg, doorStatus3, humidityX, temperatureCX, temperatureFX);
            databaseDoorsX.child(id).setValue(door);
        }
        else
        {
            Door door = new Door (id, name, togg, "Not Available", humidityX, temperatureCX, temperatureFX);
            databaseDoorsX.child(id).setValue(door);
        }
        Toast.makeText(this, "Door Updated Successfully", Toast.LENGTH_LONG).show();
        return true;
    }


    private boolean updateAllDoors()
    {
        //user = firebaseAuth.getInstance().getCurrentUser();
        doorList = getDoorListFromDatabase();
        DatabaseReference databaseUpdateAllDoors = FirebaseDatabase.getInstance().getReference("doors");
        String idTemp = null;
        String nameTemp = null;
        boolean toggleTemp = false;

        int size = doorList.size();

        for (int i = 0; i < size; i++)
        {
            idTemp = doorList.get(i).getId();
            nameTemp = doorList.get(i).getName();
            toggleTemp = doorList.get(i).getToggle();

            if(i == 0)
            {
                Door door = new Door (idTemp, nameTemp, toggleTemp, doorStatus1, humidityX, temperatureCX, temperatureFX);
                databaseUpdateAllDoors.child(user.getUid()).child(idTemp).setValue(door);
            }
            else if (i == 1)
            {
                Door door = new Door (idTemp, nameTemp, toggleTemp, doorStatus2, humidityX, temperatureCX, temperatureFX);
                databaseUpdateAllDoors.child(user.getUid()).child(idTemp).setValue(door);
            }
            else if(i == 2)
            {
                Door door = new Door (idTemp, nameTemp, toggleTemp, doorStatus3, humidityX, temperatureCX, temperatureFX);
                databaseUpdateAllDoors.child(user.getUid()).child(idTemp).setValue(door);
            }
            else
            {
                Door door = new Door (idTemp, nameTemp, toggleTemp, "Not Available", humidityX, temperatureCX, temperatureFX);
                databaseUpdateAllDoors.child(user.getUid()).child(idTemp).setValue(door);
            }

        }
        return true;
    }

    private List<Door> getDoorListFromDatabase()
    {
        user = firebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseGetDoorList = FirebaseDatabase.getInstance().getReference("doors").child(user.getUid());
        databaseGetDoorList.addValueEventListener(new ValueEventListener()
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

    private void setSensorInfo()
    {

        DatabaseReference databaseReferenceHumidity = FirebaseDatabase.getInstance().getReference().child("Humidity");
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

        DatabaseReference databaseReferenceTempC = FirebaseDatabase.getInstance().getReference().child("TemperatureC");
        databaseReferenceTempC.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                temperatureCX = dataSnapshot.getValue(Float.class).toString();
                float temperature = dataSnapshot.getValue(Float.class);
                float warningTemp = 35;
                float unwarningTemp = 30;
                float fireTemp = 80;
                float unwarningFire = 50;
                if(temperature >= warningTemp)
                {
                    tempAlert = true;
                }
                if(temperature <= unwarningTemp)
                {
                    tempMessage = false;
                    tempAlert = false;
                    fireAlert = false;
                    fireMessage = false;
                }
                if(temperature >= fireTemp)
                {
                    fireAlert = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        DatabaseReference databaseReferenceTempF = FirebaseDatabase.getInstance().getReference().child("TemperatureF");
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
        DatabaseReference databaseReferenceDoor1 = FirebaseDatabase.getInstance().getReference().child("Door1");
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

        DatabaseReference databaseReferenceDoor2 = FirebaseDatabase.getInstance().getReference().child("Door2");
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
        DatabaseReference databaseReferenceDoor3 = FirebaseDatabase.getInstance().getReference().child("Door3");
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


    private boolean deleteDoor(String id, int position)
    {
        user = firebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseDoorDelete = FirebaseDatabase.getInstance().getReference("doors").child(user.getUid()).child(id);

        if ((position < 3) && (getDoorListFromDatabase().get(position).getId() == id))
        {
            doorList = getDoorListFromDatabase();
            Toast.makeText(this, "Cannot be deleted. This door is auto connected to sensor(s).", Toast.LENGTH_LONG).show();
            return false;
        }
        else
        {
            doorList = getDoorListFromDatabase();
            databaseDoorDelete.removeValue();
            Toast.makeText(this, "Door Deleted Successfully", Toast.LENGTH_LONG).show();
            return true;
        }


    }

    @Override
    public void onClick(View view)
    {
        if (view == logOutButton)
        {

            doorList.clear();
            FirebaseAuth.getInstance().signOut();
            finish();
            sendMessageToMainActivity();
        }
    }



    private void sendMessageToMainActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.celsius:
            {
                tempType = true;
                temperatureUpdate();
                Toast.makeText(this, "Celsius Clicked", Toast.LENGTH_SHORT).show();
                break;

            }

            case R.id.fahrenheit:
            {
                tempType = false;
                temperatureUpdate();
                Toast.makeText(this, "Fahrenheit Clicked", Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.textAlertOn:
            {
                textMsgAlert = true;
                Toast.makeText(this, "Text Message Alert Turned On", Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.textAlertOff:
            {
                textMsgAlert = false;
                Toast.makeText(this, "Text Message Alert Turned Off", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        return true;
    }




}
