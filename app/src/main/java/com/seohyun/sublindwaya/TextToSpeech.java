package com.seohyun.sublindwaya;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Locale;

public class TextToSpeech extends AppCompatActivity {

    private android.speech.tts.TextToSpeech tts;
    private Button ttsButton;
    private EditText txtText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_speech);

        // 텍스트를 음성으로 변환하기 위한 TextToSpeech 객체 생성
        tts = new android.speech.tts.TextToSpeech(this, new android.speech.tts.TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=android.speech.tts.TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        ttsButton = findViewById(R.id.btnSpeak);
        txtText = findViewById(R.id.txtText);

        ttsButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override public void onClick(View v) {
                String text = txtText.getText().toString();

                tts.setPitch(1.0f);
                tts.setSpeechRate(1.0f);
                tts.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }
    @Override public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        super.onDestroy();
    }

}