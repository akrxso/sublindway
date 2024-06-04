package com.seohyun.sublindwaya;

import static android.Manifest.permission.CAMERA;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import static com.seohyun.sublindwaya.MainActivity.s;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraRecognition extends AppCompatActivity {
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_CAMERA_PERMISSION = 101;
    static Uri photoURI;

    public static String statnNm;
    private TextToSpeech tts;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupTextToSpeech();

        if (ContextCompat.checkSelfPermission(this, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            dispatchTakePictureIntent();
        }

    }

    private void setupTextToSpeech() {
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.KOREAN);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        super.onDestroy();
    }

    private void speakText(String text) {
        if (tts != null) { // TTS 객체가 null인지 확인
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.setPitch(1.0f);
                tts.setSpeechRate(0.7f);
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        } else {
            Log.e("Camera", "TextToSpeech is not initialized");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Uri getMostRecentPhotoUri() {
        Uri externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
        };

        try (Cursor cursor = getContentResolver().query(externalUri, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")) {
            if (cursor != null && cursor.moveToFirst()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Long.parseLong(id));
            }
        } catch (Exception e) {
            Log.e("Camera", "Failed to query recent photo: " + e.getMessage());
        }
        return null;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // 현재 시간을 기반으로 파일 이름 생성
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "JPEG_" + timeStamp;
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/");
            values.put(MediaStore.Images.Media.TITLE, fileName);
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName + ".jpg");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            photoURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            String kakaoId = UserManager.getInstance().getUserId();
            sendImageToServer(photoURI, kakaoId, MainActivity.latitude, MainActivity.longitude);
            String message = "Image sent to server.";
            Log.d("Camera", message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] compressImage(Uri imageUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
            Bitmap original = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            original.compress(Bitmap.CompressFormat.JPEG, 50, out); // 품질을 50%로 설정
            return out.toByteArray();
        } catch (IOException e) {
            Log.e("Camera", "Compressing image failed", e);
            return null;
        }
    }

    private void sendImageToServer(Uri imageUri, String kakaoId, double locationX, double locationY) {
        byte[] imageData = compressImage(imageUri);
        if (imageData == null) {
            Toast.makeText(this, "이미지 압축에 실패했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageData);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", "filename.jpg", requestFile);

        Retrofit_send_img service = Retrofit_subway.getSendImgService();
        Call<ServerResponse> call = service.test_send_img(body, kakaoId, locationX, locationY);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse serverResponse = response.body();
                    if (serverResponse != null) {
                        if (serverResponse.getTrainNum() != null) {
                            String message = "Train Number: " + serverResponse.getTrainNum() +
                                    "\nDirection: " + serverResponse.getUpDown() +
                                    "\nLocationX: " + serverResponse.getLocationX() +
                                    "\nLocationY: " + serverResponse.getLocationY();
                            Log.d("Camera", message);
                            Toast.makeText(CameraRecognition.this, message, Toast.LENGTH_SHORT).show();
                            trainLocationManager.getInstance().settrainLocation(serverResponse.getTrainNum());
                            trainLocationManager.getInstance().setDirection(serverResponse.getUpDown()); //상하행
//                            MainActivity.s = serverResponse.getUpDown();
//                            Log.d("s", MainActivity.s);
//                            serverResponse.setUpDown(MainActivity.s);
                            statnNm =serverResponse.getTrainNum();
                            serverResponse.setTrainNum(statnNm);

                            if(serverResponse.getUpDown().contains("상행")||serverResponse.getUpDown().contains("하행")){
                                speakText(serverResponse.getUpDown()+"          입니다                    ");
                            }
                            // 메인 액티비티로 돌아가는 코드 추가
                            Intent intent = new Intent(CameraRecognition.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String message = "Train Number is null. Retaking photo...";
                            Log.d("Camera", message);
                            Toast.makeText(CameraRecognition.this, message, Toast.LENGTH_SHORT).show();
                            if(serverResponse.getUpDown().contains("-"))
                            {
                                speakText(serverResponse.getUpDown()+"칸 입니다                ");
                                speakText("열차에 탑승하려면 재촬영해주세요");
                                dispatchTakePictureIntent();
                                //칸번호 말하기
                            }
                            else if(serverResponse.getUpDown().contains("Person")) {
                                //사람 말해주기
                                speakText("전방에 사람이 있습니다");
                                speakText("열차에 탑승하려면 재촬영해주세요");
                                dispatchTakePictureIntent();
                            }
                            else {
                                speakText("열차에 탑승하려면 스크린도어 쪽을 재촬영해주세요");
                                dispatchTakePictureIntent();
                            }
                        }
                    } else {
                        String message = "응답 본문이 없습니다";
                        Log.d("Camera", message);
                        Toast.makeText(CameraRecognition.this, message, Toast.LENGTH_SHORT).show();
                        speakText("오류 열차에 탑승하려면 재촬영해주세요");
                        dispatchTakePictureIntent();

                    }
                } else {
                    String message = "이미지 전송에 실패했습니다. Response code: " + response.code() + ", Message: " + response.message();
                    Log.d("Camera", message);

                    if (response.errorBody() != null) {
                        try {
                            Log.e("Camera", "Error body: " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e("Camera", "Error reading error body", e);
                        }
                    }

                    Toast.makeText(CameraRecognition.this, message, Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                String message = "오류: " + t.getMessage();
                Log.e("Camera", message);
                Toast.makeText(CameraRecognition.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

}