package alarmiko.geoalarm.alarm.alarmiko.alarms.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import alarmiko.geoalarm.alarm.alarmiko.alarms.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.alarms.data.AlarmCursor;
import alarmiko.geoalarm.alarm.alarmiko.alarms.data.AlarmsTableManager;
import alarmiko.geoalarm.alarm.alarmiko.alarms.misc.AlarmController;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;


public class PendingAlarmScheduler extends BroadcastReceiver {
    // We include the class name in the string to distinguish this constant from the one defined
    // in UpcomingAlarmReceiver.
    public static final String EXTRA_ALARM_ID = "alarms.background.PendingAlarmScheduler.extra.ALARM_ID";

    @Override
    public void onReceive(final Context context, Intent intent) {
        final long id = intent.getLongExtra(EXTRA_ALARM_ID, -1);
        if (id < 0) {
            throw new IllegalStateException("No alarm id received");
        }
        // Start our own thread to load the alarm instead of:
        //  * using a Loader, because we have no complex lifecycle and thus
        //  BroadcastReceiver has no built-in LoaderManager, AND getting a Loader
        //  to work here might be a hassle, let alone it might not even be appropriate to
        //  use Loaders outside of an Activity/Fragment, since it does depend on LoaderCallbacks.
        //  * using an AsyncTask, because we don't need to do anything on the UI thread
        //  after the background work is complete.
        // TODO: Verify using a Runnable like this won't cause a memory leak.
        // It *probably* won't because a BroadcastReceiver doesn't hold a Context,
        // and it also doesn't have a lifecycle, so it likely won't stick around
        // in memory.
        new Thread(new Runnable() {
            @Override
            public void run() {
                AlarmCursor cursor = new AlarmsTableManager(context).queryItem(id);
                Alarm alarm = checkNotNull(cursor.getItem());
                if (!alarm.isEnabled()) {
                    throw new IllegalStateException("Alarm must be enabled!");
                }
                alarm.ignoreUpcomingRingTime(false); // allow #ringsWithinHours() to behave normally
                // No UI work is done
                AlarmController controller = new AlarmController(context, null);
                controller.scheduleAlarm(alarm, false);
                controller.save(alarm);
            }
        }).start();
    }
}
