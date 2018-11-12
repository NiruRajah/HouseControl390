package com.example.avkash.housecontrol;

public class Door
{
    String id;
    String name;
    boolean toggle;
    String humidity;
    String temperatureC;
    String temperatureF;


    public Door()
    {
        id = null;
        name = null;
        toggle = false;
        humidity = null;
        temperatureC = null;
        temperatureF = null;

    }


    public Door(String idx, String nam, boolean state, String hum, String tempC, String tempF)
    {
        id = idx;
        name = nam;
        toggle = state;
        humidity = hum;
        temperatureC = tempC;
        temperatureF = tempF;
    }

    public String getName() {
        return name;
    }

    public void setName(String nam) {
        this.name = nam;
    }

    public boolean getToggle()
    {
        return toggle;
    }

    public void setToggle(boolean tog)
    {
        this.toggle = tog;
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
