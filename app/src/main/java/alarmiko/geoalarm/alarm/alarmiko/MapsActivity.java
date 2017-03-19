package alarmiko.geoalarm.alarm.alarmiko;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import alarmiko.geoalarm.alarm.alarmiko.utils.CurrentLocationService;
import alarmiko.geoalarm.alarm.alarmiko.utils.PermissionUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraIdleListener {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;

    private static final String TAG = "MapsActivity";

    @BindView(R.id.imv_center_marker) ImageView mImvCenterMarker;
    @BindView(R.id.btn_find_address) Button mBtnChangeAddress;
    @BindView(R.id.btn_ok_address) Button mBtnOkAddress;

    @BindView(R.id.tv_cur_address) TextView mTvCurrentAddress;

    private Geocoder mGeocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_maps);

        ButterKnife.bind(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGeocoder = new Geocoder(this, Locale.getDefault());
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraMoveListener(this);

        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);

        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

            new CurrentLocationService(this).getMyLocation(new CurrentLocationService.CurrentLocationServiceCallback() {
                @Override
                public void currentLocation(Location location) {
                    if (location == null) {
                        mImvCenterMarker.setVisibility(View.GONE);
                        return;
                    }
                    mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17f));
                }
            });
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onCameraIdle() {
        Log.d(TAG, "onCameraIdle: ");
        LatLng latLng = mMap.getCameraPosition().target;
        showView(latLng);
    }

    private void showView(LatLng latLng)  {

        try {
            List<Address> address = mGeocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (address.size() > 0) {

                mTvCurrentAddress.setText(address.get(0).getAddressLine(0));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
//
        mBtnChangeAddress.setVisibility(View.VISIBLE);
        mBtnOkAddress.setVisibility(View.VISIBLE);
        mTvCurrentAddress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCameraMove() {
        Log.d(TAG, "onCameraMove: ");
//        hideViews();
    }

    private void hideViews() {

        mBtnChangeAddress.setVisibility(View.GONE);
        mBtnOkAddress.setVisibility(View.GONE);
        mTvCurrentAddress.setVisibility(View.GONE);
    }

    public void okClick(View view) {
        Toast.makeText(this, "hello ok", Toast.LENGTH_SHORT).show();
    }

    public void changeAddressClick(View view) {
        Toast.makeText(this, "change address", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraMoveStarted(int i) {
        hideViews();
    }
}
