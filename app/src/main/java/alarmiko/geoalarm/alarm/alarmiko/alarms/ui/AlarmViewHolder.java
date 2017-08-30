
package alarmiko.geoalarm.alarm.alarmiko.alarms.ui;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import alarmiko.geoalarm.alarm.alarmiko.R;
import alarmiko.geoalarm.alarm.alarmiko.alarms.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.alarms.list.BaseViewHolder;
import alarmiko.geoalarm.alarm.alarmiko.alarms.list.OnListItemInteractionListener;
import alarmiko.geoalarm.alarm.alarmiko.alarms.misc.AlarmController;
import alarmiko.geoalarm.alarm.alarmiko.alarms.misc.DaysOfWeek;
import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTouch;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class AlarmViewHolder extends BaseViewHolder<Alarm> {
    private static final String TAG = "AlarmViewHolder";
    private static final String COMMA_SEPARATOR = ", ";

    private final AlarmController mAlarmController;

//    private final Drawable mDismissNowDrawable;
    private final Drawable mCancelSnoozeDrawable;

    final FragmentManager mFragmentManager;

    @BindView(R.id.tv_alarm_item_label)
    TextView mLabel;
    @BindView(R.id.alarm_switch)
    SwitchCompat mSwitch;
    @BindView(R.id.tv_alarm_item_street)
    TextView mStreet;
    @BindView(R.id.dismiss)
    Button mDismissButton;
    @BindView(R.id.tv_alarm_item_info)
    TextView mInfo;


    public AlarmViewHolder(ViewGroup parent,
                           OnListItemInteractionListener<Alarm> listener,
                           AlarmController controller) {
        super(parent, R.layout.fragment_alarmitem, listener);
        mAlarmController = controller;
//        mDismissNowDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_dismiss_alarm_24dp);
        mCancelSnoozeDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_cancel_snooze);

        AppCompatActivity act = (AppCompatActivity) getContext();
        mFragmentManager = act.getSupportFragmentManager();
    }

    @Override
    public void onBind(Alarm alarm) {
        super.onBind(alarm);

        bindSwitch(alarm.isEnabled());
        bindDismissButton(alarm);
        bindLabel(alarm.label());
        bindStreet(alarm.address());
        bindInfo(alarm);
    }

    protected void bindInfo(Alarm alarm) {

        List<String> days = new ArrayList<>();

        for (int i = 0; i < DaysOfWeek.NUM_DAYS; i++) {
            int weekDay = DaysOfWeek.getInstance(getContext()).weekDayAt(i);
            if (alarm.isRecurring(weekDay)) {
                days.add(DaysOfWeek.getLabel(weekDay));
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (days.size() == DaysOfWeek.NUM_DAYS) {
            stringBuilder.append(getContext().getString(R.string.every_day));
        } else if (days.size() == 0) {
            stringBuilder.append(getContext().getString(R.string.once));
        } else {
            for (int i = 0; i < days.size(); i++) {
                String s = days.get(i);
                stringBuilder.append(s);
                if (i+1 < days.size()) {
                    stringBuilder.append(COMMA_SEPARATOR);
                }
            }
        }

        mInfo.setText(stringBuilder.toString());
    }

    protected void bindStreet(String street) {
        boolean visibility = street.length() > 0;
        setVisibility(mStreet, visibility);
        mStreet.setText(street);
    }

    protected void bindLabel(String label) {
        boolean visible = label.length() > 0;
        setVisibility(mLabel, visible);
        mLabel.setText(label);
    }

    /**
     * Exposed to subclasses if they have visibility logic for their views.
     */
    protected final void setVisibility(@NonNull View view, boolean visible) {
        view.setVisibility(visible ? VISIBLE : GONE);
    }

    protected final Alarm getAlarm() {
        return getItem();
    }

    @OnClick(R.id.dismiss)
    void dismiss() {
        Alarm alarm = getAlarm();
        if (alarm.isSnoozed()) {
            alarm.stopSnoozing();
            persistUpdatedAlarm(alarm, false);
            setVisibility(mDismissButton, false);
        }
    }

    @OnTouch(R.id.alarm_switch)
    boolean slide(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            mSwitch.setPressed(true);
        }
        return false;
    }

    @OnCheckedChanged(R.id.alarm_switch)
    void toggle(boolean checked) {

        if (mSwitch.isPressed()) {
            Alarm alarm = getAlarm();
            alarm.setEnabled(checked);
            if (alarm.isEnabled()) {
                persistUpdatedAlarm(alarm, true);
            } else {
                if (alarm.isGeo()) {
                    mAlarmController.cancelGeo(alarm, true, true);
                } else {
                    mAlarmController.cancelAlarm(alarm, true, false);
                }
            }
            mSwitch.setPressed(false);
        }
    }

    final void persistUpdatedAlarm(Alarm newAlarm, boolean showSnackbar) {
        if (newAlarm.isGeo()) {
            mAlarmController.scheduleGeo(newAlarm, showSnackbar);
        } else {
            mAlarmController.scheduleAlarm(newAlarm, showSnackbar);
        }
        mAlarmController.save(newAlarm);
    }

    private void bindSwitch(boolean enabled) {
        mSwitch.setChecked(enabled);
    }

    private void bindDismissButton(Alarm alarm) {
        boolean snoozed = alarm.isSnoozed();
        boolean visible = alarm.isEnabled() && snoozed;
        String buttonText = getContext().getString(R.string.cancel_snoozing);
        setVisibility(mDismissButton, visible);
        mDismissButton.setText(buttonText);
        mDismissButton.setCompoundDrawablesRelativeWithIntrinsicBounds(mCancelSnoozeDrawable, null, null, null);
    }
}
