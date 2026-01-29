package com.example.my_iot_app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {

    private TextView tvTemp, tvHumid;
    private Button btnRefresh, btnLedOn, btnLedOff;

    // [새로 추가된 변수]
    private TextView tvSensorStatus, tvLastCheck;
    Handler handler = new Handler();
    boolean isAlertShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 뷰 연결
        tvTemp = findViewById(R.id.tvTemp);
        tvHumid = findViewById(R.id.tvHumid);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnLedOn = findViewById(R.id.btnLedOn);
        btnLedOff = findViewById(R.id.btnLedOff);
        tvSensorStatus = findViewById(R.id.tvSensorStatus);
        tvLastCheck = findViewById(R.id.tvLastCheck);

        // 2. 버튼 리스너
        btnRefresh.setOnClickListener(v -> getSensorDataFromServer());
        btnLedOn.setOnClickListener(v -> sendCommand("ON"));
        btnLedOff.setOnClickListener(v -> sendCommand("OFF"));

        // 3. 알림 설정
        createNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // 4. 감시 시작
        startMonitoring();
    }

    void startMonitoring() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getSensorDataFromServer();
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(runnable);
    }

    private void getSensorDataFromServer() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://3.34.188.230:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        apiService.getLastSensorData().enqueue(new Callback<SensorData>() {
            @Override
            public void onResponse(Call<SensorData> call, Response<SensorData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SensorData data = response.body();

                    // 1. 온도/습도/시간 갱신
                    tvTemp.setText("온도: " + data.getTemperature() + " °C");
                    tvHumid.setText("습도: " + data.getHumidity() + " %");

                    if (data.getCreatedAt() != null) {
                        tvLastCheck.setText("마지막 확인: " + data.getCreatedAt());
                    } else {
                        tvLastCheck.setText("데이터 수신중...");
                    }

                    // 2. [수정됨] 인체 감지 로직 (getMotion 사용!)
                    int motionValue = data.getMotion(); // 0 또는 1

                    if (motionValue == 1) {
                        // 사람 있음!
                        tvSensorStatus.setText("🚨 침입자 감지됨! 🚨");
                        tvSensorStatus.setTextColor(Color.RED);

                        if (!isAlertShown) {
                            showNotification("경고!", "집에 누군가 침입했습니다!");
                            isAlertShown = true;
                        }
                    } else {
                        // 사람 없음
                        tvSensorStatus.setText("안전함 (사람 없음)");
                        tvSensorStatus.setTextColor(Color.GREEN);
                        isAlertShown = false;
                    }
                }
            }

            @Override
            public void onFailure(Call<SensorData> call, Throwable t) {
                Log.e("MyIoTApp", "에러: " + t.getMessage());
                if (tvSensorStatus != null) {
                    tvSensorStatus.setText("서버 연결 끊김");
                    tvSensorStatus.setTextColor(Color.GRAY);
                }
            }
        });
    }

    private void sendCommand(String cmd) {
        CommandReq req = new CommandReq(cmd);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://3.34.188.230:8080/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        apiService.controlLed(req).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful())
                    Toast.makeText(MainActivity.this, "전송: " + cmd, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });
    }

    void showNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "sensor_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        notificationManager.notify(1, builder.build());
    }

    void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("sensor_channel", "Sensor Alerts", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("인체 감지 알림");

            // 👇 이 줄이 빠져서 에러가 난 겁니다! (시스템에서 매니저를 불러와야 함)
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}