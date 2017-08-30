package alarmiko.geoalarm.alarm.alarmiko.ui;

import android.view.View;

import alarmiko.geoalarm.alarm.alarmiko.alarms.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.alarms.misc.AlarmController;

public interface AlarmEditInterface {
    void onListItemClick(Alarm item, int position);
    void onListItemDeleted(Alarm item);
    void onListItemUpdate(Alarm item, int position);
    View getSnackbarAnchor();
    AlarmController getAlarmController();
    void editFinished();
    void onAddNewAlarmCLicked();
}
