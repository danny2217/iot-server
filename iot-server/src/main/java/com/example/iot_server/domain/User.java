package com.example.iot_server.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class User {
    @Id
    private String userId;  // 아이디 (PK)
    private String password; // 비밀번호
    private String name;     // 사용자 이름
}