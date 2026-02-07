#include "DHT.h"

// 1. 핀 설정
#define DHTPIN 2       // 온습도 센서 (D2)
#define DHTTYPE DHT11
#define PIR_PIN 3      // 인체감지 센서 (D3)
#define LED_PIN 13     // 제어할 LED (D13)

DHT dht(DHTPIN, DHTTYPE);

// 2. 변수 설정
unsigned long lastTime = 0;   // 마지막으로 센서 보낸 시간
const long interval = 2000;   // 센서 측정 간격 (2초)

volatile int motionStatus = 0; // 움직임 감지 상태

// 인터럽트 함수 (사람 움직이면 1로 변경)
void detectMotion() {
  motionStatus = 1;
}

void setup() {
  // ⭐ 통신 속도 115200 (파이썬과 맞춤)
  Serial.begin(115200);
  
  dht.begin();
  pinMode(PIR_PIN, INPUT);
  pinMode(LED_PIN, OUTPUT);
  
  // PIR 센서는 인터럽트로 감시 (놓치지 않기 위해)
  attachInterrupt(digitalPinToInterrupt(PIR_PIN), detectMotion, RISING);
}

void loop() {
  // ==========================================
  // 1. [Down-link] LED 명령 수신 (항상 감시)
  // ==========================================
  if (Serial.available() > 0) {
    char command = Serial.read(); // 파이썬에서 보낸 글자 읽기
    
    if (command == '1') {
      digitalWrite(LED_PIN, HIGH); // LED 켜기
    } 
    else if (command == '0') {
      digitalWrite(LED_PIN, LOW);   // LED 끄기
    }
  }

  // ==========================================
  // 2. [Up-link] 센서 데이터 전송 (2초마다)
  // ==========================================
  unsigned long currentTime = millis(); // 현재 시간 가져오기
  
  if (currentTime - lastTime >= interval) {
    // 2초가 지났다면 실행
    lastTime = currentTime; // 시간 갱신

    float h = dht.readHumidity();
    float t = dht.readTemperature();

    // 센서값 에러 처리
    if (isnan(h)) h = 0.0;
    if (isnan(t)) t = 0.0;

    // ⭐ CSV 포맷으로 전송 (습도,온도,움직임)
    // 예시: 45.2,26.5,1
    Serial.print(h);
    Serial.print(",");
    Serial.print(t);
    Serial.print(",");
    Serial.println(motionStatus);

    // 움직임 상태 초기화 (다음 감지 위해)
    motionStatus = 0; 
  }
}