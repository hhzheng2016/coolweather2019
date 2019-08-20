package com.weather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {
    public String status;
    public Basic Basic;
    public AQI AQI;
    public Now now;
    public Suggestion suggestion;

    @SerializedName("daily_forcast")
    public List<Forecast> forecastList;
}
