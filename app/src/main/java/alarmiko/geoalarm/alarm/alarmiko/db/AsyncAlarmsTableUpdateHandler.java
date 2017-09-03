
package alarmiko.geoalarm.alarm.alarmiko.db;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;

import alarmiko.geoalarm.alarm.alarmiko.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.R;
import alarmiko.geoalarm.alarm.alarmiko.list.ScrollHandler;
import alarmiko.geoalarm.alarm.alarmiko.misc.AlarmController;

public final class AsyncAlarmsTableUpdateHandler extends AsyncDatabaseTableUpdateHandler<Alarm, AlarmsTableManager> {
    private static final String TAG = "AsyncAlarmsTableUpdateHandler";

    private final View mSnackbarAnchor;
    private final AlarmController mAlarmController;

    /**
     * @param context the Context from which we get the application context
     * @param snackbarAnchor
     */
    public AsyncAlarmsTableUpdateHandler(Context context, View snackbarAnchor,
                                         ScrollHandler scrollHandler,
                                         AlarmController alarmController) {
        super(context, scrollHandler);
        mSnackbarAnchor = snackbarAnchor;
        mAlarmController = alarmController;
    }

    @Override
    protected AlarmsTableManager onCreateTableManager(Context context) {
        return new AlarmsTableManager(context);
    }

    @Override
    protected void onPostAsyncDelete(Integer result, final Alarm alarm) {
        if (alarm.isGeo()) {
            mAlarmController.cancelGeo(alarm, false, true);
        } else {
            mAlarmController.cancelAlarm(alarm, false, false);
        }
        if (mSnackbarAnchor != null) {
            // TODO: Consider adding delay to allow the alarm item animation
            // to finish first before we show the snackbar. Inbox app does this.
            String message = getContext().getString(R.string.snackbar_item_deleted,
                    getContext().getString(R.string.alarm));
            Snackbar.make(mSnackbarAnchor, message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.snackbar_undo_item_deleted, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            asyncInsert(alarm);
                        }
                    }).show();
        }
    }

    @Override
    protected void onPostAsyncInsert(Long result, Alarm alarm) {
        if (alarm.isGeo()) {
            mAlarmController.scheduleGeo(alarm, true);
        } else {
            mAlarmController.scheduleAlarm(alarm, true);
        }
    }

    @Override
    protected void onPostAsyncUpdate(Long result, Alarm alarm) {
        if (alarm.isGeo()) {
            mAlarmController.scheduleGeo(alarm, true);
        } else {
            mAlarmController.scheduleAlarm(alarm, true);
        }
    }
}
