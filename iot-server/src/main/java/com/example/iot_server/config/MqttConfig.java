package com.example.iot_server.config;

import com.example.iot_server.domain.SensorData;
import com.example.iot_server.service.SensorService;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
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

    // ========================================================
    // 1. MQTT 설정 변수 (내 환경에 맞게 수정)
    // ========================================================
    // 라즈베리파이 IP가 맞는지 꼭 확인하세요!
    // 이렇게 바꾸세요!
    private static final String BROKER_URL = "tcp://localhost:1883";;

    private static final String CLIENT_ID_IN = "spring-boot-server";     // 서버 수신용 ID
    private static final String CLIENT_ID_OUT = "spring-boot-publisher"; // 서버 송신용 ID

    // 🚨 [중요] 파이썬 코드의 토픽과 글자 하나라도 틀리면 안 됨!
    private static final String TOPIC_IN = "sensor/data";   // 파이썬 -> 서버 (데이터 수신)
    private static final String TOPIC_OUT = "led/control";  // 서버 -> 파이썬 (LED 제어)

    // ========================================================
    // 2. MQTT 연결 공장 (Connection Factory)
    // ========================================================
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{BROKER_URL});
        // options.setUserName("username"); // 필요하다면 설정
        // options.setPassword("password".toCharArray());
        factory.setConnectionOptions(options);
        return factory;
    }

    // ========================================================
    // [PART 3] 받는 곳 (Inbound) - 센서 데이터 수신 및 DB 저장
    // ========================================================

    // 3-1. 받는 파이프 (채널)
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    // 3-2. 어댑터 (브로커와 파이프 연결)
    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(CLIENT_ID_IN, mqttClientFactory(), TOPIC_IN);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    // 3-3. 메시지 처리기 (Handler)
    // 💡 여기서 SensorService를 파라미터로 주입받습니다! (필드 선언 X)
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler(SensorService sensorService) {
        return message -> {
            // 1. 메시지 내용 꺼내기 (String)
            String payload = (String) message.getPayload();
            // 로그 찍기 (잘 들어오는지 확인용)
            // System.out.println("📩 [수신] Raw Data: " + payload);

            try {
                // 2. CSV 파싱 (콤마로 자르기)
                // 예: "45.2,26.5,1" -> ["45.2", "26.5", "1"]
                String[] parts = payload.split(",");

                if(parts.length == 3) {
                    Double humidity = Double.parseDouble(parts[0]);
                    Double temperature = Double.parseDouble(parts[1]);
                    int motion = Integer.parseInt(parts[2]);

                    // 3. SensorData 객체 생성
                    SensorData data = new SensorData();
                    data.setHumidity(humidity);
                    data.setTemperature(temperature);
                    data.setMotion(motion);

                    // 🚨 Entity의 날짜 필드명에 맞춰주세요 (createdAt vs timestamp)
                    data.setCreatedAt(LocalDateTime.now());

                    // 4. Service를 통해 DB에 저장
                    sensorService.register(data);

                    System.out.println("✅ DB 저장 성공! (온도: " + temperature + " / 동작: " + motion + ")");
                }
            } catch (Exception e) {
                System.err.println("❌ 데이터 처리 에러 (형식이 맞지 않음): " + payload);
                // e.printStackTrace(); // 자세한 에러 보고 싶으면 주석 해제
            }
        };
    }

    // ========================================================
    // [PART 4] 보내는 곳 (Outbound) - 앱 명령 -> LED 제어
    // ========================================================

    // 4-1. 보내는 파이프 (채널)
    // 이름("mqttOutboundChannel")이 Controller에서 호출할 때 쓰입니다.
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    // 4-2. 발사대 (브로커로 전송)
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler(CLIENT_ID_OUT, mqttClientFactory());

        messageHandler.setAsync(true); // 비동기 전송 (서버 안 멈추게)
        messageHandler.setDefaultTopic(TOPIC_OUT); // 기본 토픽 설정

        return messageHandler;
    }
}