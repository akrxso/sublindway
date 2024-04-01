package com.seohyun.sublindwaya;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.util.FusedLocationSource;

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

    public double getLongitude() {
        return lo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location_tracking);

        textView =findViewById(R.id.textView2);
        call = Retrofit_subway.getApiService().test_api_get(getLatitude(),getLongitude());

        call.enqueue(new Callback<xy_model>(){
            @Override
            public void onResponse(Call<xy_model> call, Response<xy_model> response) {
                xy_model result = response.body();
                String str;
                str = result.getStation()+"\n";
                textView.setText(str);
            }

            @Override
            public void onFailure(Call<xy_model> call, Throwable t) {

            }
        });

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
                la=location.getLatitude();
                lo=location.getLongitude();

                Toast.makeText(getApplicationContext(),
                        la+","+lo,Toast.LENGTH_SHORT).show(); //위도 경도를 토스트 메시지로 알려준다
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

}
