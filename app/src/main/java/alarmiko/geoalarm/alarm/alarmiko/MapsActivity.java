package alarmiko.geoalarm.alarm.alarmiko;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import alarmiko.geoalarm.alarm.alarmiko.alarms.Alarm;
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
        GoogleMap.OnCameraIdleListener, AddressView.AddressLoaderListener, LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 101;
    private static final int ALARM_LOADER = 1001;

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

    @BindView(R.id.imb_menu) ImageButton mMenuButton;

    private static final int ANIMATION_DURATION = 300;

    private CurrentLocationService mCurrentLocationService;

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

        getSupportLoaderManager().initLoader(ALARM_LOADER, null, this);

        mCurrentLocationService = new CurrentLocationService(this, new CurrentLocationService.CurrentLocationServiceCallback() {
            @Override
            public void currentLocation(@Nullable LatLng location, boolean isConnected) {
                Log.d(TAG, "currentLocation() called with: location = [" + location + "], isConnected = [" + isConnected + "]");

                if (!isConnected) {
                    return;
                }

                if (location != null) {
                    mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(location, 17f));
                    mCurrentLocationService.stopGettingLocation();
                }
            }
        });
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

    @Override
    protected void onStop() {
        super.onStop();
        mCurrentLocationService.stopGettingLocation();
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

            mImvCenterMarker.animate().alpha(1f).setDuration(ANIMATION_DURATION).start();
            mCurrentLocationService.startGettingLocation();
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
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
        Intent intent = new Intent(MapsActivity.this, AlarmEditActivity.class);

        String address = mAddressView.getAddressString();
        if (address.isEmpty()) {
            address = getString(R.string.unknown_address);
        }

        Alarm alarm = Alarm.builder()
                .hour(-1)
                .minutes(-1)
                .address(address)
                .coordinates(mMap.getCameraPosition().target)
                .build();
        Log.d(TAG, "okClick: " + alarm);

        intent.setAction(AlarmEditActivity.ACTION_EDIT_ALARM)
                .putExtra(AlarmEditActivity.EXTRA_PICKED_ADDRESS, alarm);
        startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + place.getName());
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

    public void changeAddressClick(View view) {
        Toast.makeText(this, "change address", Toast.LENGTH_SHORT).show();
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

    public void menuClick(View view) {
        Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show();
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
        mBtnChangeAddress.animate().alpha(1f).setDuration(ANIMATION_DURATION).start();
        mBtnOkAddress.animate().alpha(1f).setDuration(ANIMATION_DURATION).start();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
