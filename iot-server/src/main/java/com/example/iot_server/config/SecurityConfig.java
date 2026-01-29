package com.example.iot_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 끄기 (앱/IoT 통신 필수)
                .csrf(csrf -> csrf.disable())

                // 2. 주소별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 로그인, 회원가입은 누구나 들어올 수 있게 열어줌
                        .requestMatchers("/api/login", "/api/register").permitAll()

                        // 센서 데이터 수신도 일단 열어줌 (나중에 IoT 인증 추가 가능)
                        .requestMatchers("/api/sensor/**").permitAll()

                        // 그 외(LED 제어 등)는 무조건 로그인한 사람만!
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}