package com.seohyun.sublindwaya;

import static android.Manifest.permission.CAMERA;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Environment.DIRECTORY_PICTURES;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_recognition);
        if (ContextCompat.checkSelfPermission(this, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            dispatchTakePictureIntent();
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
        String[] projection = new String[] {
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
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/"); // 'Sublindwaya' 폴더 부분을 제거
            values.put(MediaStore.Images.Media.TITLE, fileName); // 제목에 파일 이름 설정
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName + ".jpg"); // 실제 파일 이름 설정
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
            sendImageToServer(photoURI);
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

    private void sendImageToServer(Uri imageUri) {
        byte[] imageData = compressImage(imageUri);
        if (imageData == null) {
            Toast.makeText(this, "이미지 압축에 실패했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageData);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", "filename.jpg", requestFile);

        Retrofit_send_img service = Retrofit_subway.getSendImgService();
        Call<String> call = service.test_send_img(body);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String message = response.isSuccessful() ? "이미지가 성공적으로 전송되었습니다" : "이미지 전송에 실패했습니다. Response code: " + response.code() + ", Message: " + response.message();
                Log.d("Camera", message);
                Toast.makeText(CameraRecognition.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                String message = "오류: " + t.getMessage();
                Log.e("Camera", message);
                Toast.makeText(CameraRecognition.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }


}