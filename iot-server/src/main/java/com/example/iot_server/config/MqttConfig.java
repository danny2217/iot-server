package com.example.iot_server.config;

import com.example.iot_server.domain.SensorData;
import com.example.iot_server.service.SensorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.time.LocalDateTime;

@Configuration
public class MqttConfig {

    // 브로커 설정
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID_IN = "spring-boot-server";   // 수신용 ID
    private static final String CLIENT_ID_OUT = "spring-boot-publisher"; // 송신용 ID (새로 추가)
    private static final String TOPIC_IN = "iot/topic";   // 듣는 토픽 (센서 데이터)
    private static final String TOPIC_OUT = "led/control"; // 말하는 토픽 (LED 제어)

    @Autowired
    private SensorService sensorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1. 공장 (MQTT 연결 공장) - 공통 사용
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{BROKER_URL});
        factory.setConnectionOptions(options);
        return factory;
    }

    // ========================================================
    // [PART 1] 받는 곳 (Inbound) - 기존 코드 유지
    // ========================================================

    // 2-1. 받는 파이프 (채널)
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    // 2-2. 어댑터 (브로커 -> 받는 파이프 연결)
    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(CLIENT_ID_IN, mqttClientFactory(), TOPIC_IN);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    // 2-3. 처리 로직 (DB 저장)
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message -> {
            try {
                String payload = (String) message.getPayload();
                System.out.println("📩 [수신] Raw Data: " + payload);

                // JSON 파싱 & DB 저장
                SensorData data = objectMapper.readValue(payload, SensorData.class);
                data.setCreatedAt(LocalDateTime.now());
                sensorService.register(data);

                System.out.println("✅ DB 저장 성공! (온도: " + data.getTemperature() + ")");
            } catch (Exception e) {
                System.err.println("❌ 수신 에러: " + e.getMessage());
            }
        };
    }

    // ========================================================
    // [PART 2] 보내는 곳 (Outbound) - 🚨 새로 추가된 부분!
    // ========================================================

    // 3-1. 보내는 파이프 (채널) - 이름 중요: "mqttOutboundChannel"
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    // 3-2. 발사대 (보내는 파이프 -> 브로커 연결)
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel") // 파이프 이름 일치해야 함
    public MessageHandler mqttOutbound() {
        // "spring-boot-publisher" 라는 이름으로 브로커에 접속
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler(CLIENT_ID_OUT, mqttClientFactory());

        messageHandler.setAsync(true); // 비동기 전송 (빠름)
        messageHandler.setDefaultTopic(TOPIC_OUT); // 기본 토픽: led/control

        return messageHandler;
    }
}