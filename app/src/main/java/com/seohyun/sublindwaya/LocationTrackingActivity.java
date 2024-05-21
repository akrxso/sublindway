//package com.seohyun.sublindwaya;
//
//import static android.content.ContentValues.TAG;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.fragment.app.FragmentManager;
//
//import android.location.Location;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.naver.maps.map.LocationTrackingMode;
//import com.naver.maps.map.MapFragment;
//import com.naver.maps.map.NaverMap;
//import com.naver.maps.map.NaverMapSdk;
//import com.naver.maps.map.OnMapReadyCallback;
//import com.naver.maps.map.util.FusedLocationSource;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class LocationTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {
//
//    private NaverMap naverMap;
//    private FusedLocationSource locationSource;
//    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
//    private TextView textView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_location_tracking);
//        textView = findViewById(R.id.textView2);
//
//        NaverMapSdk.getInstance(this).setClient(new NaverMapSdk.NaverCloudPlatformClient("ceh0sosrje"));
//
//        FragmentManager fm = getSupportFragmentManager();
//        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map_fragment);
//        if (mapFragment == null) {
//            mapFragment = MapFragment.newInstance();
//            fm.beginTransaction().add(R.id.map_fragment, mapFragment).commit();
//        }
//        mapFragment.getMapAsync(this);
//        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
//    }
//
//    @Override
//    public void onMapReady(@NonNull NaverMap naverMap) {
//        this.naverMap = naverMap;
//        naverMap.setLocationSource(locationSource);
//        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
//        naverMap.addOnLocationChangeListener(this::handleLocationChange);
//    }
//
//    private void handleLocationChange(@NonNull Location location) {
//        LocationManager locationManager = LocationManager.getInstance();
//        locationManager.updateLocation(location.getLatitude(), location.getLongitude());
//
//        sendSubwayRequest();
//        Log.d(TAG, "Current Location - Latitude: " + locationManager.getLatitude() + ", Longitude: " + locationManager.getLongitude());
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
//            if (!locationSource.isActivated()) {
//                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
//            }
//        }
//    }
//
//    public void sendSubwayRequest() {
//        LocationManager locationManager = LocationManager.getInstance();
//        double locationX = locationManager.getLatitude();
//        double locationY = locationManager.getLongitude();
//        String id = UserManager.getInstance().getUserId();  // Ensure you handle null if UserManager not properly set up
//
//        Log.d(TAG, "Server Request - Latitude: " + locationX + ", Longitude: " + locationY + ", UserId: " + id);
//
//        Retrofit_subway_interface service = Retrofit_subway.getInstance().create(Retrofit_subway_interface.class);
//        Call<xy_model> call = service.test_api_get(id, locationX, locationY);
//        call.enqueue(new Callback<xy_model>() {
//            @Override
//            public void onResponse(Call<xy_model> call, Response<xy_model> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    String subwayName = response.body().getSubwayName();
//                    textView.setText(subwayName + "\n");
//                    Log.d(TAG, "Server Response: " + subwayName);
//                } else {
//                    Log.e(TAG, "Server Response Failed - Error Code: " + response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<xy_model> call, Throwable t) {
//                Log.e(TAG, "Network Request Failed: " + t.getMessage());
//            }
//        });
//    }
//}
