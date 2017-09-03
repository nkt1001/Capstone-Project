
package alarmiko.geoalarm.alarm.alarmiko.ringtone;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import alarmiko.geoalarm.alarm.alarmiko.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.R;
import alarmiko.geoalarm.alarm.alarmiko.misc.AlarmController;
import alarmiko.geoalarm.alarm.alarmiko.ringtone.playback.AlarmRingtoneService;
import alarmiko.geoalarm.alarm.alarmiko.ringtone.playback.RingtoneService;
import alarmiko.geoalarm.alarm.alarmiko.utils.TimeFormatUtils;

public class AlarmActivity extends RingtoneActivity<Alarm> {
    private static final String TAG = "AlarmEditActivity";

    private AlarmController mAlarmController;
    private NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        mAlarmController = new AlarmController(this, null);
        // TODO: If the upcoming alarm notification isn't present, verify other notifications aren't affected.
        // This could be the case if we're starting a new instance of this activity after leaving the first launch.
        mAlarmController.removeUpcomingAlarmNotification(getRingingObject());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void finish() {
        super.finish();
        // If the presently ringing alarm is about to be superseded by a successive alarm,
        // this, unfortunately, will cancel the missed alarm notification for the presently
        // ringing alarm.
        //
        // A workaround is to override onNewIntent() and post the missed alarm notification again,
        // AFTER calling through to its base implementation, because it calls finish().
        mNotificationManager.cancel(TAG, getRingingObject().getIntId());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // -------------- TOneverDO: precede super ---------------
        // Even though the base implementation calls finish() on this instance and starts a new
        // instance, this instance will still be alive with all of its member state intact at
        // this point. So this notification will still refer to the Alarm that was just missed.
        postMissedAlarmNote();
    }

    @Override
    protected Class<? extends RingtoneService> getRingtoneServiceClass() {
        return AlarmRingtoneService.class;
    }

    @Override
    protected CharSequence getHeaderTitle() {
        return getRingingObject().label();
    }

    @Override
    protected void getHeaderContent(ViewGroup parent) {
        if (getRingingObject().isGeo()) {
            TextView addressView = (TextView) getLayoutInflater().inflate(R.layout.content_header_geo_alarm_activity, parent, false);
            addressView.setText(getRingingObject().address());
            parent.addView(addressView);
        } else {
            getLayoutInflater().inflate(R.layout.content_header_alarm_activity, parent, true);
        }
    }

    @Override
    protected int getAutoSilencedText() {
        return R.string.alarm_auto_silenced_text;
    }

    @Override
    protected int getLeftButtonText() {
        return !getRingingObject().isGeo() ? R.string.snooze : getRingingObject().hasRecurrence() ? R.string.skip : 0;
    }

    @Override
    protected int getRightButtonText() {return (!getRingingObject().isGeo() || !getRingingObject().hasRecurrence())
            ? R.string.dismiss : R.string.dismiss_today;
    }

    @Override
    protected int getLeftButtonDrawable() {
        return R.drawable.ic_snooze_48dp;
    }

    @Override
    protected int getRightButtonDrawable() {
        return R.drawable.ic_dismiss_alarm_48dp;
    }

    @Override
    protected void onLeftButtonClick() {
        if (!getRingingObject().isGeo()) {
            mAlarmController.snoozeAlarm(getRingingObject());
        }
        // Can't call dismiss() because we don't want to also call cancelAlarm()! Why? For example,
        // we don't want the alarm, if it has no recurrence, to be turned off right now.
        stopAndFinish();
    }

    @Override
    protected void onRightButtonClick() {
        // TODO do we really need to cancel the intent and alarm?
        if (getRingingObject().isGeo()) {
            mAlarmController.cancelGeo(getRingingObject(), false, false);
        } else {
            mAlarmController.cancelAlarm(getRingingObject(), false, true);
        }
        stopAndFinish();
    }

    @Override
    protected Parcelable.Creator<Alarm> getParcelableCreator() {
        return Alarm.CREATOR;
    }

    // TODO: Consider changing the return type to Notification, and move the actual
    // task of notifying to the base class.
    @Override
    protected void showAutoSilenced() {
        super.showAutoSilenced();
        postMissedAlarmNote();
    }

    private void postMissedAlarmNote() {
        String alarmTime = TimeFormatUtils.formatTime(this,
                getRingingObject().hour(), getRingingObject().minutes());
        Notification note = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.missed_alarm))
                .setContentText(alarmTime)
                .setSmallIcon(R.drawable.ic_alarm_24dp)
                .build();
        mNotificationManager.notify(TAG, getRingingObject().getIntId(), note);
    }
}
