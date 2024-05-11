package com.seohyun.sublindwaya;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        final Button mapButton = findViewById(R.id.mapBtn);
        mapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LocationTrackingActivity.class);
                // 새로운 액티비티 시작
                startActivity(intent);
            }
        });

        final Button cameraButton = findViewById(R.id.cameraBtn);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraRecognition.class);
                // 새로운 액티비티 시작
                startActivity(intent);
            }
        });

        final Button ttsButton = findViewById(R.id.ttsBtn);
        ttsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TextToSpeech.class);
                // 새로운 액티비티 시작
                startActivity(intent);
            }
        });

        final Button bluetoothBtn = findViewById(R.id.bluetoothBtn);
        bluetoothBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, raspiBluetooth.class);
                // 새로운 액티비티 시작
                startActivity(intent);
            }
        });

    }

}