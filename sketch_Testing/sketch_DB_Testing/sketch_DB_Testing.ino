#include "DHT.h"
#define DHTPIN 2       
#define DHTTYPE DHT11
#define PIR_PIN 3      

DHT dht(DHTPIN, DHTTYPE);
volatile int motionStatus = 0; 

void detectMotion() {
  motionStatus = 1;
}

void setup() {
  // ⭐ 속도를 115200으로 변경 (가장 안정적)
  Serial.begin(115200);
  
  dht.begin();
  pinMode(PIR_PIN, INPUT);
  attachInterrupt(digitalPinToInterrupt(PIR_PIN), detectMotion, RISING);
}

void loop() {
  delay(2000); 

  float h = dht.readHumidity();
  float t = dht.readTemperature();

  // 값 없으면 0.0으로라도 보내기 (포맷 유지)
  if (isnan(h)) h = 0.0;
  if (isnan(t)) t = 0.0;

  // 데이터 전송
  Serial.print(h);
  Serial.print(",");
  Serial.print(t);
  Serial.print(",");
  Serial.println(motionStatus);

  motionStatus = 0; 
}