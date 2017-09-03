package alarmiko.geoalarm.alarm.alarmiko.ui;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.facebook.stetho.Stetho;
import com.google.android.gms.maps.model.LatLng;

import alarmiko.geoalarm.alarm.alarmiko.BuildConfig;
import alarmiko.geoalarm.alarm.alarmiko.widget.AlarmikoWidget;
import io.fabric.sdk.android.Fabric;

public class Alarmiko extends Application {

    private static final String TAG = "Alarmiko";

    @Nullable
    private static LatLng mCurrentLocation;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
        Fabric.with(this, new Answers(), new Crashlytics());
    }

    public static void setCurrentLocation(LatLng currentLocation) {
        mCurrentLocation = currentLocation;
    }

    public static LatLng getCurrentLocation() {
        return mCurrentLocation;
    }

    public static void updateWidget(Context context) {
        Intent intent = new Intent(AlarmikoWidget.WIDGET_REFRESH_ACTION);
        context.sendBroadcast(intent);
    }
}
