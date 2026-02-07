import paho.mqtt.client as mqtt
import serial
import time

# ==========================================
# 1. 설정 (여기를 본인 환경에 맞게 수정!)
# ==========================================
BROKER_ADDRESS = "localhost"  # 맥북에서 도커가 돌고 있으니까 localhost
TOPIC = "led/control"         # 앱에서 보내는 토픽

# 🚨 아까 터미널에서 찾은 주소를 여기에 넣으세요!
PORT = "/dev/cu.usbmodemFX2348N1" 
BAUDRATE = 115200               # 아두이노랑 속도 맞춤

# ==========================================
# 2. 아두이노 연결 (Serial)
# ==========================================
try:
    arduino = serial.Serial(PORT, BAUDRATE)
    print(f"🔌 아두이노 연결 성공! ({PORT})")
    time.sleep(2) # 연결 후 잠시 대기 (아두이노 리셋 방지)
except Exception as e:
    print(f"❌ 아두이노 연결 실패: {e}")
    exit()

# ==========================================
# 3. MQTT 연결 및 동작
# ==========================================

# 메시지가 왔을 때 실행될 함수
def on_message(client, userdata, message):
    msg = str(message.payload.decode("utf-8")) # "ON" 또는 "OFF"
    print(f"📩 앱에서 받은 명령: {msg}")

    if msg == "ON":
        arduino.write(b'1') # 아두이노에게 '1' 전송 (바이트 단위)
        print("➡️ 아두이노로 '1' 전송함 (LED ON)")
    elif msg == "OFF":
        arduino.write(b'0') # 아두이노에게 '0' 전송
        print("➡️ 아두이노로 '0' 전송함 (LED OFF)")

# MQTT 클라이언트 설정
client = mqtt.Client()
client.on_message = on_message
client.connect(BROKER_ADDRESS, 1883)
client.subscribe(TOPIC)

print("브릿지 프로그램 시작! (명령 대기중...)")
client.loop_forever() # 무한 반복하며 메시지 기다림