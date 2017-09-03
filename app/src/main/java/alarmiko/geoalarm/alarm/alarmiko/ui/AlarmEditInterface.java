package alarmiko.geoalarm.alarm.alarmiko.ui;

import android.view.View;

import alarmiko.geoalarm.alarm.alarmiko.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.db.AsyncAlarmsTableUpdateHandler;
import alarmiko.geoalarm.alarm.alarmiko.misc.AlarmController;

public interface AlarmEditInterface {
    void onListItemClick(Alarm item, int position);
    void onListItemDeleted(Alarm item);
    void onListItemUpdate(Alarm item, int position);
    View getSnackbarAnchor();
    AlarmController getAlarmController();
    AsyncAlarmsTableUpdateHandler getTableUpdater();
    void editFinished();
    void onAddNewAlarmCLicked();
}
