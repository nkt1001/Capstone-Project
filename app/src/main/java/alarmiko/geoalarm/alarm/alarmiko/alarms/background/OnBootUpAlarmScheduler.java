package alarmiko.geoalarm.alarm.alarmiko.alarms.background;

import android.app.IntentService;
import android.content.Intent;

import alarmiko.geoalarm.alarm.alarmiko.alarms.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.alarms.data.AlarmCursor;
import alarmiko.geoalarm.alarm.alarmiko.alarms.data.AlarmsTableManager;
import alarmiko.geoalarm.alarm.alarmiko.alarms.misc.AlarmController;

public class OnBootUpAlarmScheduler extends IntentService {

    public OnBootUpAlarmScheduler() {
        super("OnBootUpAlarmScheduler");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            AlarmController controller = new AlarmController(this, null);
            // IntentService works in a background thread, so this won't hold us up.
            AlarmCursor cursor = new AlarmsTableManager(this).queryEnabledAlarms();
            while (cursor.moveToNext()) {
                Alarm alarm = cursor.getItem();
                if (!alarm.isEnabled()) {
                    throw new IllegalStateException(
                            "queryEnabledAlarms() returned alarm(s) that aren't enabled");
                }
                controller.scheduleAlarm(alarm, false);
            }
            cursor.close();

        }
    }

}
