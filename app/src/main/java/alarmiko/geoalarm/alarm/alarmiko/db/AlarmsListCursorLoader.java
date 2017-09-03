
package alarmiko.geoalarm.alarm.alarmiko.db;

import android.content.Context;

import alarmiko.geoalarm.alarm.alarmiko.Alarm;

public class AlarmsListCursorLoader extends SQLiteCursorLoader<Alarm, AlarmCursor> {
    public static final String ACTION_CHANGE_CONTENT
            = "alarms.data.action.CHANGE_CONTENT";

    public AlarmsListCursorLoader(Context context) {
        super(context);
    }

    @Override
    protected AlarmCursor loadCursor() {
        return new AlarmsTableManager(getContext()).queryItems();
    }

    @Override
    protected String getOnContentChangeAction() {
        return ACTION_CHANGE_CONTENT;
    }
}
