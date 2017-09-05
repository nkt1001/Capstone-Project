
package alarmiko.geoalarm.alarm.alarmiko.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.iid.FirebaseInstanceId;

import alarmiko.geoalarm.alarm.alarmiko.R;
import alarmiko.geoalarm.alarm.alarmiko.ads.AdConfig;
import alarmiko.geoalarm.alarm.alarmiko.utils.CurrentLocationService;
import alarmiko.geoalarm.alarm.alarmiko.utils.ErrorReceiver;
import alarmiko.geoalarm.alarm.alarmiko.utils.ErrorUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity implements ErrorReceiver.ErrorHandler,
        CurrentLocationService.CurrentLocationServiceCallback {

    private static final String TAG = "BaseActivity";

    @Nullable
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private Menu mMenu;

    private ErrorReceiver mErrorReceiver;

    private CurrentLocationService mCurrentLocation;

    protected InterstitialAd mInterstitial;

    @LayoutRes
    protected abstract int layoutResId();
    @MenuRes
    protected abstract int menuResId();

    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize the associated SharedPreferences file with default values
        // for each preference when the user first opens your application.
        // When false, the system sets the default values only if this method has
        // never been called in the past (or the KEY_HAS_SET_DEFAULT_VALUES in the
        // default value shared preferences file is false).

//        {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // ========================================================================================
        // TOneverDO: Set theme after setContentView()
        // ========================================================================================
        setContentView(layoutResId());
        // Direct volume changes to the alarm stream
        setVolumeControlStream(AudioManager.STREAM_ALARM);
        ButterKnife.bind(this);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(isDisplayHomeUpEnabled());
                ab.setDisplayShowTitleEnabled(isDisplayShowTitleEnabled());
            }
        }
//        }

        mErrorReceiver = new ErrorReceiver(this);
        mCurrentLocation = new CurrentLocationService(this, this);

        if (AdConfig.isEnabled) {
            MobileAds.initialize(this, AdConfig.APP_ID);

            initInterstitial();
            requestNewInterstitial();
        }

        Log.d(TAG, "onCreate: " + FirebaseInstanceId.getInstance().getToken());
    }

    @Override
    public void currentLocation(@Nullable LatLng location, boolean isConnected) {
        if (mCurrentLocation != null) {
            mCurrentLocation.stopGettingLocation();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ErrorReceiver.ACTION_ERROR_RECEIVED);
        registerReceiver(mErrorReceiver, filter);

        mCurrentLocation.startGettingLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkGooglePlayServicesAvailable(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Alarmiko.updateWidget(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mCurrentLocation.stopGettingLocation();
        unregisterReceiver(mErrorReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ErrorUtils.ErrorData.RESOLUTION_ERROR_CODE_LOCATION_UPDATE){
            if (resultCode == RESULT_OK) {
                mCurrentLocation.startGettingLocation();
            } else {
                ErrorUtils.sendBroadcastError(this, ErrorUtils.ErrorData.CRITICAL_ERROR_CODE_LOCATION_UPDATE, null);
            }
        } else if (requestCode == ErrorUtils.ErrorData.RESOLUTION_ERROR_CODE_CONNECT_GOOGLE_SERVICES
                || requestCode == ErrorUtils.ErrorData.RESOLUTION_ERROR_CODE_GOOGLE_SERVICES){
            if (resultCode == RESULT_OK) {
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initInterstitial() {
        mInterstitial = new InterstitialAd(getApplicationContext());
        mInterstitial.setAdUnitId(AdConfig.INTERSTITIAL_ID);
        mInterstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });
    }

    private void requestNewInterstitial() {
        if (mInterstitial != null) {
            AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
            mInterstitial.loadAd(adRequest);
        }
    }

    protected void showInterstitial() {
        if (mInterstitial != null && mInterstitial.isLoaded()) {
            mInterstitial.show();
        }
    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        if (menuResId() != 0) {
            getMenuInflater().inflate(menuResId(), menu);
            mMenu = menu;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Nullable
    public final Menu getMenu() {
        return mMenu;
    }

    protected boolean isDisplayHomeUpEnabled() {
        return true;
    }

    protected boolean isDisplayShowTitleEnabled() {
        return false;
    }

    protected boolean checkGooglePlayServicesAvailable(Activity activity) {
        Log.d(TAG, "checkGooglePlayServicesAvailable() called with: activity = [" + activity + "]");

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                ErrorUtils.sendBroadcastError(activity, ErrorUtils.ErrorData.RESOLUTION_ERROR_CODE_GOOGLE_SERVICES, null);
            } else {
                ErrorUtils.sendBroadcastError(activity, ErrorUtils.ErrorData.CRITICAL_ERROR_CODE_GOOGLE_SERVICES, null);
            }
            return false;
        }

        return true;
    }

    @Override
    public void connectionError(int errorCode, ConnectionResult status) {
        Log.d(TAG, "connectionError() called with: errorCode = [" + errorCode + "], status = [" + status + "]");

        if (ErrorUtils.ErrorData.CRITICAL_ERROR_CODE_CONNECT_GOOGLE_SERVICES == errorCode) {
            if (!isFinishing()) {
                int statusCode = status == null ? -4 : status.getErrorCode();
                String description = ErrorUtils
                        .generateErrorDescription(BaseActivity.this, "Connecting to GOOGLE SERVICES", statusCode);
                ErrorUtils.navigateToErrorActivity(BaseActivity.this, description);
            }
        } else if (ErrorUtils.ErrorData.RESOLUTION_ERROR_CODE_CONNECT_GOOGLE_SERVICES == errorCode && status.hasResolution()) {
            try {
                status.startResolutionForResult(BaseActivity.this, errorCode);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void criticalError(int errorCode, Status status) {
        Log.d(TAG, "criticalError() called with: errorCode = [" + errorCode + "], status = [" + status + "]");

        if (isFinishing()) {
            return;
        }

        String description = null;
        int statusCode = status == null ? -1 : status.getStatusCode();

        switch (errorCode) {
            case ErrorUtils.ErrorData.CRITICAL_ERROR_CODE_GOOGLE_SERVICES:
                description = "Google Services are not available";
                break;
            case ErrorUtils.ErrorData.CRITICAL_ERROR_CODE_GEOFENCE_REGISTER:
                description = ErrorUtils.generateErrorDescription(BaseActivity.this, "Registering GEO Alarm", statusCode);
                break;
            case ErrorUtils.ErrorData.CRITICAL_ERROR_CODE_LOCATION_UPDATE:
                description = getString(R.string.location_update_error_message);
                break;
        }

        if (description != null) {
            ErrorUtils.navigateToErrorActivity(BaseActivity.this, description);
        }

        this.finish();
    }

    @Override
    public void handleError(int errorCode, @Nullable Status status) {
        Log.d(TAG, "handleError() called with: errorCode = [" + errorCode + "], status = [" + status + "]");

        if (status != null && status.hasResolution()) {
            try {
                status.startResolutionForResult(BaseActivity.this, errorCode);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else if (errorCode == ErrorUtils.ErrorData.RESOLUTION_ERROR_CODE_GOOGLE_SERVICES) {
            GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
            int statusCode = googleApiAvailability.isGooglePlayServicesAvailable(BaseActivity.this);
            googleApiAvailability.getErrorDialog(BaseActivity.this, statusCode, errorCode);
        }
    }
}
