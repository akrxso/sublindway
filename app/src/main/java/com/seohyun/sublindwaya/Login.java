package com.seohyun.sublindwaya;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {
    private static final String TAG = "Login";
    private TextView userIdText, subwaynameText;
    private View loginButton, logoutButton;

    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;
    private Retrofit_subway_interface retrofitService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userIdText = findViewById(R.id.userId);
        subwaynameText = findViewById(R.id.subwayname);
        loginButton = findViewById(R.id.login);
        logoutButton = findViewById(R.id.logout);

        setupLocationClient();
        setupLoginLogout();
        setupRetrofit();

        updateKakaoLoginUi();

        UserApiClient.getInstance().me((user, throwable) -> {
            if (user != null) {
                userIdText.setText(String.valueOf(user.getId()));
            }
            return null;
        });
    }

    private void setupRetrofit() {
        retrofitService = Retrofit_subway.getInstance().create(Retrofit_subway_interface.class);
    }

    private void setupLoginLogout() {
        Function2<OAuthToken, Throwable, Unit> callback = new Function2<OAuthToken, Throwable, Unit>() {
            @Override
            public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
                if (oAuthToken != null) {
                    // 로그인 성공
                }
                if (throwable != null) {
                    // 로그인 실패
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
                subwaynameText.setText(LocationManager.getInstance().toString());
                loginButton.setVisibility(View.GONE);
                logoutButton.setVisibility(View.VISIBLE);
            } else {
                // 로그아웃 상태
                UserManager.getInstance().clear();
                userIdText.setText(null);
                subwaynameText.setText(null);
                loginButton.setVisibility(View.VISIBLE);
                logoutButton.setVisibility(View.GONE);
            }
            return null;
        });
    }

    private void setupLocationClient() {
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    sendLocationToServer(location);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling ActivityCompat#requestPermissions
            return;
        }
        locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void sendLocationToServer(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String userId = UserManager.getInstance().getUserId();

        Retrofit_subway_interface service = Retrofit_subway.getInstance().create(Retrofit_subway_interface.class);
        Call<xy_model> call = service.test_api_get(userId, latitude, longitude);
        call.enqueue(new Callback<xy_model>() {
            @Override
            public void onResponse(Call<xy_model> call, Response<xy_model> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String subwayName = response.body().getSubwayName();
                    subwaynameText.setText(subwayName + "\n");
                    Log.d(TAG, "Server Response: " + subwayName);
                } else {
                    Log.e(TAG, "Server Response Failed - Error Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<xy_model> call, Throwable t) {
                Log.e(TAG, "Network Request Failed: " + t.getMessage());
            }
        });
    }

}
