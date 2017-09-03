
package alarmiko.geoalarm.alarm.alarmiko.db;

import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;

import alarmiko.geoalarm.alarm.alarmiko.Alarm;

import static alarmiko.geoalarm.alarm.alarmiko.misc.DaysOfWeek.FRIDAY;
import static alarmiko.geoalarm.alarm.alarmiko.misc.DaysOfWeek.MONDAY;
import static alarmiko.geoalarm.alarm.alarmiko.misc.DaysOfWeek.SATURDAY;
import static alarmiko.geoalarm.alarm.alarmiko.misc.DaysOfWeek.SUNDAY;
import static alarmiko.geoalarm.alarm.alarmiko.misc.DaysOfWeek.THURSDAY;
import static alarmiko.geoalarm.alarm.alarmiko.misc.DaysOfWeek.TUESDAY;
import static alarmiko.geoalarm.alarm.alarmiko.misc.DaysOfWeek.WEDNESDAY;

public class AlarmCursor extends BaseItemCursor<Alarm> {
    private static final String TAG = "AlarmCursor";

    public AlarmCursor(Cursor c) {
        super(c);
    }

    /**
     * @return an Alarm instance configured for the current row,
     * or null if the current row is invalid
     */
    @Override
    public Alarm getItem() {
        if (isBeforeFirst() || isAfterLast())
            return null;
        Alarm alarm = Alarm.builder()
                .hour(getInt(getColumnIndexOrThrow(AlarmsTable.COLUMN_HOUR)))
                .minutes(getInt(getColumnIndexOrThrow(AlarmsTable.COLUMN_MINUTES)))
                .vibrates(isTrue(AlarmsTable.COLUMN_VIBRATES))
                .ringtone(getString(getColumnIndexOrThrow(AlarmsTable.COLUMN_RINGTONE)))
                .label(getString(getColumnIndexOrThrow(AlarmsTable.COLUMN_LABEL)))
                .radius(getDouble(getColumnIndexOrThrow(AlarmsTable.COLUMN_RADIUS)))
                .zoom(getDouble(getColumnIndexOrThrow(AlarmsTable.COLUMN_ZOOM)))
                .address(getString(getColumnIndexOrThrow(AlarmsTable.COLUMN_ADDRESS)))
                .coordinates(new LatLng(
                        getDouble(getColumnIndexOrThrow(AlarmsTable.COLUMN_LAT)),
                        getDouble(getColumnIndexOrThrow(AlarmsTable.COLUMN_LNG))))
                .build();
        alarm.setId(getLong(getColumnIndexOrThrow(AlarmsTable.COLUMN_ID)));
        alarm.setEnabled(isTrue(AlarmsTable.COLUMN_ENABLED));
        alarm.setSnoozing(getLong(getColumnIndexOrThrow(AlarmsTable.COLUMN_SNOOZING_UNTIL_MILLIS)));
        alarm.setRecurring(SUNDAY, isTrue(AlarmsTable.COLUMN_SUNDAY));
        alarm.setRecurring(MONDAY, isTrue(AlarmsTable.COLUMN_MONDAY));
        alarm.setRecurring(TUESDAY, isTrue(AlarmsTable.COLUMN_TUESDAY));
        alarm.setRecurring(WEDNESDAY, isTrue(AlarmsTable.COLUMN_WEDNESDAY));
        alarm.setRecurring(THURSDAY, isTrue(AlarmsTable.COLUMN_THURSDAY));
        alarm.setRecurring(FRIDAY, isTrue(AlarmsTable.COLUMN_FRIDAY));
        alarm.setRecurring(SATURDAY, isTrue(AlarmsTable.COLUMN_SATURDAY));
        alarm.ignoreUpcomingRingTime(isTrue(AlarmsTable.COLUMN_IGNORE_UPCOMING_RING_TIME));
        return alarm;
    }
}
