package com.seohyun.sublindwaya;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CAMERA_ACTIVITY = 102;
    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 2;

    public static double latitude;
    public static double longitude;
    public static String s;

    private TextView subwaynameText;

    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;
    private Retrofit_subway_interface retrofitService_subway_interface;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech tts;
    private String lastSubwayName = "";

    private Bluetooth bluetooth;
    private EditText inputEditText;
    private ArrayAdapter<String> conversationArrayAdapter;
    private TextView connectionStatus;

    public String trainLocation;

    private Handler handler = new Handler();
    private Runnable trackTrainRunnable;

    private String currentSubwayName;
    private String currentTrainLocation;

    private String subwayLineName;
    private String nextStationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trainLocation = trainLocationManager.gettrainLocation();
        setContentView(R.layout.activity_main);

        subwaynameText = findViewById(R.id.textView);

        setupLocationClient();
        setupRetrofit();
        setupSpeechRecognizer();
        setupTextToSpeech();

        inputEditText = new EditText(this);
        connectionStatus = new TextView(this);
        conversationArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        Log.d(TAG, "Creating Bluetooth instance");
        bluetooth = new Bluetooth(this, connectionStatus, inputEditText, conversationArrayAdapter);

        bluetooth.showPairedDevicesListDialog();
        Log.d(TAG, "Calling showPairedDevicesListDialog");

        Log.d(TAG, "trainLocation" + trainLocation);

        trackTrainRunnable = new Runnable() {
            @Override
            public void run() {
                if (trainLocation != null && !trainLocation.isEmpty()) {
                    trackTrain(trainLocation);
                }
                handler.postDelayed(this, 5000); // 5초마다 실행
            }
        };

        handler.post(trackTrainRunnable);

        // 초기 위치에서 현재 역 이름 설정
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                sendLocationToServer(location);
            }
        });
    }

    public String getSubwayName() {
        return subwaynameText.getText().toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentTrainLocation();
    }

    private void updateCurrentTrainLocation() {
        if (currentTrainLocation != null) {
            subwaynameText.setText(currentTrainLocation + "\n");
            Log.d(TAG, "Updated train location to: " + currentTrainLocation);
        } else {
            Log.d(TAG, "No train location available");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (checkAudioPermission()) {
                startSpeechRecognition();
            } else {
                requestAudioPermission();
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void setupRetrofit() {
        retrofitService_subway_interface = Retrofit_subway.getInstance().create(Retrofit_subway_interface.class);
    }

    private void setupLocationClient() {
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
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

    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                RECORD_AUDIO_PERMISSION_REQUEST_CODE);
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
        } else if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechRecognition();
            } else {
                Toast.makeText(this, "Audio permission denied", Toast.LENGTH_SHORT).show();
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
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        String userId = UserManager.getInstance().getUserId();

        Log.d(TAG, "Server Request - Latitude: " + latitude + ", Longitude: " + longitude + ", UserId: " + userId);

        Call<xy_model> call = retrofitService_subway_interface.test_api_get(userId, latitude, longitude);
        call.enqueue(new Callback<xy_model>() {
            @Override
            public void onResponse(Call<xy_model> call, Response<xy_model> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String subwayName = response.body().getSubwayName();
                    if (currentSubwayName == null || !currentSubwayName.equals(subwayName)) {
                        currentSubwayName = subwayName;
                        updateSubwayInfo(subwayName);
                    }
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

    private void setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Toast.makeText(MainActivity.this, "음성 인식 시작", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
                String message = String.valueOf(error);
                Toast.makeText(MainActivity.this, "음성 인식 에러 발생: " + message, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Speech recognition error: " + message);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    for (String match : matches) {
                        Log.d(TAG, "Recognized speech: " + match);
                        if (match.contains("카메라")) {
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            locationClient.getLastLocation().addOnSuccessListener(location -> {
                                if (location != null) {
                                    sendLocationToServer(location);
                                }
                            });
                            Intent intent = new Intent(MainActivity.this, CameraRecognition.class);
                            startActivityForResult(intent, REQUEST_CAMERA_ACTIVITY);
                            break;
                        } else if (match.contains("하차")) {
                            sendLocationToQuitServer();
                            break;
                        } else if (match.contains("로그아웃")) {
                            UserApiClient.getInstance().logout(throwable -> {
                                if (throwable != null) {
                                    Toast.makeText(MainActivity.this, "Logout failed", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Logout failed", throwable);
                                } else {
                                    UserManager.getInstance().clear();
                                    Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, Login.class);
                                    startActivity(intent);
                                    finish();
                                }
                                return null;
                            });
                        } else if (match.contains("어디야")) {
                            speakText("이 번 역 은     " + getSubwayName() + " 역   입 니 다");
                            Log.d("Tag", getSubwayName());
                            bluetooth.sendMessage(getSubwayName());
                        }
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }

    private void sendLocationToQuitServer() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                String userId = UserManager.getInstance().getUserId();

                Log.d(TAG, "Server Request - Latitude: " + latitude + ", Longitude: " + longitude + ", UserId: " + userId);

                Call<xy_model> call = retrofitService_subway_interface.quit(userId, latitude, longitude);
                call.enqueue(new Callback<xy_model>() {
                    @Override
                    public void onResponse(Call<xy_model> call, Response<xy_model> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String subwayName = response.body().getSubwayName();
                            subwaynameText.setText(subwayName + "\n");
                            Log.d(TAG, "Server Response: " + subwayName);
                            speakText("하 차   완 료 되 었 습 니 다");
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
        });
    }

    private void trackTrain(String trainNumber) {
        Log.d("trackTrain", "trackTrain 메서드가 호출되었습니다.");
        Log.d("trackTrain", "하이 " + trainNumber);
        Call<statnNm_model> call = retrofitService_subway_interface.train(trainNumber);

        call.enqueue(new Callback<statnNm_model>() {
            @Override
            public void onResponse(Call<statnNm_model> call, Response<statnNm_model> response) {
                if (response.isSuccessful() && response.body() != null) {
                    statnNm_model trainData = response.body();
                    String newTrainLocation = trainData.getSubwayName();
                    subwayLineName = trainData.getSubwayLineName();
                    nextTrainManager.getInstance().setCurrentStation(newTrainLocation);
                    nextTrainManager.getInstance().setSubwayLine(subwayLineName);

                    if (!newTrainLocation.equals(currentTrainLocation)) {
                        currentTrainLocation = newTrainLocation;
                        updateSubwayInfo(currentTrainLocation);
                    }

                    Log.d("trackTrain", "현재 열차: " + currentTrainLocation);
                    Log.d("trackTrain", "호선: " + subwayLineName);
                } else {
                    String message = "Failed to track train. Response code: " + response.code();
                    Log.e("trackTrain", message);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<statnNm_model> call, Throwable t) {
                String message = "Network request failed: " + t.getMessage();
                Log.e("trackTrain", message);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTextToSpeech() {
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.KOREAN);
            }
        });
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        speechRecognizer.startListening(intent);
    }

    private void speakText(String text) {
        if (tts != null) { // TTS 객체가 null인지 확인
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.setPitch(1.0f);
                tts.setSpeechRate(0.5f);
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        } else {
            Log.e("Camera", "TextToSpeech is not initialized");
        }
    }

    private void updateSubwayInfo(String subwayName) {
        subwaynameText.setText(subwayName + "\n");
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        handler.removeCallbacks(trackTrainRunnable);
        super.onDestroy();
    }
}
