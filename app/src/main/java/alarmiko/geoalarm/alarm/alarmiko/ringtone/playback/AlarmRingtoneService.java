
package alarmiko.geoalarm.alarm.alarmiko.ringtone.playback;

import android.app.Notification;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import alarmiko.geoalarm.alarm.alarmiko.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.R;
import alarmiko.geoalarm.alarm.alarmiko.misc.AlarmController;
import alarmiko.geoalarm.alarm.alarmiko.misc.AlarmPreferences;

import static alarmiko.geoalarm.alarm.alarmiko.utils.TimeFormatUtils.formatTime;

public class AlarmRingtoneService extends RingtoneService<Alarm> {
    private static final String TAG = "AlarmRingtoneService";
    private static final String ACTION_SNOOZE = "ringtone.action.SNOOZE";
    private static final String ACTION_DISMISS = "ringtone.action.DISMISS";

    private AlarmController mAlarmController;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We can have this before super because this will only call through
        // WHILE this Service has already been alive.
        if (intent.getAction() != null) {
            if (ACTION_SNOOZE.equals(intent.getAction())) {
                if (!getRingingObject().isGeo()) {
                    mAlarmController.snoozeAlarm(getRingingObject());
                }
            } else if (ACTION_DISMISS.equals(intent.getAction())) {
                if (getRingingObject().isGeo()) {
                    mAlarmController.cancelGeo(getRingingObject(), false, false);
                } else {
                    mAlarmController.cancelAlarm(getRingingObject(), false, true); // TODO do we really need to cancel the intent and alarm?
                }
            } else {
                throw new UnsupportedOperationException();
            }
            // ==========================================================================
            stopSelf(startId);
            finishActivity();
        }
        return super.onStartCommand(intent, flags, startId);
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        mAlarmController = new AlarmController(this, null);
    }

    @Override
    protected void onAutoSilenced() {
        // TODO do we really need to cancel the alarm and intent?
        if (getRingingObject().isGeo()) {
            mAlarmController.cancelGeo(getRingingObject(), false, false);
        } else {
            mAlarmController.cancelAlarm(getRingingObject(), false, true);
        }
    }

    @Override
    protected Uri getRingtoneUri() {
        String ringtone = getRingingObject().ringtone();
        // can't be null...
        if (ringtone.isEmpty()) {
            return Settings.System.DEFAULT_ALARM_ALERT_URI;
        }
        return Uri.parse(ringtone);
    }

    @Override
    protected Notification getForegroundNotification() {
        String title = getRingingObject().label().isEmpty()
                ? getString(R.string.alarm)
                : getRingingObject().label();
        return new NotificationCompat.Builder(this)
                // Required contents
                .setSmallIcon(R.drawable.ic_alarm_24dp)
                .setContentTitle(title)
                .setContentText(formatTime(this, System.currentTimeMillis()))
                .addAction(R.drawable.ic_snooze_24dp,
                        getString(R.string.snooze),
                        getPendingIntent(ACTION_SNOOZE, getRingingObject().getIntId()))
                .addAction(R.drawable.ic_dismiss_alarm_24dp,
                        getString(R.string.dismiss),
                        getPendingIntent(ACTION_DISMISS, getRingingObject().getIntId()))
                .build();
    }

    @Override
    protected boolean doesVibrate() {
        return getRingingObject().vibrates();
    }

    @Override
    protected int minutesToAutoSilence() {
        return AlarmPreferences.minutesToSilenceAfter(this);
    }

    @Override
    protected Parcelable.Creator<Alarm> getParcelableCreator() {
        return Alarm.CREATOR;
    }
}
