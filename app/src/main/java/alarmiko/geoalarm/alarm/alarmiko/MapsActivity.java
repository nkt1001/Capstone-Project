package alarmiko.geoalarm.alarm.alarmiko;

import android.content.pm.PackageManager;
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
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import alarmiko.geoalarm.alarm.alarmiko.ui.AddressView;
import alarmiko.geoalarm.alarm.alarmiko.utils.CurrentLocationService;
import alarmiko.geoalarm.alarm.alarmiko.utils.PermissionUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraIdleListener, AddressView.AddressLoaderListener {

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

    @BindView(R.id.address_view) AddressView mAddressView;

    private static final int ANIMATION_DURATION = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_maps);

        ButterKnife.bind(this);

        mAddressView.addOnAddressLoadedListener(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);

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
                        return;
                    }
                    mImvCenterMarker.animate().alpha(1f).setDuration(ANIMATION_DURATION).start();
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
        mAddressView.loadAddress(latLng);
    }

    private void hideViews() {
        mAddressView.hide();
        mBtnChangeAddress.animate().alpha(0f).setDuration(ANIMATION_DURATION).start();
        mBtnOkAddress.animate().alpha(0f).setDuration(ANIMATION_DURATION).start();
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

    @Override
    public void onAddressLoaded(String address) {
        mBtnChangeAddress.animate().alpha(1f).setDuration(ANIMATION_DURATION).start();
        mBtnOkAddress.animate().alpha(1f).setDuration(ANIMATION_DURATION).start();
    }
}
