package com.seohyun.sublindwaya;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class Login extends AppCompatActivity {
    private static final String TAG = "Login";

    private TextView userIdText;
    private View loginButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userIdText = findViewById(R.id.userId);
        loginButton = findViewById(R.id.login);
        logoutButton = findViewById(R.id.logout);

        setupLoginLogout();
        updateKakaoLoginUi();

        UserApiClient.getInstance().me((user, throwable) -> {
            if (user != null) {
                userIdText.setText(String.valueOf(user.getId()));
            }
            return null;
        });
    }

    private void setupLoginLogout() {
        Function2<OAuthToken, Throwable, Unit> callback = new Function2<OAuthToken, Throwable, Unit>() {
            @Override
            public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
                if (oAuthToken != null) {
                    // 로그인 성공
                    navigateToMainActivity();
                }
                if (throwable != null) {
                    // 로그인 실패
                    Toast.makeText(Login.this, "Login failed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Login failed", throwable);
                }
                updateKakaoLoginUi();
                return null;
            }
        };

        loginButton.setOnClickListener(view -> {
            if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(Login.this)) {
                UserApiClient.getInstance().loginWithKakaoTalk(Login.this, callback);
            } else {
                UserApiClient.getInstance().loginWithKakaoAccount(Login.this, callback);
            }
        });

        logoutButton.setOnClickListener(view -> {
            UserApiClient.getInstance().logout(throwable -> {
                UserManager.getInstance().clear();
                updateKakaoLoginUi();
                return null;
            });
        });
    }

    private void updateKakaoLoginUi() {
        UserApiClient.getInstance().me((user, throwable) -> {
            if (user != null) {
                // 로그인 성공, 유저 정보 업데이트
                UserManager.getInstance().setUserId(String.valueOf(user.getId()));
                UserManager.getInstance().setLoggedIn(true);

                userIdText.setText(UserManager.getInstance().getUserId());
                loginButton.setVisibility(View.GONE);
                logoutButton.setVisibility(View.VISIBLE);

                // 로그인 성공 후 MainActivity로 이동
                navigateToMainActivity();
            } else {
                // 로그아웃 상태
                UserManager.getInstance().clear();
                userIdText.setText(null);
                loginButton.setVisibility(View.VISIBLE);
                logoutButton.setVisibility(View.GONE);
            }
            return null;
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
        finish(); // 현재 로그인 액티비티를 종료
    }
}
