package com.example.iot_server.service; // 또는 repository 패키지

import com.example.iot_server.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    // 이미 findById(userId)는 자동으로 만들어져 있음!
}