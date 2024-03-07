package com.seohyun.sublindwaya;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //카카오 로그인 구현

        final Button button = findViewById(R.id.mapBtn);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LocationTrackingActivity.class);
                // 새로운 액티비티 시작
                startActivity(intent);
            }
        });
    }


}