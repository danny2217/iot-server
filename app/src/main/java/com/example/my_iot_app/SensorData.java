package com.example.my_iot_app;

import com.google.gson.annotations.SerializedName;

public class SensorData {
    // 서버의 JSON 키값("temperature")과 똑같아야 함!
    @SerializedName("temperature")
    private double temperature;

    @SerializedName("humidity")
    private double humidity;

    // Getter (값을 꺼내는 도구)
    public double getTemperature() { return temperature; }
    public double getHumidity() { return humidity; }
}