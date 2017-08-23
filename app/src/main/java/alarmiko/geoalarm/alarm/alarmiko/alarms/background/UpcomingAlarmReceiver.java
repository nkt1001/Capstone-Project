package alarmiko.geoalarm.alarm.alarmiko.alarms.background;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import alarmiko.geoalarm.alarm.alarmiko.R;
import alarmiko.geoalarm.alarm.alarmiko.alarms.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.alarms.misc.AlarmController;
import alarmiko.geoalarm.alarm.alarmiko.utils.ContentIntentUtils;
import alarmiko.geoalarm.alarm.alarmiko.utils.ParcelableUtil;

import static alarmiko.geoalarm.alarm.alarmiko.utils.TimeFormatUtils.formatTime;


public class UpcomingAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "UpcomingAlarmReceiver";
    private static final String ACTION_DISMISS_NOW = "action.DISMISS_NOW";

    public static final String ACTION_CANCEL_NOTIFICATION = "action.CANCEL_NOTIFICATION";
    public static final String ACTION_SHOW_SNOOZING = "action.SHOW_SNOOZING";
    public static final String EXTRA_ALARM = "extra.ALARM";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final byte[] alarmBytes = intent.getByteArrayExtra(EXTRA_ALARM);
        // Unmarshall the bytes into a parcel and create our Alarm with it.
        final Alarm alarm = ParcelableUtil.unmarshall(alarmBytes, Alarm.CREATOR);
        if (alarm == null) {
            throw new IllegalStateException("No alarm received");
        }

        final long id = alarm.getId();
        final NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        final boolean actionShowSnoozing = ACTION_SHOW_SNOOZING.equals(intent.getAction());
        if (intent.getAction() == null || actionShowSnoozing) {
            // Prepare notification
            // http://stackoverflow.com/a/15803726/5055032
            // Notifications aren't updated on the UI thread, so we could have
            // done this in the background. However, no lengthy operations are
            // done here, so doing so is a premature optimization.
            String title;
            String text;
            if (actionShowSnoozing) {
                if (!alarm.isSnoozed()) {
                    throw new IllegalStateException("Can't show snoozing notif. if alarm not snoozed!");
                }
                title = alarm.label().isEmpty() ? context.getString(R.string.alarm) : alarm.label();
                text = context.getString(R.string.title_snoozing_until,
                        formatTime(context, alarm.snoozingUntil()));
            } else {
                // No intent action required for default behavior
                title = context.getString(R.string.upcoming_alarm);
                text = formatTime(context, alarm.ringsAt());
                if (!alarm.label().isEmpty()) {
                    text = alarm.label() + ", " + text;
                }
            }

            Intent dismissIntent = new Intent(context, UpcomingAlarmReceiver.class)
                    .setAction(ACTION_DISMISS_NOW)
                    .putExtra(EXTRA_ALARM, ParcelableUtil.marshall(alarm));
            PendingIntent piDismiss = PendingIntent.getBroadcast(context, (int) id,
                    dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            Notification note = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_alarm_24dp)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(ContentIntentUtils.create(context, id))
                    .addAction(R.drawable.ic_dismiss_alarm_24dp,
                            context.getString(R.string.dismiss_now), piDismiss)
                    .build();
            nm.notify(TAG, (int) id, note);
        } else if (ACTION_CANCEL_NOTIFICATION.equals(intent.getAction())) {
            nm.cancel(TAG, (int) id);
        } else if (ACTION_DISMISS_NOW.equals(intent.getAction())) {
            if (alarm.isGeo()) {
                new AlarmController(context, null).cancelGeo(alarm, false, false);
            } else {
                new AlarmController(context, null).cancelAlarm(alarm, false, true);
            }
        }
    }
}
