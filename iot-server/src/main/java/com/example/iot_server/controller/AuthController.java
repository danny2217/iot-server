package com.example.iot_server.controller;

import com.example.iot_server.domain.User;
import com.example.iot_server.dto.LoginRequest;
import com.example.iot_server.service.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    // 1. 회원가입 (테스트용 유저 만들기 위해)
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        userRepository.save(user);
        return "회원가입 성공! (ID: " + user.getUserId() + ")";
    }

    // 2. 로그인 (핵심 로직)
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        // (1) DB에서 아이디로 유저 찾기
        User user = userRepository.findById(request.getUserId()).orElse(null);

        // (2) 아이디가 없으면?
        if (user == null) {
            return "FAIL: 존재하지 않는 아이디";
        }

        // (3) 비밀번호 비교 (단순 문자열 비교)
        if (!user.getPassword().equals(request.getPassword())) {
            return "FAIL: 비밀번호 불일치";
        }

        // (4) 성공 시 "가짜 토큰" 반환 (나중에 진짜 JWT로 바꿀 예정)
        return "SUCCESS_TOKEN_" + user.getUserId();
    }
}