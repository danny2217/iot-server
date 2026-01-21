package com.example.iot_server.domain; // 본인 패키지명 확인!

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity // 1. "난 그냥 자바 객체가 아니라 DB 테이블이야"라고 선언 (SQLD 엔터티 개념!)
@Getter @Setter // 2. 롬복: 지루한 get/set 코드 자동 생성
@ToString
public class SensorData {

    @Id // 3. 식별자(PK): 주민등록번호 같은 역할
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 4. "번호는 1씩 자동으로 증가시켜줘" (Auto Increment)
    private Long id;

    private Double temperature; // 속성 1: 온도
    private Double humidity;    // 속성 2: 습도

    private LocalDateTime createdAt; // 속성 3: 측정 시간
}