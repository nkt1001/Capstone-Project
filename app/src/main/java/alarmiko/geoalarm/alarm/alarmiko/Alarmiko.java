package alarmiko.geoalarm.alarm.alarmiko;

import android.app.Application;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.facebook.stetho.Stetho;
import com.google.android.gms.maps.model.LatLng;

import io.fabric.sdk.android.Fabric;

public class Alarmiko extends Application {

    @Nullable
    private static LatLng mCurrentLocation;

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        Fabric.with(this, new Answers(), new Crashlytics());
    }

    public static void setCurrentLocation(LatLng currentLocation) {
        mCurrentLocation = currentLocation;
    }

    public static LatLng getCurrentLocation() {
        return mCurrentLocation;
    }
}
