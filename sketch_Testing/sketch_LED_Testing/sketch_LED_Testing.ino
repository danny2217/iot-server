int ledPin = 13; // 아두이노에 내장된 LED 핀 번호

void setup() {
  pinMode(ledPin, OUTPUT); // 13번 핀을 출력용으로 설정
  Serial.begin(115200);      // USB 통신 속도 설정 (파이썬이랑 맞춰야 함)
}

void loop() {
  // USB(Serial)로 뭔가 데이터가 들어왔다면?
  if (Serial.available() > 0) {
    char command = Serial.read(); // 들어온 글자 하나 읽기

    if (command == '1') {
      digitalWrite(ledPin, HIGH); // LED 켜기
    } 
    else if (command == '0') {
      digitalWrite(ledPin, LOW);  // LED 끄기
    }
  }
}