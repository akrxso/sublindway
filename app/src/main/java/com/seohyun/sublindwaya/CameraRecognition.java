package com.seohyun.sublindwaya;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Collections;

public class CameraRecognition extends AppCompatActivity {

    // 카메라 미리보기를 위한 TextureView
    private TextureView textureView;

    // 카메라 디바이스를 참조하기 위한 객체
    private CameraDevice cameraDevice;

    // 캡처 요청을 만들기 위한 Builder
    private CaptureRequest.Builder captureRequestBuilder;

    // 카메라 캡처 세션
    private CameraCaptureSession cameraCaptureSession;

    // 카메라 사용 권한 요청 코드
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    // TextureView의 상태를 감지하는 리스너
    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            // 텍스쳐뷰가 사용 가능할 때 카메라를 여는 함수 호출
            openCamera();
        }

        // 텍스처 크기가 변경될 때 호출
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        // 텍스쳐가 파괴될 때 호출
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        // 텍스쳐가 업데이트될 때 호출
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    // 카메라 상태 콜백
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            // 카메라가 성공적으로 열리면 CameraDevice 인스턴스를 할당하고 미리보기 시작
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            // 카메라 연결이 끊기면 닫음
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            // 에러 발생시 카메라 닫고 null 할당
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_recognition);
        textureView = findViewById(R.id.textureView);
        // TextureView에 리스너 설정
        textureView.setSurfaceTextureListener(textureListener);

    }

    // 카메라 미리보기를 생성하는 메소드
    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            // 텍스처의 크기를 카메라 미리보기 크기로 설정
            texture.setDefaultBufferSize(1920, 1080);
            Surface surface = new Surface(texture);
            // 미리보기를 위한 CaptureRequest.Builder 설정
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            // 카메라 캡처 세션 생성
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    // 카메라가 이미 닫혔다면 아무 작업도 수행하지 않음
                    if (cameraDevice == null) {
                        return;
                    }
                    // 캡처 세션 시작
                    cameraCaptureSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    // 구성 실패시 Toast 메시지 출력
                    Toast.makeText(CameraRecognition.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 카메라를 여는 메소드
    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            // 카메라 권한 체크
            if (ActivityCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.CAMERA"}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            // 카메라 오픈, 상태 콜백과 핸들러 설정
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 카메라 미리보기를 업데이트하는 메소드
    private void updatePreview() {
        if (cameraDevice == null) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            return;
        }
        // 자동 모드로 미리보기 설정
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            // 미리보기 요청을 반복하여 캡처 세션에 제출
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        // 액티비티가 일시정지되면 카메라를 닫음
        super.onPause();
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }
}