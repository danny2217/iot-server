package com.example.iot_server.mqtt;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;

// "이 인터페이스를 통해 MQTT로 메시지를 보낼 거야"라고 선언
@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface MqttGateway {

    // 이 함수를 호출하면 topic으로 payload(메시지)가 날아감!
    void sendToMqtt(String payload, @Header(MqttHeaders.TOPIC) String topic);
}