package alarmiko.geoalarm.alarm.alarmiko.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.List;

import alarmiko.geoalarm.alarm.alarmiko.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.R;
import alarmiko.geoalarm.alarm.alarmiko.db.AsyncAlarmsTableUpdateHandler;
import alarmiko.geoalarm.alarm.alarmiko.list.ScrollHandler;
import alarmiko.geoalarm.alarm.alarmiko.misc.AlarmController;
import alarmiko.geoalarm.alarm.alarmiko.utils.ActivityEventAdapter;
import butterknife.BindView;

public class AlarmEditActivity extends BaseActivity implements
        AlarmEditInterface, ScrollHandler {

    private static final String TAG = "AlarmEditActivity";

    public static final String ACTION_ADD_ALARM = "alarm.alarmiko.AlarmEditActivity.ACTION_ADD_ALARM";
    public static final String ACTION_EDIT_ALARM = "alarm.alarmiko.AlarmEditActivity.ACTION_EDIT_ALARM";
    public static final String ACTION_ALARM_LIST = "alarm.alarmiko.AlarmEditActivity.ACTION_ALARM_LIST";

    public static final String EXTRA_PICKED_ADDRESS = "alarm.alarmiko.AlarmEditActivity.EXTRA_PICKED_ADDRESS";

    private static final int EVENT_ERROR_CONNECTION = 428;
    private static final int EVENT_CRITICAL_ERROR = 239;
    private static final int EVENT_HANDLABLE_ERROR = 180;
    private static final int EVENT_SCROLL_TO_POSITION = 830;
    private static final int EVENT_SCROLL_TO_STABLE_ID = 980;

    private final List<ActivityEventAdapter> mListeners = new ArrayList<>();

    private AsyncAlarmsTableUpdateHandler mAsyncUpdateHandler;
    private AlarmController mAlarmController;

    @BindView(R.id.main_content) View mSnackbarAnchor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mToolbar != null) {
            mToolbar.setTitle(R.string.title_activity_alarm);
        }
        String action = getIntent().getAction();

        mSnackbarAnchor = findViewById(R.id.main_content);
        mAlarmController = new AlarmController(this, mSnackbarAnchor);
        mAsyncUpdateHandler = new AsyncAlarmsTableUpdateHandler(this,
                mSnackbarAnchor, this, mAlarmController);


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (action == null || ACTION_ALARM_LIST.equals(action)) {
            transaction.replace(R.id.fragment_container, new AlarmsFragment());
        } else if (ACTION_ADD_ALARM.equals(action)) {
            Alarm alarm = getIntent().getParcelableExtra(EXTRA_PICKED_ADDRESS);
//            mAsyncUpdateHandler.asyncInsert(alarm);
            transaction.replace(R.id.fragment_container, EditAlarmFragment.newInstance(alarm, false));
        } else if (ACTION_EDIT_ALARM.equals(action)) {
            Alarm alarm = getIntent().getParcelableExtra(EXTRA_PICKED_ADDRESS);
            transaction.replace(R.id.fragment_container, EditAlarmFragment.newInstance(alarm, true));
        }

        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected int layoutResId() {
        return R.layout.activity_alarm_edit;
    }

    @Override
    protected int menuResId() {
        return 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onListItemClick(Alarm item, int position) {
        showAlarmEditFragment(item);
    }

    public void addActivityEventListener(@NonNull ActivityEventAdapter listener) {
        mListeners.add(listener);
    }

    public void removeActivityEventListener(ActivityEventAdapter callback) {
        mListeners.remove(callback);
    }

    private void showAlarmEditFragment(Alarm item) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, EditAlarmFragment.newInstance(item, true));
        transaction.addToBackStack(null);
        transaction.commit();
        showInterstitial();
    }

    private void showAlarmListFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new AlarmsFragment());
            transaction.commit();
        }
        showInterstitial();
    }

    @Override
    public void onListItemDeleted(Alarm item) {
        mAsyncUpdateHandler.asyncDelete(item);
    }

    @Override
    public void onListItemUpdate(Alarm item, int position) {
        mAsyncUpdateHandler.asyncUpdate(item.getId(), item);
    }

    @Override
    public View getSnackbarAnchor() {
        return mSnackbarAnchor;
    }

    @Override
    public AlarmController getAlarmController() {
        return mAlarmController;
    }

    @Override
    public AsyncAlarmsTableUpdateHandler getTableUpdater() {
        return mAsyncUpdateHandler;
    }

    @Override
    public void editFinished() {
        showAlarmListFragment();
    }

    @Override
    public void onAddNewAlarmCLicked() {
        finish();
    }

    @Override
    public void setScrollToStableId(long id) {
        fireEvent(EVENT_SCROLL_TO_STABLE_ID, 0, null, null, 0, id);
    }

    @Override
    public void scrollToPosition(int position) {
        fireEvent(EVENT_SCROLL_TO_STABLE_ID, 0, null, null, position, 0);
    }

    private void fireEvent(int type, int errorCode, ConnectionResult result, Status status, int position, long stableId) {
        for (ActivityEventAdapter listener : mListeners) {
            switch (type) {
                case EVENT_ERROR_CONNECTION:
                    listener.connectionError(errorCode, result);
                    break;
                case EVENT_HANDLABLE_ERROR:
                    listener.handleError(errorCode, status);
                    break;
                case EVENT_CRITICAL_ERROR:
                    listener.criticalError(errorCode, status);
                    break;
                case EVENT_SCROLL_TO_POSITION:
                    listener.scrollToPosition(position);
                    break;
                case EVENT_SCROLL_TO_STABLE_ID:
                    listener.setScrollToStableId(stableId);
                    break;
            }
        }
    }

    @Override
    public void connectionError(int errorCode, ConnectionResult status) {
        super.connectionError(errorCode, status);

        fireEvent(EVENT_ERROR_CONNECTION, errorCode, status, null, 0, 0);
    }

    @Override
    public void criticalError(int errorCode, Status status) {
        super.criticalError(errorCode, status);

        fireEvent(EVENT_CRITICAL_ERROR, errorCode, null, status, 0, 0);
    }

    @Override
    public void handleError(int errorCode, @Nullable Status status) {
        super.handleError(errorCode, status);

        fireEvent(EVENT_HANDLABLE_ERROR, errorCode, null, status, 0, 0);
    }
}
