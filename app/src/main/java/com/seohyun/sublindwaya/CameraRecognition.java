package com.seohyun.sublindwaya;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class CameraRecognition extends AppCompatActivity {

    private static int CAMERA_PERMISSION_CODE = 100;

    ImageView thumb; // 가져온 이미지를 표시할 썸네일
    Button send; // 서버로 전송하기 버튼

    File file; // 서버로 전송할 파일

    // 안드로이드 기기의 카메라를 통해서 이미지를 촬영할 때 사용할 Activity Launcher 초기화
    ActivityResultLauncher<Intent> launcher_capture = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @RequiresApi(api = Build.VERSION_CODES.P)
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d("launcher_capture Callback", "image capturing is succeed");

                        // 번들을 통해서 촬영 후 저장된 사진의 비트맵을 가져옴
                        Bundle extra = result.getData().getExtras();
                        Bitmap bitmap = (Bitmap) extra.get("data");

                        // ImageView에 촬영한 사진 보이게 설정
                        thumb.setImageBitmap(bitmap);

                        // 현재 ImageView가 숨김 상태일 경우 ImageView와 Send Button을 사용자에게 보이게 변경
                        if(thumb.getVisibility() != View.VISIBLE){
                            thumb.setVisibility(View.VISIBLE);
                            send.setVisibility(View.VISIBLE);
                        }

                        // 서버로 전송하기 위해 비트맵을 파일로 변환
                        file = saveBitmapToJpeg(bitmap, CameraRecognition.this);

                    }else if(result.getResultCode() == Activity.RESULT_CANCELED){
                        Log.d("launcher_capture Callback", "image capturing is canceled");
                    }else{
                        Log.e("launcher_capture Callback", "image capturing has failed");
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_recognition);

        thumb = (ImageView) findViewById(R.id.imageView);

        Button capture = (Button) findViewById(R.id.capture);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCameraPermission(); // 카메라 권한 확인
            }
        });

        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                getCameraPermission();
                if(file != null){ // File 객체 변수가 null 이 아닐 경우 파일 전송 진행

                    // File 객체로 RequestBody 객체 생성
                    RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);

                    // RequestBody로 MultipartBody.Part 객체 생성                            // (서버에서 확인할 파일 키, 파일 이름, RequestBody)
                    MultipartBody.Part filePart = MultipartBody.Part.createFormData("file_key", file.getName(), fileBody);

                    // Retrofit 객체 생성
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(Retrofit_send_img.BASE_URL)
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    // Retrofit 객체를 사용해 비동기 통신 메소드가 있는 인터페이스 객체 생성
                    Retrofit_send_img api = retrofit.create(Retrofit_send_img.class);

                    // 인터페이스 객체를 사용해 Call 객체를 만들어 통신 시작
                    Call<String> call = api.test_send_img(filePart);
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            Log.e("Img Send Test response code : ", ""+response.code());
                            Log.e("Img Send Test response message : ", ""+response.body());
                            String jsonStr = response.body(); // 응답 결과
                            try {
                                // JSONObject로 변환
                                JSONObject jsonObject = new JSONObject(jsonStr);

                                if(jsonObject.getString("status").equals("success")){ // 업로드에 성공했을 경우
                                    Toast.makeText(CameraRecognition.this, "이미지 파일 업로드에 성공했습니다", Toast.LENGTH_SHORT).show();
                                }else{ // 업로드에 실패했을 경우
                                    Toast.makeText(CameraRecognition.this, "이미지 파일 업로드에 실패했습니다", Toast.LENGTH_SHORT).show();
                                    Log.e("Img Send Test fail message : ", jsonObject.getString("message"));
                                }
                            } catch (JSONException e) { // 업로드에 실패했을 경우
                                Toast.makeText(CameraRecognition.this, "이미지 파일 업로드에 실패했습니다", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) { // 서버와 통신에 실패했을 경우
                            Toast.makeText(CameraRecognition.this, "서버와 연결에 실패했습니다", Toast.LENGTH_SHORT).show();
                            Log.e("Img Send Test fail message : ", t.getMessage());
                        }
                    });
                }
            }
        });

    }

    // 권한 확인 메소드
    private void getCameraPermission(){
        // 현재 카메라 권한 여부 확인
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            // 권한이 없을 경우 요청
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }else{
            // 권한이 있을 경우 카메라 촬영 진행
            Log.d("Check Camera Permission", "Permission Allowed");
            captureImage();
        }
    }

    // 이미지 촬영 메소드
    private void captureImage(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        launcher_capture.launch(intent);
    }

    // 비트맵을 파일로 변환하는 메소드
    public File saveBitmapToJpeg(Bitmap bitmap, Context context) {

        //내부저장소 캐시 경로를 받아옵니다.
        File storage = context.getCacheDir();

        //저장할 파일 이름
        String fileName = String.valueOf(System.currentTimeMillis()) + ".jpg";

        //storage 에 파일 인스턴스를 생성합니다.
        File imgFile = new File(storage, fileName);

        try {

            // 자동으로 빈 파일을 생성합니다.
            imgFile.createNewFile();

            // 파일을 쓸 수 있는 스트림을 준비합니다.
            FileOutputStream out = new FileOutputStream(imgFile);

            // compress 함수를 사용해 스트림에 비트맵을 저장합니다.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            // 스트림 사용후 닫아줍니다.
            out.close();

            return imgFile;

        } catch (FileNotFoundException e) {
            Log.e("MyTag","FileNotFoundException : " + e.getMessage());
        } catch (IOException e) {
            Log.e("MyTag","IOException : " + e.getMessage());
        }

        return imgFile;
    }

}