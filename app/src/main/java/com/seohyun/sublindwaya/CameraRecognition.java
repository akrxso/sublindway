package com.seohyun.sublindwaya;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    private static final int CAMERA_PERMISSION_CODE = 100;
    private ImageView thumb;
    private Button send;
    private File file;
    private Uri photoURI;

    ActivityResultLauncher<Intent> launcher_capture = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @RequiresApi(api = Build.VERSION_CODES.P)
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d("launcher_capture Callback", "image capturing and saving is succeed");

                        // ImageView에 촬영한 사진 보이게 설정
                        thumb.setImageURI(photoURI);
                        thumb.setVisibility(View.VISIBLE);
                        send.setVisibility(View.VISIBLE);

                        // 이미지 파일 객체로 변환
                        file = new File(photoURI.getPath());
                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        Log.d("launcher_capture Callback", "image capturing is canceled");
                    } else {
                        Log.e("launcher_capture Callback", "image capturing has failed");
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_recognition);

        thumb = findViewById(R.id.imageView);
        send = findViewById(R.id.send);
        Button capture = findViewById(R.id.capture);

        capture.setOnClickListener(view -> getCameraPermission());
        send.setOnClickListener(view -> sendImage());
    }

    private void getCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            captureImage();
        }
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("captureImage", "Image file creation failed", ex);
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".provider",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                launcher_capture.launch(intent);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    private void sendImage() {
        if (file != null) {
            RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file_key", file.getName(), fileBody);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Retrofit_send_img.BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            Retrofit_send_img api = retrofit.create(Retrofit_send_img.class);
            Call<String> call = api.test_send_img(filePart);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Log.e("Img Send Test response code : ", "" + response.code());
                    Toast.makeText(CameraRecognition.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(CameraRecognition.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
                    Log.e("Img Send Test fail message : ", t.getMessage());
                }
            });
        }
    }
}
