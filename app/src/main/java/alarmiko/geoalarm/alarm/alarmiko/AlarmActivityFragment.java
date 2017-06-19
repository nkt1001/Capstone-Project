package alarmiko.geoalarm.alarm.alarmiko;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import net.cachapa.expandablelayout.ExpandableLayout;

import alarmiko.geoalarm.alarm.alarmiko.ui.TempCheckableImageButton;
import alarmiko.geoalarm.alarm.alarmiko.utils.Utils;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class AlarmActivityFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_SECTION_NUMBER = "alarmiko.AlarmActivityFragment.ARG_SECTION_NUMBER";
    @BindView(R.id.editor_alias)
    EditText mEditTextAlias;
    @BindView(R.id.editor_switch)
    SwitchCompat mSwitchOnOff;
    @BindView(R.id.tv_editor_radius)
    TextView mTvRadius;
    @BindView(R.id.expandable_layout)
    ExpandableLayout mExpandableLayout;
    @BindView(R.id.ok)
    Button mOk;
    @BindView(R.id.dismiss) Button mDismissButton;
    @BindView(R.id.delete) Button mBtnDelete;
    @BindView(R.id.ringtone) Button mBtnRingtone;
    @BindView(R.id.vibrate)
    TempCheckableImageButton mBtnVibrate;
    @BindViews({R.id.day0, R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6})
    ToggleButton[] mBtnDays;

    private ColorStateList mDayToggleColors;
    private ColorStateList mVibrateColors;

    public AlarmActivityFragment() {
    }

    public static AlarmActivityFragment newInstance(int alarmId) {
        AlarmActivityFragment fragment = new AlarmActivityFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, alarmId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int[][] states = {
                /*item 1*/{/*states*/android.R.attr.state_checked},
                /*item 2*/{/*states*/}
        };
        // TODO: Phase out Utils.getColorFromThemeAttr because it doesn't work for text colors.
        // WHereas getTextColorFromThemeAttr works for both regular colors and text colors.
        int[] dayToggleColors = {
                /*item 1*/Utils.getTextColorFromThemeAttr(getContext(), R.attr.colorAccent),
                /*item 2*/Utils.getTextColorFromThemeAttr(getContext(), android.R.attr.textColorHint)
        };
        int[] vibrateColors = {
                /*item 1*/Utils.getTextColorFromThemeAttr(getContext(), R.attr.colorAccent),
                /*item 2*/Utils.getTextColorFromThemeAttr(getContext(), R.attr.themedIconTint)
        };

        mDayToggleColors = new ColorStateList(states, dayToggleColors);
        mVibrateColors = new ColorStateList(states, vibrateColors);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_alarm, container, false);
        ButterKnife.bind(this, rootView);

        mTvRadius.setOnClickListener(this);

        return rootView;
    }

    @OnClick(R.id.vibrate)
    void onVibrateToggled() {
        final boolean checked = mBtnVibrate.isChecked();
        if (checked) {
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(300);
        }
        final Alarm oldAlarm = getAlarm();
        Alarm newAlarm = oldAlarm.toBuilder()
                .vibrates(checked)
                .build();
        oldAlarm.copyMutableFieldsTo(newAlarm);
        persistUpdatedAlarm(newAlarm, false);
    }

    @OnClick({ R.id.day0, R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6 })
    void onDayToggled(ToggleButton view) {
        final Alarm oldAlarm = getAlarm();
        Alarm newAlarm = oldAlarm.toBuilder().build();
        oldAlarm.copyMutableFieldsTo(newAlarm);
        // ---------------------------------------------------------------------------------
        // TOneverDO: precede copyMutableFieldsTo()
        int position = ((ViewGroup) view.getParent()).indexOfChild(view);
        int weekDayAtPosition = DaysOfWeek.getInstance(getContext()).weekDayAt(position);
        Log.d(TAG, "Day toggle #" + position + " checked changed. This is weekday #"
                + weekDayAtPosition + " relative to a week starting on Sunday");
        newAlarm.setRecurring(weekDayAtPosition, view.isChecked());
        // ---------------------------------------------------------------------------------
        persistUpdatedAlarm(newAlarm, true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_editor_radius:
                if (mExpandableLayout.isExpanded()) {
                    mExpandableLayout.collapse();
                } else {
                    mExpandableLayout.expand();
                }
                break;
        }
    }
}
