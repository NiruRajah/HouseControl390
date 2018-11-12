package com.example.avkash.housecontrol;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DoorList extends ArrayAdapter<Door>
{
    private Activity context;
    private List<Door> doorList;



    public DoorList (Activity context, List<Door> doorList)
    {
        super(context, R.layout.list_layout, doorList);
        this.context = context;
        this.doorList = new ArrayList<>(doorList);
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.list_layout, null, true);

        TextView textViewName = (TextView) listViewItem.findViewById(R.id.textViewName);
        TextView textViewStatus = (TextView) listViewItem.findViewById(R.id.textViewStatus);

        Door door = doorList.get(position);
        String status = String.valueOf(door.getToggle());
        String humid = door.getHumidity();
        String tempC = door.getTemperatureC();
        String tempF = door.getTemperatureF();


        if (status == "true")
        {
            status = "Door: Open\nHumidity: "+ humid + "\nTemperature in Celsius: " + tempC + "\nTemperature in Farenheits: "
            + tempF;
        }
        else
            {
            status = "Door: Closed\nHumidity: " + humid + "\nTemperature in Celsius: " + tempC + "\nTemperature in Farenheits: "
                    + tempF;
            }


        textViewName.setText(door.getName());
        textViewStatus.setText(status);

        return listViewItem;
    }


}
