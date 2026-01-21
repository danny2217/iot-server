// 핀 설정
const int PIR_PIN = 3;  // 사용자가 선택한 3번 핀
const int LED_PIN = 13; // 아두이노 내장 LED (깜빡거림 확인용)

// 인터럽트에서 값을 바꾸는 변수는 volatile 필수!
volatile bool motionDetected = false;

void setup() {
  Serial.begin(9600); // 시리얼 통신 시작
  pinMode(PIR_PIN, INPUT);
  pinMode(LED_PIN, OUTPUT);

  // ⭐ 핵심: 3번 핀을 감시하다가 신호가 오면(RISING) detectMotion 함수 실행!
  attachInterrupt(digitalPinToInterrupt(PIR_PIN), detectMotion, RISING);
  
  Serial.println("PIR 센서 감시 시작! (초기 안정화 30초 대기 권장)");
}

void loop() {
  // 평소에는 아무것도 안 함 (다른 센서 처리 가능)
  
  if (motionDetected) {
    Serial.println("🚨 움직임 감지됨! (3번 핀)");
    
    // LED 켜서 눈으로 확인
    digitalWrite(LED_PIN, HIGH);
    delay(1000); // 1초 동안 켜짐
    digitalWrite(LED_PIN, LOW);
    
    motionDetected = false; // 다시 감시 모드로 복귀
  }
}

// 인터럽트 발생 시 실행되는 함수 (최대한 짧게!)
void detectMotion() {
  motionDetected = true;
}