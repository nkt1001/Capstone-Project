package alarmiko.geoalarm.alarm.alarmiko.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

/**
 * Created by nkt01 on 19.03.2017.
 * Service that helping to get current user {@link Location location}.
 */

public class CurrentLocationService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "CurrentLocationService";

    private Context mContext;
    private GoogleApiClient mGoogleServices;
    private CurrentLocationServiceCallback mCallback;

    public CurrentLocationService(Context context) {
        this.mContext = context;
        mGoogleServices = new GoogleApiClient.Builder(mContext, this, this)
                .addApi(Awareness.API)
                .build();
    }

    public void getMyLocation(@NonNull CurrentLocationServiceCallback callback) {
        Log.d(TAG, "getMyLocation: ");
        mCallback = callback;

        mGoogleServices.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Awareness.SnapshotApi.getLocation(mGoogleServices)
                    .setResultCallback(new ResultCallback<LocationResult>() {
                        @Override
                        public void onResult(@NonNull LocationResult locationResult) {
                            if (!locationResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Could not get location.");
                                mCallback.currentLocation(null);
                                return;
                            }
                            Location location = locationResult.getLocation();
                            Log.i(TAG, "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());

                            mCallback.currentLocation(location);
                            mGoogleServices.disconnect();
                        }
                    });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called with: i = [" + i + "]");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mCallback.currentLocation(null);
    }

    public interface CurrentLocationServiceCallback {
        void currentLocation(@Nullable Location location);
    }
}
