#include "DHT.h"

// 핀 설정
#define DHTPIN 2     // 데이터 핀을 2번에 연결
#define DHTTYPE DHT11   // 우리는 DHT11 센서를 씀

// 센서 객체 생성 (싱글톤 아님, 그냥 전역 객체)
DHT dht(DHTPIN, DHTTYPE);

void setup() {
  // 1. 시리얼 통신 시작 (속도: 9600bps) -> 이거 안 맞으면 글자 깨짐!
  Serial.begin(9600);
  
  // 2. 센서 시작
  dht.begin();
  
  Serial.println("DHT11 센서 테스트를 시작합니다...");
}

void loop() {
  // 2초 대기 (DHT11은 반응 속도가 느려서 너무 자주 읽으면 에러 남)
  delay(2000);

  // 값 읽기
  float h = dht.readHumidity();    // 습도
  float t = dht.readTemperature(); // 온도

  // 값이 잘 들어왔는지 확인 (NaN: Not a Number)
  if (isnan(h) || isnan(t)) {
    Serial.println("센서 읽기 실패! 선 연결을 확인하세요.");
    return;
  }

  // 화면(시리얼 모니터)에 출력
  // 이 형식이 나중에 라즈베리 파이가 파싱할 데이터 형식이 됩니다.
  Serial.print("습도: ");
  Serial.print(h);
  Serial.print(" %\t");
  Serial.print("온도: ");
  Serial.print(t);
  Serial.println(" *C");
}