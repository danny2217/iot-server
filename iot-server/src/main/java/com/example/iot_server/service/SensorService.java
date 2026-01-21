package com.example.iot_server.service;

import com.example.iot_server.domain.SensorData;
import com.example.iot_server.domain.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // "나 이제 서비스(지배인)야" 선언
public class SensorService {

    @Autowired
    private SensorRepository sensorRepository; // 창고지기 데려오기

    // 저장 로직 (나중에 "온도 50도 넘으면 알람 보내!" 같은 코드가 여기 들어감)
    public SensorData register(SensorData data) {
        return sensorRepository.save(data);
    }

    // 조회 로직
    public List<SensorData> findAll() {
        return sensorRepository.findAll();
    }
    public SensorData getLatestData() {
        return sensorRepository.findTopByOrderByCreatedAtDesc();
    }
}