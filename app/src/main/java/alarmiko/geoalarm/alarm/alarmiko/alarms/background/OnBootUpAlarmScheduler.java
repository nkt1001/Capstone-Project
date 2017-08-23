package alarmiko.geoalarm.alarm.alarmiko.alarms.background;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

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
        Log.d("BOOT!", "onHandleIntent: ");
        if (intent != null) {
            AlarmController controller = new AlarmController(this, null);
            // IntentService works in a background thread, so this won't hold us up.
            AlarmCursor cursor = new AlarmsTableManager(this).queryEnabledAlarms();
            ArrayList<Alarm> geoAlarms = new ArrayList<>();
            while (cursor.moveToNext()) {
                Alarm alarm = cursor.getItem();
                if (!alarm.isEnabled()) {
                    throw new IllegalStateException(
                            "queryEnabledAlarms() returned alarm(s) that aren't enabled");
                }
                if (alarm.isGeo()) {
                    geoAlarms.add(alarm);
                } else {
                    controller.scheduleAlarm(alarm, false);
                }
            }
            cursor.close();

            if (geoAlarms.size() > 0) {
                controller.scheduleGeo(geoAlarms);
            }
        }
    }

}
