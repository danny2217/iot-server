package com.example.my_iot_app; // 패키지명 본인꺼 확인!

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class LoginActivity extends AppCompatActivity {

    EditText etId, etPw;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etId = findViewById(R.id.et_id);
        etPw = findViewById(R.id.et_pw);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {
            String id = etId.getText().toString();
            String pw = etPw.getText().toString();
            login(id, pw);
        });
    }

    private void login(String id, String pw) {
        // 1. Retrofit 설정 (IP 본인꺼!)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://3.34.188.230:8080/")
                .addConverterFactory(ScalarsConverterFactory.create()) // String 받기용
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);

        // 2. 로그인 요청
        LoginRequest request = new LoginRequest(id, pw);
        service.login(request).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body();

                    if (result.startsWith("SUCCESS")) {
                        Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();

                        // 3. 성공 시 메인 화면(LED 제어)으로 이동
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // 뒤로가기 못하게 종료
                    } else {
                        Toast.makeText(LoginActivity.this, "로그인 실패..", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}