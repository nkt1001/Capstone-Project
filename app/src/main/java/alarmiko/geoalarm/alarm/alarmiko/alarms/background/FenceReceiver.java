package alarmiko.geoalarm.alarm.alarmiko.alarms.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.awareness.fence.FenceState;

import java.util.concurrent.TimeUnit;

import alarmiko.geoalarm.alarm.alarmiko.alarms.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.alarms.ringtone.AlarmActivity;
import alarmiko.geoalarm.alarm.alarmiko.utils.ContentIntentUtils;
import alarmiko.geoalarm.alarm.alarmiko.utils.ParcelableUtil;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.getActivity;

public class FenceReceiver extends BroadcastReceiver {

    public static final String FENCE_ACTION = "alarmiko.geoalarm.alarm.alarmiko.alarms.background.action.FenceReceiver.ACTION_FENCE";

    private static final String TAG = "FenceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        FenceState fenceState = FenceState.extract(intent);

        Log.d(TAG, "onReceive: fence key " + fenceState.getFenceKey());
        switch (fenceState.getCurrentState()) {
            case FenceState.TRUE:
                if (!intent.hasExtra(AlarmActivity.EXTRA_RINGING_OBJECT)) {
                    break;
                }

                Alarm alarm = intent.getParcelableExtra(AlarmActivity.EXTRA_RINGING_OBJECT);
                if (alarm == null || !alarm.isEnabled() || alarm.isSnoozed()) {
                    break;
                }

                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                PendingIntent alarmIntent = alarmIntent(context, alarm);
                long ringAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(1);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    PendingIntent showIntent = ContentIntentUtils.create(context, alarm.getId());
                    AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(ringAt, showIntent);
                    am.setAlarmClock(info, alarmIntent);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        am.setExact(AlarmManager.RTC_WAKEUP, ringAt, alarmIntent);
                    }
                    // Show alarm in the status bar
                    Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
                    alarmChanged.putExtra("alarmSet", true);
                    context.sendBroadcast(alarmChanged);
                }
                break;
        }
    }

    private PendingIntent alarmIntent(Context context, Alarm alarm) {
        Intent intent = new Intent(context, AlarmActivity.class)
                .putExtra(AlarmActivity.EXTRA_RINGING_OBJECT, ParcelableUtil.marshall(alarm));
        return getActivity(context, alarm.getIntId(), intent, FLAG_CANCEL_CURRENT);
    }
}
