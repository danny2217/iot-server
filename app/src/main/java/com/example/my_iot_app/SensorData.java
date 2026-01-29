package com.example.my_iot_app; // 패키지명 확인

import com.google.gson.annotations.SerializedName;

public class SensorData {
    // 1. 온도
    @SerializedName("temperature")
    private double temperature;

    // 2. 습도
    @SerializedName("humidity")
    private double humidity;

    // 3. 모션 (추가됨! 0:안전, 1:감지)
    @SerializedName("motion")
    private int motion;

    // 4. 시간 (추가됨! 서버에서 보낸 시간)
    @SerializedName("createdAt")
    private String createdAt;

    // --- Getter (꺼내는 도구) ---
    public double getTemperature() { return temperature; }
    public double getHumidity() { return humidity; }
    public int getMotion() { return motion; } // 핵심!
    public String getCreatedAt() { return createdAt; }
}