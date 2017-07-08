package alarmiko.geoalarm.alarm.alarmiko;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import alarmiko.geoalarm.alarm.alarmiko.alarms.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.alarms.data.AsyncAlarmsTableUpdateHandler;
import alarmiko.geoalarm.alarm.alarmiko.alarms.list.ScrollHandler;
import alarmiko.geoalarm.alarm.alarmiko.alarms.misc.AlarmController;
import alarmiko.geoalarm.alarm.alarmiko.alarms.ui.AlarmsFragment;
import alarmiko.geoalarm.alarm.alarmiko.dummy.DummyContent;
import alarmiko.geoalarm.alarm.alarmiko.ui.AlarmEditInterface;

public class AlarmEditActivity extends AppCompatActivity implements
        AlarmListFragment.OnListFragmentInteractionListener,
        AlarmEditInterface, ScrollHandler {

    private static final String TAG = "AlarmEditActivity";

    public static final String ACTION_EDIT_ALARM = "alarm.alarmiko.AlarmEditActivity.ACTION_EDIT_ALARM";
    public static final String ACTION_ALARM_LIST = "alarm.alarmiko.AlarmEditActivity.ACTION_ALARM_LIST";

    public static final String EXTRA_PICKED_ADDRESS = "alarm.alarmiko.AlarmEditActivity.EXTRA_PICKED_ADDRESS";

    private AsyncAlarmsTableUpdateHandler mAsyncUpdateHandler;
    private AlarmController mAlarmController;
    private View mSnackbarAnchor;

    private AlarmsFragment mAlarmsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        String action = getIntent().getAction();

        mAlarmsFragment = new AlarmsFragment();

        mSnackbarAnchor = findViewById(R.id.main_content);
        mAlarmController = new AlarmController(this, mSnackbarAnchor);
        mAsyncUpdateHandler = new AsyncAlarmsTableUpdateHandler(this,
                mSnackbarAnchor, this, mAlarmController);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (action == null || ACTION_ALARM_LIST.equals(action)) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });

            transaction.add(R.id.fragment_container, mAlarmsFragment);
        } else {
            fab.setVisibility(View.GONE);
            Alarm alarm = getIntent().getParcelableExtra(EXTRA_PICKED_ADDRESS);
            alarm.setEnabled(true);
            mAsyncUpdateHandler.asyncInsert(alarm);

            Log.d("TAG", "onCreate: alarm = " + alarm);

            transaction.add(R.id.fragment_container, EditAlarmFragment.newInstance(alarm));
        }

        transaction.commit();
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {
        showAlarmEditFragment(null);
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
    public void onListItemClick(Alarm item, int position) {
        Log.d(TAG, "onListItemClick() called with: item = [" + item + "], position = [" + position + "]");

        showAlarmEditFragment(item);
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
//        transaction.addToBackStack(null);
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
}
