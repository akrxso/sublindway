package com.seohyun.sublindwaya;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    Call<xy_model> call;
    TextView textView;
    private NaverMap naverMap;
    private FusedLocationSource locationSource; //위치 반환
    private  static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private double la,lo; //위도 경도

    public double getLatitude() {
        return la;
    }

    public void setLatitude(double la){
        this.la = la;
    }

    public double getLongitude() {
        return lo;
    }

    public void setLongitude(double lo){
        this.lo = lo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location_tracking);

        textView =findViewById(R.id.textView2);

        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient("ceh0sosrje"));

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map_fragment);
        if(mapFragment == null)
        {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        locationSource = new FusedLocationSource(this,LOCATION_PERMISSION_REQUEST_CODE);

        sendSubwayRequest();
    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Log.d( TAG, "onMapReady");

        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        naverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener()
        {
            @Override
            public void onLocationChange(@NonNull Location location)
            {
                setLatitude(location.getLatitude());
                setLongitude(location.getLongitude());

                //Toast.makeText(getApplicationContext(),getLatitude() + "," + getLongitude(),Toast.LENGTH_SHORT).show(); //위도 경도를 토스트 메시지로 알려준다
            }

        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,  @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // 권한 거부됨
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    public void sendSubwayRequest() {
        double locationX = 37.588380;
        double locationY = 127.006751;

        Retrofit_subway_interface service = Retrofit_subway.getInstance().create(Retrofit_subway_interface.class);
        Call<xy_model> call = service.test_api_get(locationX, locationY);
        call.enqueue(new Callback<xy_model>() {
            @Override
            public void onResponse(Call<xy_model> call, Response<xy_model> response) {
                if (response.isSuccessful()) {
                    int statusCode = response.code();
                    Log.d(TAG, "서버 응답 상태 코드: " + statusCode);

                    xy_model result = response.body();
                    String str;

                    // 기존 Android 코드 부분
                    if (result != null) {
                        Log.d(TAG, "서버 응답 본문: " + result.toString());
                        str = result.getSubwayName() + "\n"; // 예를 들어 "한성대입구역\n"
                        textView.setText(str); // 텍스트 뷰에 문자열을 설정

                        // 자모 분리 코드 추가 부분
                        List<List<String>> jamoSplit = JamoUtils.split(str.trim()); // str에서 개행 문자 제거 후 자모 분리
                        StringBuilder sb = new StringBuilder();
                        for (List<String> jamo : jamoSplit) {
                            sb.append(String.join(",", jamo)).append("\n"); // 자모를 쉼표로 연결하여 한 글자씩 줄바꿈 추가
                        }
                        Log.d(TAG, "분리된 자모:\n" + sb.toString());
                    } else {
                        Log.d(TAG, "서버 응답 본문이 비어 있습니다.");
                    }

                }else {
                    // 서버 응답이 실패한 경우
                    int errorCode = response.code();
                    Log.e(TAG, "서버 응답 실패 - 에러 코드: " + errorCode);
                }
            }

            @Override
            public void onFailure(Call<xy_model> call, Throwable t) {
                Log.e(TAG, "네트워크 요청 실패: " + t.getMessage());            }
        });
    }


}
