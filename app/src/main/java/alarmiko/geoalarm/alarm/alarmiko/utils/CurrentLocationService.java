package alarmiko.geoalarm.alarm.alarmiko.utils;

import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;

import alarmiko.geoalarm.alarm.alarmiko.Alarmiko;

public class CurrentLocationService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "CurrentLocationService";

    public static final int RESOLUTION_REQUEST = 611;

    private Activity mActivity;
    private GoogleApiClient mGoogleServices;
    private CurrentLocationServiceCallback mCallback;
    private LocationRequest mLocationRequest;

    public CurrentLocationService(Activity activity, @NonNull CurrentLocationServiceCallback callback) {

        this.mActivity = activity;
        mCallback = callback;

        mGoogleServices = new GoogleApiClient.Builder(mActivity, this, this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        createLocationRequest();
    }

    public void startGettingLocation() {
        Log.d(TAG, "startGettingLocation: ");
        if (mGoogleServices.isConnected()) {
            requestLocationUpdates();
        } else {
            mGoogleServices.connect();
        }
    }

    public void stopGettingLocation() {
        if (mGoogleServices.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleServices, this);
            mGoogleServices.disconnect();
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void requestLocationUpdates() {


            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            builder.setAlwaysShow(true);
            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleServices, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                    final Status status = locationSettingsResult.getStatus();

                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            startLocationRequest();
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            ErrorUtils.sendBroadcastError(mActivity,
                                    ErrorUtils.ErrorData.RESOLUTION_ERROR_CODE_LOCATION_UPDATE, status);
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            ErrorUtils.sendBroadcastError(mActivity, ErrorUtils.ErrorData.CRITICAL_ERROR_CODE_LOCATION_UPDATE, status);
                            break;
                    }
                }
            });
    }

    private void startResolutionForResult(Status status) {
        try {
            status.startResolutionForResult(
                    mActivity, RESOLUTION_REQUEST);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    public void startLocationRequest() {
        if (ContextCompat.checkSelfPermission(mActivity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleServices, mLocationRequest, CurrentLocationService.this, null);
        }
    }

    @Nullable
    public GoogleApiClient getGoogleServices() {
        if (mGoogleServices.isConnected()) {
            return mGoogleServices;
        }
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: ");
        mCallback.currentLocation(null, false);
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        Alarmiko.setCurrentLocation(latLng);
        mCallback.currentLocation(latLng, true);
    }

    public interface CurrentLocationServiceCallback {
        void currentLocation(@Nullable LatLng location, boolean isConnected);
    }
}
