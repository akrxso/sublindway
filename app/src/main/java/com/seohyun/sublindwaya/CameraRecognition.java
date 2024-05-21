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
            ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoURI = createImageFileUri();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            } catch (IOException ex) {
                Log.e("Camera", "Error occurred while creating the File", ex);
                Toast.makeText(this, "Failed to create image file.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private Uri createImageFileUri() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "JPEG_" + timeStamp;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/");
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName + ".jpg");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            // Get the userId from UserManager
            String kakaoId = UserManager.getInstance().getUserId();
            sendImageToServer(photoURI, kakaoId, 35.422, 125.084);
            Toast.makeText(this, "Image sent to server.", Toast.LENGTH_SHORT).show();
        }
    }
    private byte[] compressImage(Uri imageUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
            Bitmap original = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            original.compress(Bitmap.CompressFormat.JPEG, 50, out);
            return out.toByteArray();
        } catch (IOException e) {
            Log.e("Camera", "Compressing image failed", e);
            return null;
        }
    }

    private void sendImageToServer(Uri imageUri, String kakaoId, double locationX, double locationY) {
        byte[] imageData = compressImage(imageUri);
        if (imageData == null) {
            Toast.makeText(this, "Failed to compress image.", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageData);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", "filename.jpg", requestFile);
        RequestBody kakaoIdBody = RequestBody.create(MediaType.parse("text/plain"), kakaoId);
        RequestBody locationXBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(locationX));
        RequestBody locationYBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(locationY));

        Retrofit_send_img service = Retrofit_subway.getSendImgService();
        Call<String> call = service.test_send_img(body, kakaoIdBody, locationXBody, locationYBody);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d("Camera", "Image successfully sent to server");
                    Toast.makeText(CameraRecognition.this, "Image successfully sent to server", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("Camera", "Image transmission failed. Response code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(CameraRecognition.this, "Image transmission failed. Response code: " + response.code() + ", Message: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("Camera", "Error: " + t.getMessage());
                Toast.makeText(CameraRecognition.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
