package com.example.my_iot_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {

    // 화면에 있는 녀석들을 담을 변수
    private TextView tvTemp;
    private TextView tvHumid;
    private Button btnRefresh;
    private Button btnLedOn, btnLedOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // XML 화면이랑 연결

        // 1. XML에 있는 애들을 찾아와서 변수에 넣기 (ID로 찾음)
        tvTemp = findViewById(R.id.tvTemp);
        tvHumid = findViewById(R.id.tvHumid);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnLedOn = findViewById(R.id.btnLedOn);
        btnLedOff = findViewById(R.id.btnLedOff);

        // 2. 버튼이 눌리면 할 일 정하기 (리스너)
        btnRefresh.setOnClickListener(v -> {
            getSensorDataFromServer(); // 서버에 요청 보내는 함수 실행!
        });
        // [LED 켜기 버튼 클릭]
        btnLedOn.setOnClickListener(v -> sendCommand("ON"));

        // [LED 끄기 버튼 클릭]
        btnLedOff.setOnClickListener(v -> sendCommand("OFF"));
    }

    // 서버로 명령 보내는 함수
    private void sendCommand(String cmd) {
        // 1. 보낼 데이터 포장 (CommandReq)
        CommandReq req = new CommandReq(cmd);

        // 2. Retrofit 준비 (아까 만든 거 재활용하거나 새로 생성)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create()) // 문자열 받으려면 필요 (없으면 에러날 수 있음, 일단 해보고 에러나면 추가)
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // 3. 전송!
        apiService.controlLed(req).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "명령 전송: " + cmd, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(MainActivity.this, "에러: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 서버랑 통신하는 함수
    private void getSensorDataFromServer() {
        // (1) Retrofit 설정 (무전기 조립)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // 중요! 에뮬레이터에서 내 컴퓨터(Localhost)를 부르는 주소
                .addConverterFactory(GsonConverterFactory.create()) // JSON -> 자바 변환기 장착
                .build();

        // (2) 메뉴판 가져오기
        ApiService apiService = retrofit.create(ApiService.class);

        // (3) 요청 보내기 (비동기: 앱 멈추지 말고 백그라운드에서 갔다 와!)
        apiService.getLastSensorData().enqueue(new Callback<SensorData>() {
            @Override
            public void onResponse(Call<SensorData> call, Response<SensorData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 성공! 데이터 꺼내기
                    SensorData data = response.body();

                    // 화면 갱신
                    tvTemp.setText("온도: " + data.getTemperature() + " °C");
                    tvHumid.setText("습도: " + data.getHumidity() + " %");

                    Toast.makeText(MainActivity.this, "갱신 완료!", Toast.LENGTH_SHORT).show();
                } else {
                    // 서버에는 갔는데 데이터가 없음
                    Toast.makeText(MainActivity.this, "데이터가 없어요 ㅠㅠ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SensorData> call, Throwable t) {
                // 아예 연결 실패 (서버가 꺼졌거나 인터넷 문제)
                Log.e("MyIoTApp", "에러 발생: " + t.getMessage());
                Toast.makeText(MainActivity.this, "서버 연결 실패.. 서버 켰나요?", Toast.LENGTH_SHORT).show();
            }
        });
    }
}