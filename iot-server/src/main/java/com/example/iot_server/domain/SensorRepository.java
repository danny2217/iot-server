package com.example.iot_server.domain;

import com.example.iot_server.domain.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorRepository extends JpaRepository<SensorData, Long> {

    // ⬇️⬇️ 이 줄이 없어서 빨간 줄이 뜨는 겁니다! 꼭 넣어주세요! ⬇️⬇️
    SensorData findTopByOrderByCreatedAtDesc();

}