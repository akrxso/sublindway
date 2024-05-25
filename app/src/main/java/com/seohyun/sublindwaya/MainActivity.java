package com.seohyun.sublindwaya;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.kakao.sdk.user.UserApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextView subwaynameText;
    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;
    private Retrofit_subway_interface retrofitService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        subwaynameText = findViewById(R.id.textView);

        setupLocationClient();
        setupRetrofit();

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


        Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(view -> {
            UserApiClient.getInstance().logout(throwable -> {
                if (throwable != null) {
                    Toast.makeText(MainActivity.this, "Logout failed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Logout failed", throwable);
                } else {
                    UserManager.getInstance().clear();
                    Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                    finish(); // 현재 액티비티 종료
                }
                return null;
            });
        });
    }

    private void setupRetrofit() {
        retrofitService = Retrofit_subway.getInstance().create(Retrofit_subway_interface.class);
    }

    private void setupLocationClient() {
        locationClient = LocationServices.getFusedLocationProviderClient(this);

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

        if (checkLocationPermission()) {
            startLocationUpdates();
        } else {
            requestLocationPermission();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void sendLocationToServer(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String userId = UserManager.getInstance().getUserId();

        Log.d(TAG, "Server Request - Latitude: " + latitude + ", Longitude: " + longitude + ", UserId: " + userId);

        Call<xy_model> call = retrofitService.test_api_get(userId, latitude, longitude);
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
