package com.example.my_iot_app;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    // 서버의 주소 뒤에 붙을 상세 주소 (Endpoint)
    // 예: http://localhost:8080/api/sensor/latest
    // 주의: 작성자님 서버 컨트롤러에 이 주소가 있어야 합니다!
    @GET("/api/sensor/latest")
    Call<SensorData> getLastSensorData();

    //[추가] LED 제어 요청 (POST)
    @POST("/api/sensor/control")
    Call<String> controlLed(@Body CommandReq req);
}