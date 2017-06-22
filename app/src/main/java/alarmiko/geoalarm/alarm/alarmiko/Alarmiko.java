package alarmiko.geoalarm.alarm.alarmiko;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import io.fabric.sdk.android.Fabric;

public class Alarmiko extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Answers(), new Crashlytics());
    }
}
