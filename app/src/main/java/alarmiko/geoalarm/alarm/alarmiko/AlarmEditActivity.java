package alarmiko.geoalarm.alarm.alarmiko;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import alarmiko.geoalarm.alarm.alarmiko.alarms.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.alarms.ui.AlarmsFragment;
import alarmiko.geoalarm.alarm.alarmiko.dummy.DummyContent;

public class AlarmEditActivity extends AppCompatActivity implements
        AlarmListFragment.OnListFragmentInteractionListener,
        AlarmsFragment.Callback {

    public static final String ACTION_EDIT_ALARM = "alarm.alarmiko.AlarmEditActivity.ACTION_EDIT_ALARM";
    public static final String ACTION_ALARM_LIST = "alarm.alarmiko.AlarmEditActivity.ACTION_ALARM_LIST";

    public static final String EXTRA_PICKED_ADDRESS = "alarm.alarmiko.AlarmEditActivity.EXTRA_PICKED_ADDRESS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        String action = getIntent().getAction();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (action == null || ACTION_ALARM_LIST.equals(action)) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });

            transaction.add(R.id.fragment_container, new AlarmsFragment());
        } else {
            fab.setVisibility(View.GONE);
            String address = getIntent().getStringExtra(EXTRA_PICKED_ADDRESS);

            transaction.add(R.id.fragment_container, EditAlarmFragment.newInstance(Alarm.builder()
                    .address(address != null ? address : "")
                    .build()));
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
        showAlarmEditFragment(item);
    }

    private void showAlarmEditFragment(Alarm item) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, EditAlarmFragment.newInstance(item));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onListItemDeleted(Alarm item) {
    }

    @Override
    public void onListItemUpdate(Alarm item, int position) {
    }
}
