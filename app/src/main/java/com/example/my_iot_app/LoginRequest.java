package com.example.my_iot_app;

public class LoginRequest {
    //중요: 서버쪽 LoginRequest DTO의 변수명과 토씨 하나 안 틀리고 똑같아야 함
    private String userId;
    private String password;

    // 생성자 (Constructor)
    public LoginRequest(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }
}