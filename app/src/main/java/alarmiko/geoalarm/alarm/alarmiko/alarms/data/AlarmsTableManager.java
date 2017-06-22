
package alarmiko.geoalarm.alarm.alarmiko.alarms.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import alarmiko.geoalarm.alarm.alarmiko.alarms.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.db.DatabaseTableManager;

import static alarmiko.geoalarm.alarm.alarmiko.alarms.misc.DaysOfWeek.FRIDAY;
import static alarmiko.geoalarm.alarm.alarmiko.alarms.misc.DaysOfWeek.MONDAY;
import static alarmiko.geoalarm.alarm.alarmiko.alarms.misc.DaysOfWeek.SATURDAY;
import static alarmiko.geoalarm.alarm.alarmiko.alarms.misc.DaysOfWeek.SUNDAY;
import static alarmiko.geoalarm.alarm.alarmiko.alarms.misc.DaysOfWeek.THURSDAY;
import static alarmiko.geoalarm.alarm.alarmiko.alarms.misc.DaysOfWeek.TUESDAY;
import static alarmiko.geoalarm.alarm.alarmiko.alarms.misc.DaysOfWeek.WEDNESDAY;

public class AlarmsTableManager extends DatabaseTableManager<Alarm> {

    public AlarmsTableManager(Context context) {
        super(context);
    }

    @Override
    protected String getQuerySortOrder() {
        return AlarmsTable.NEW_SORT_ORDER;
    }

    @Override
    public AlarmCursor queryItem(long id) {
        return wrapInAlarmCursor(super.queryItem(id));
    }

    @Override
    public AlarmCursor queryItems() {
        return wrapInAlarmCursor(super.queryItems());
    }

    public AlarmCursor queryEnabledAlarms() {
        return queryItems(AlarmsTable.COLUMN_ENABLED + " = 1", null);
    }

    @Override
    protected AlarmCursor queryItems(String where, String limit) {
        return wrapInAlarmCursor(super.queryItems(where, limit));
    }

    @Override
    protected String getTableName() {
        return AlarmsTable.TABLE_ALARMS;
    }

    @Override
    protected ContentValues toContentValues(Alarm alarm) {
        ContentValues values = new ContentValues();
        values.put(AlarmsTable.COLUMN_HOUR, alarm.hour());
        values.put(AlarmsTable.COLUMN_MINUTES, alarm.minutes());
        values.put(AlarmsTable.COLUMN_LABEL, alarm.label());
        values.put(AlarmsTable.COLUMN_RINGTONE, alarm.ringtone());
        values.put(AlarmsTable.COLUMN_VIBRATES, alarm.vibrates());
        values.put(AlarmsTable.COLUMN_ENABLED, alarm.isEnabled());
        values.put(AlarmsTable.COLUMN_RADIUS, alarm.radius());
        values.put(AlarmsTable.COLUMN_LAT, alarm.coordinates().latitude);
        values.put(AlarmsTable.COLUMN_LNG, alarm.coordinates().longitude);
        values.put(AlarmsTable.COLUMN_ADDRESS, alarm.address());
        values.put(AlarmsTable.COLUMN_SNOOZING_UNTIL_MILLIS, alarm.snoozingUntil());
        values.put(AlarmsTable.COLUMN_SUNDAY, alarm.isRecurring(SUNDAY));
        values.put(AlarmsTable.COLUMN_MONDAY, alarm.isRecurring(MONDAY));
        values.put(AlarmsTable.COLUMN_TUESDAY, alarm.isRecurring(TUESDAY));
        values.put(AlarmsTable.COLUMN_WEDNESDAY, alarm.isRecurring(WEDNESDAY));
        values.put(AlarmsTable.COLUMN_THURSDAY, alarm.isRecurring(THURSDAY));
        values.put(AlarmsTable.COLUMN_FRIDAY, alarm.isRecurring(FRIDAY));
        values.put(AlarmsTable.COLUMN_SATURDAY, alarm.isRecurring(SATURDAY));
        values.put(AlarmsTable.COLUMN_IGNORE_UPCOMING_RING_TIME, alarm.isIgnoringUpcomingRingTime());
        return values;
    }

    @Override
    protected String getOnContentChangeAction() {
        return AlarmsListCursorLoader.ACTION_CHANGE_CONTENT;
    }

    private AlarmCursor wrapInAlarmCursor(Cursor c) {
        return new AlarmCursor(c);
    }
}
