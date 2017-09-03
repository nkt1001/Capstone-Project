package alarmiko.geoalarm.alarm.alarmiko.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import alarmiko.geoalarm.alarm.alarmiko.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.R;
import alarmiko.geoalarm.alarm.alarmiko.background.OnBootUpAlarmScheduler;
import alarmiko.geoalarm.alarm.alarmiko.db.AlarmCursor;
import alarmiko.geoalarm.alarm.alarmiko.db.AlarmsListCursorLoader;
import alarmiko.geoalarm.alarm.alarmiko.utils.ErrorUtils;
import alarmiko.geoalarm.alarm.alarmiko.utils.PermissionUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends BaseActivity implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnCameraIdleListener, AddressView.AddressLoaderListener, LoaderManager.LoaderCallbacks<AlarmCursor> {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 101;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;

    private static final String TAG = "MapsActivity";

    @BindView(R.id.imv_center_marker)
    ImageView mImvCenterMarker;

    @BindView(R.id.btn_find_address)
    Button mBtnChangeAddress;
    @BindView(R.id.btn_ok_address)
    Button mBtnOkAddress;

    @BindView(R.id.address_view)
    AddressView mAddressView;

    @BindView(R.id.imb_menu)
    ImageButton mMenuButton;
    @BindView(R.id.imb_location)
    ImageButton mLocationButton;
    private Handler mMarkerAddHandler;
    private List<Alarm> mAlarms;
    private LatLng mLastKnownLocation;

    private static final int ANIMATION_DURATION = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        refreshAlarms();

        mBtnOkAddress.setPaintFlags(mBtnOkAddress.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        mBtnChangeAddress.setPaintFlags(mBtnChangeAddress.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        mAddressView.addOnAddressLoadedListener(this);

        mLastKnownLocation = null;

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mMarkerAddHandler = new Handler();
        mAlarms = new ArrayList<>();

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void refreshAlarms() {
        Intent intent = new Intent(this, OnBootUpAlarmScheduler.class);
        startService(intent);
    }

    private void moveCamera(@Nullable LatLng latLng, float zoom) {
        if (latLng != null && mMap != null) {
            mMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(latLng, zoom));
        }
    }

    private void animateCamera(@Nullable LatLng location) {
        if (location != null && mMap != null) {
            mMap.animateCamera(CameraUpdateFactory
                    .newLatLng(location));
        }
    }

    @Override
    public void currentLocation(@Nullable LatLng location, boolean isConnected) {
        if (mLastKnownLocation == null && mMap != null) {
            moveCamera(location, 17f);
        } else if (mMap == null) {
            mLastKnownLocation = location;
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);

        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

        fillMapWithMarkers(mAlarms);

        enableMyLocation();
    }

    private LatLng getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }

        if (bestLocation == null) {
            return null;
        }
        LatLng result = new LatLng(bestLocation.getLatitude(), bestLocation.getLongitude());
        mLastKnownLocation = result;
        return result;
    }

    @OnClick(R.id.imb_location)
    void onMyLocationClicked() {
        LatLng currentLocation = Alarmiko.getCurrentLocation();
        if (currentLocation != null) {
            animateCamera(currentLocation);
        } else if (getLastKnownLocation() != null){
            animateCamera(getLastKnownLocation());
        } else {
            ErrorUtils.sendBroadcastError(this, ErrorUtils.ErrorData.CRITICAL_ERROR_CODE_LOCATION_UPDATE, null);
        }
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
            LatLng currentLocation = Alarmiko.getCurrentLocation();
            if (currentLocation != null) {
                moveCamera(currentLocation, 17f);
            } else if (getLastKnownLocation() != null){
                moveCamera(getLastKnownLocation(), 17f);
            }
            mImvCenterMarker.animate().alpha(1f).setDuration(ANIMATION_DURATION).start();
        }
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

    @Override
    protected int layoutResId() {
        return R.layout.activity_maps;
    }

    @Override
    protected int menuResId() {
        return 0;
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onCameraIdle() {
        LatLng latLng = mMap.getCameraPosition().target;
        mAddressView.loadAddress(latLng);
    }

    private void hideViews() {
        mAddressView.hide();
        mBtnChangeAddress.animate().alpha(0f).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBtnChangeAddress.setVisibility(View.INVISIBLE);
            }
        }).start();
        mBtnOkAddress.animate().alpha(0f).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBtnOkAddress.setVisibility(View.INVISIBLE);
            }
        }).start();
    }

    private void fillMapWithMarkers(List<Alarm> alarms) {
        if (mMap == null || alarms == null) {
            return;
        }

//        Log.d(TAG, "fillMapWithMarkers() called with: alarms = [" + alarms.size() + "]");

        mMap.clear();

        for (final Alarm alarm : alarms) {
            mMarkerAddHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mMap != null) {

                        MarkerOptions markerOptions = new MarkerOptions()
                                .draggable(false)
                                .position(alarm.coordinates())
                                .title(alarm.address())
                                .snippet(alarm.label());
                        mMap.addMarker(markerOptions);

                        CircleOptions circleOptions = new CircleOptions()
                                .center(alarm.coordinates())
                                .radius(alarm.radius())
                                .strokeWidth(0)
                                .fillColor(ContextCompat.getColor(MapsActivity.this,
                                        alarm.isEnabled() ? R.color.circle_fill_color_map_enabled : R.color.circle_fill_color_map_disabled));

                        mMap.addCircle(circleOptions);
                    }
                }
            }, 50L);
        }
    }

    @OnClick(R.id.btn_ok_address)
    void okClick() {

        String address = mAddressView.getAddressString();
        if (address.isEmpty()) {
            address = getString(R.string.unknown_address);
        }

        Alarm alarm = Alarm.builder()
                .address(address)
                .coordinates(mMap.getCameraPosition().target)
                .build();

        openAddress(alarm, AlarmEditActivity.ACTION_ADD_ALARM);
    }

    private void openAddress(Alarm alarm, String action) {
        Intent intent = new Intent(MapsActivity.this, AlarmEditActivity.class);
        intent.setAction(action)
                .putExtra(AlarmEditActivity.EXTRA_PICKED_ADDRESS, alarm);
        startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                moveCamera(place.getLatLng(), 17f);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void handleError(int errorCode, @Nullable Status status) {
        super.handleError(errorCode, status);

        if (errorCode == ErrorUtils.ErrorData.RESOLUTION_ERROR_CODE_GOOGLE_SERVICES) {
            hideControls();
        }
    }

    @Override
    public void criticalError(int errorCode, Status status) {
        super.criticalError(errorCode, status);
        if (errorCode == ErrorUtils.ErrorData.CRITICAL_ERROR_CODE_GOOGLE_SERVICES) {
            hideControls();
        }
    }

    private void hideControls() {
        if (mMenuButton != null) {
            mMenuButton.setVisibility(View.GONE);
            mLocationButton.setVisibility(View.GONE);
            mBtnOkAddress.setVisibility(View.GONE);
            mBtnChangeAddress.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.btn_find_address)
    void changeAddressClick() {
        // Construct an intent for the place picker
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {

        }
    }

    @OnClick(R.id.imb_menu)
    void menuClick() {
        Intent intent = new Intent(this, AlarmEditActivity.class);
        intent.setAction(AlarmEditActivity.ACTION_ALARM_LIST);
        startActivity(intent);
    }

    @Override
    public void onCameraMoveStarted(int i) {
        hideViews();
    }

    @Override
    public void onAddressLoaded(String address) {

        mBtnChangeAddress.animate().alpha(1f).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mBtnChangeAddress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBtnChangeAddress.setVisibility(View.VISIBLE);
            }
        }).start();
        mBtnOkAddress.animate().alpha(1f).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mBtnOkAddress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBtnOkAddress.setVisibility(View.VISIBLE);
            }
        }).start();
    }

    @Override
    public Loader<AlarmCursor> onCreateLoader(int id, Bundle args) {
        return new AlarmsListCursorLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<AlarmCursor> loader, AlarmCursor data) {
        int count = data == null ? 0 : data.getCount();

        if (count == 0) {
            return;
        }

        if (mAlarms != null && mAlarms.size() > 0) {
            mAlarms.clear();
        }

        data.moveToFirst();
        while (!data.isAfterLast()) {
            if (mAlarms == null) {
                mAlarms = new ArrayList<>();
            }

            if (data.getItem().isGeo()) {
                mAlarms.add(data.getItem());
            }
            data.moveToNext();
        }

        data.close();
        fillMapWithMarkers(mAlarms);
    }

    @Override
    public void onLoaderReset(Loader<AlarmCursor> loader) {

        if (mMap != null) {
            mMap.clear();
        }
        if (mAlarms != null) {
            mAlarms.clear();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (mAlarms == null) {
            return;
        }

        for (Alarm alarm : mAlarms) {
            if (alarm.coordinates().equals(marker.getPosition())) {
                openAddress(alarm, AlarmEditActivity.ACTION_EDIT_ALARM);
                break;
            }
        }
    }
}
