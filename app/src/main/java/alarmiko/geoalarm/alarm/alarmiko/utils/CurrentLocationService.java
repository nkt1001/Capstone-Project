package alarmiko.geoalarm.alarm.alarmiko.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import alarmiko.geoalarm.alarm.alarmiko.Alarmiko;

/**
 * Created by nkt01 on 19.03.2017.
 * Service that helping to get current user {@link Location location}.
 */

public class CurrentLocationService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "CurrentLocationService";

    private Context mContext;
    private GoogleApiClient mGoogleServices;
    private CurrentLocationServiceCallback mCallback;
    private LocationRequest mLocationRequest;

    public CurrentLocationService(Context context, @NonNull CurrentLocationServiceCallback callback) {

        this.mContext = context;
        mCallback = callback;

        mGoogleServices = new GoogleApiClient.Builder(mContext, this, this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        createLocationRequest();
    }

    public void startGettingLocation() {
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
        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleServices, mLocationRequest, this, null);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
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
