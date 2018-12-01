package com.example.avkash.housecontrol;

public class Door //door class
{
    String id;
    String name;
    boolean alert;
    String doorStatus;
    String humidity;
    String temperatureC;
    String temperatureF;


    public Door() //door contructor
    {
        id = null;
        name = null;
        alert = false;
        doorStatus = null;
        humidity = null;
        temperatureC = null;
        temperatureF = null;

    }


    public Door(String idx, String nam, boolean togg, String state, String hum, String tempC, String tempF) //door construction assigning variables
    {
        id = idx;
        name = nam;
        alert = togg;
        doorStatus = state;
        humidity = hum;
        temperatureC = tempC;
        temperatureF = tempF;
    }

    //below are settings and getters
    public String getName() {
        return name;
    }

    public void setName(String nam) {
        this.name = nam;
    }

    public String getDoorStatus()
    {
        return doorStatus;
    }

    public void setDoorStatus(String doorStatus) {
        this.doorStatus = doorStatus;
    }

    public boolean getToggle()
    {
        return alert;
    }

    public void setToggle(boolean toggle)
    {
        this.alert = toggle;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String idx)
    {
        this.id = idx;
    }

    public String getHumidity()
    {
        return humidity;
    }

    public void setHumidity(String hum)
    {
        this.humidity = hum;
    }

    public String getTemperatureC()
    {
        return temperatureC;
    }

    public void setTemperatureC(String tempC)
    {
        this.temperatureC = tempC;
    }

    public String getTemperatureF()
    {
        return temperatureF;
    }

    public void setTemperatureF(String tempF)
    {
        this.temperatureF = tempF;
    }

}
