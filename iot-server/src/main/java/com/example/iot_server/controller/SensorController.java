package com.example.iot_server.controller;

import com.example.iot_server.domain.SensorData;
import com.example.iot_server.service.SensorService;
import com.example.iot_server.domain.CommandReq;
import com.example.iot_server.mqtt.MqttGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; //
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sensor")
public class SensorController {

    @Autowired
    private SensorService sensorService;

    @Autowired
    private MqttGateway mqttGateway; // 발사대 주입

    // 1. 기존 코드 (데이터 저장)
    @PostMapping
    public SensorData saveSensor(@RequestBody SensorData data) {
        data.setCreatedAt(LocalDateTime.now());
        return sensorService.register(data);
    }

    // 2. 기존 코드 (전체 목록 조회)
    @GetMapping
    public List<SensorData> getAllSensors() {
        return sensorService.findAll();
    }

    @GetMapping("/latest")
    public ResponseEntity<SensorData> getLatest() {
        // 서비스한테 "제일 최신 거 하나 줘!" 라고 시킴
        SensorData data = sensorService.getLatestData();

        // 데이터가 없으면(DB가 비었으면) 0.0도라도 보내줌 (앱 꺼짐 방지)
        if (data == null) {
            SensorData dummy = new SensorData();
            dummy.setTemperature(0.0);
            dummy.setHumidity(0.0);
            return ResponseEntity.ok(dummy);
        }

        return ResponseEntity.ok(data);
    }
    @PostMapping("/control")
    public String controlLed(@RequestBody CommandReq req) {
        String cmd = req.getCommand(); // "ON" 또는 "OFF"

        System.out.println("📱 앱에서 명령 도착: " + cmd);

        // MQTT로 발사! (토픽: led/control, 메시지: ON 또는 OFF)
        mqttGateway.sendToMqtt(cmd, "led/control");

        return "명령 전송 완료: " + cmd;
    }
}