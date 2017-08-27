package alarmiko.geoalarm.alarm.alarmiko;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import alarmiko.geoalarm.alarm.alarmiko.alarms.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.alarms.data.AsyncAlarmsTableUpdateHandler;
import alarmiko.geoalarm.alarm.alarmiko.alarms.list.ScrollHandler;
import alarmiko.geoalarm.alarm.alarmiko.alarms.misc.AlarmController;
import alarmiko.geoalarm.alarm.alarmiko.alarms.ui.AlarmsFragment;
import alarmiko.geoalarm.alarm.alarmiko.ui.AlarmEditInterface;
import alarmiko.geoalarm.alarm.alarmiko.utils.CurrentLocationService;
import alarmiko.geoalarm.alarm.alarmiko.utils.ErrorReceiver;
import butterknife.BindView;

public class AlarmEditActivity extends BaseActivity implements
        AlarmEditInterface, ScrollHandler, CurrentLocationService.CurrentLocationServiceCallback {

    private static final String TAG = "AlarmEditActivity";

    public static final String ACTION_EDIT_ALARM = "alarm.alarmiko.AlarmEditActivity.ACTION_EDIT_ALARM";
    public static final String ACTION_ALARM_LIST = "alarm.alarmiko.AlarmEditActivity.ACTION_ALARM_LIST";

    public static final String EXTRA_PICKED_ADDRESS = "alarm.alarmiko.AlarmEditActivity.EXTRA_PICKED_ADDRESS";

    private static final int ERROR_CONNECTION = 428;
    private static final int CRITICAL_ERROR = 239;
    private static final int HANDLABLE_ERROR = 180;

    private final List<ErrorReceiver.ErrorHandler> mListeners = new ArrayList<>();

    private AsyncAlarmsTableUpdateHandler mAsyncUpdateHandler;
    private AlarmController mAlarmController;

    @BindView(R.id.main_content) View mSnackbarAnchor;

    private AlarmsFragment mAlarmsFragment;

    private CurrentLocationService mLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_alarm_edit);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String action = getIntent().getAction();

        mAlarmsFragment = new AlarmsFragment();

        mSnackbarAnchor = findViewById(R.id.main_content);
        mAlarmController = new AlarmController(this, mSnackbarAnchor);
        mAsyncUpdateHandler = new AsyncAlarmsTableUpdateHandler(this,
                mSnackbarAnchor, this, mAlarmController);

        mLocationService = new CurrentLocationService(this, this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (action == null || ACTION_ALARM_LIST.equals(action)) {

            transaction.add(R.id.fragment_container, mAlarmsFragment);
        } else {
//            fab.setVisibility(View.GONE);
            Alarm alarm = getIntent().getParcelableExtra(EXTRA_PICKED_ADDRESS);
            alarm.setEnabled(false);
            mAsyncUpdateHandler.asyncInsert(alarm);

            Log.d("TAG", "onCreate: alarm = " + alarm);

            transaction.add(R.id.fragment_container, EditAlarmFragment.newInstance(alarm));
        }

        transaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mLocationService.startGettingLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mLocationService.stopGettingLocation();
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
        Log.d(TAG, "onListItemClick() called with: item = [" + item + "], position = [" + position + "]");

        showAlarmEditFragment(item);
    }

    public void addErrorListener(@NonNull ErrorReceiver.ErrorHandler listener) {
        mListeners.add(listener);
    }

    public void removeErrorListener(ErrorReceiver.ErrorHandler callback) {
        mListeners.remove(callback);
    }

    private void showAlarmEditFragment(Alarm item) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, EditAlarmFragment.newInstance(item));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showAlarmListFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mAlarmsFragment);
        transaction.commit();
    }

    @Override
    public void onListItemDeleted(Alarm item) {
        Log.d(TAG, "onListItemDeleted() called with: item = [" + item + "]");
        mAsyncUpdateHandler.asyncDelete(item);
    }

    @Override
    public void onListItemUpdate(Alarm item, int position) {
        Log.d(TAG, "onListItemUpdate() called with: item = [" + item + "], position = [" + position + "]");
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
    public void editFinished() {
        showAlarmListFragment();
    }

    @Override
    public void setScrollToStableId(long id) {
        Log.d(TAG, "setScrollToStableId() called with: id = [" + id + "]");
        if (mAlarmsFragment.isAdded()) {
            mAlarmsFragment.setScrollToStableId(id);
        }
    }

    @Override
    public void scrollToPosition(int position) {
        Log.d(TAG, "scrollToPosition() called with: position = [" + position + "]");
        if (mAlarmsFragment.isAdded()) {
            mAlarmsFragment.scrollToPosition(position);
        }
    }

    @Override
    public void currentLocation(@Nullable LatLng location, boolean isConnected) {
//        if (mListeners.size() > 0) {
//            for (CurrentLocationService.CurrentLocationServiceCallback callback : mListeners) {
//                callback.currentLocation(location, isConnected);
//            }
//        }
    }

    private void fireEvent(int type, int errorCode, ConnectionResult result, Status status) {
        for (ErrorReceiver.ErrorHandler listener : mListeners) {
            switch (type) {
                case ERROR_CONNECTION:
                    listener.connectionError(errorCode, result);
                    break;
                case HANDLABLE_ERROR:
                    listener.handleError(errorCode, status);
                    break;
                case CRITICAL_ERROR:
                    listener.criticalError(errorCode, status);
                    break;
            }
        }
    }

    @Override
    public void connectionError(int errorCode, ConnectionResult status) {
        super.connectionError(errorCode, status);

        fireEvent(ERROR_CONNECTION, errorCode, status, null);
    }

    @Override
    public void criticalError(int errorCode, Status status) {
        super.criticalError(errorCode, status);

        fireEvent(CRITICAL_ERROR, errorCode, null, status);
    }

    @Override
    public void handleError(int errorCode, @Nullable Status status) {
        super.handleError(errorCode, status);

        fireEvent(HANDLABLE_ERROR, errorCode, null, status);
    }
}
