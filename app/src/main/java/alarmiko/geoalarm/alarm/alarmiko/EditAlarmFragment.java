package alarmiko.geoalarm.alarm.alarmiko;

import android.content.Context;
import android.content.res.ColorStateList;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.IdRes;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;

import net.cachapa.expandablelayout.ExpandableLayout;

import alarmiko.geoalarm.alarm.alarmiko.alarms.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.alarms.misc.AlarmController;
import alarmiko.geoalarm.alarm.alarmiko.alarms.misc.DaysOfWeek;
import alarmiko.geoalarm.alarm.alarmiko.dialogs.RingtonePickerDialog;
import alarmiko.geoalarm.alarm.alarmiko.dialogs.RingtonePickerDialogController;
import alarmiko.geoalarm.alarm.alarmiko.ui.TempCheckableImageButton;
import alarmiko.geoalarm.alarm.alarmiko.utils.FragmentTagUtils;
import alarmiko.geoalarm.alarm.alarmiko.utils.Utils;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class EditAlarmFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "EditAlarmFragment";

    private GoogleMap mMap;

    private static final String ARG_SECTION_NUMBER = "alarmiko.EditAlarmFragment.ARG_SECTION_NUMBER";
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
    private RingtonePickerDialogController mRingtonePickerController;
    private AlarmController mAlarmController;

    private Alarm mAlarm;

    public EditAlarmFragment() {
    }

    public static EditAlarmFragment newInstance(Alarm alarm) {
        EditAlarmFragment fragment = new EditAlarmFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SECTION_NUMBER, alarm);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: 20.06.17 change to real id
        mAlarm = getArguments().getParcelable(ARG_SECTION_NUMBER);

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

        mRingtonePickerController = new RingtonePickerDialogController(getFragmentManager(),
                new RingtonePickerDialog.OnRingtoneSelectedListener() {
                    @Override
                    public void onRingtoneSelected(Uri ringtoneUri) {
                        Log.d(TAG, "Selected ringtone: " + ringtoneUri.toString());
                        final Alarm oldAlarm = getAlarm();
                        Alarm newAlarm = oldAlarm.toBuilder()
                                .ringtone(ringtoneUri.toString())
                                .build();
                        oldAlarm.copyMutableFieldsTo(newAlarm);
                        persistUpdatedAlarm(newAlarm, false);
                    }
                }
        );

        mAlarmController = new AlarmController(getActivity(), null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_alarm_edit, container, false);
        ButterKnife.bind(this, rootView);


        mTvRadius.setText(mAlarm.address());

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

    final void persistUpdatedAlarm(Alarm newAlarm, boolean showSnackbar) {
        mAlarmController.scheduleAlarm(newAlarm, showSnackbar);
        mAlarmController.save(newAlarm);
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

    @OnClick(R.id.tv_editor_radius)
    void onExpandLayout(TextView textView) {
        if (mExpandableLayout.isExpanded()) {
            mExpandableLayout.collapse();
        } else {
            mExpandableLayout.expand();
        }
    }

    @OnClick(R.id.ringtone)
    void showRingtonePickerDialog() {
//        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
//        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
//                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
//                // The ringtone to show as selected when the dialog is opened
//                .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, getSelectedRingtoneUri())
//                // Whether to show "Default" item in the list
//                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
//        // The ringtone that plays when default option is selected
//        //.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, DEFAULT_TONE);
//        // TODO: This is VERY BAD. Use a Controller/Presenter instead.
//        // The result will be delivered to MainActivity, and then delegated to AlarmsFragment.
//        ((Activity) getContext()).startActivityForResult(intent, AlarmsFragment.REQUEST_PICK_RINGTONE);

        mRingtonePickerController.show(getSelectedRingtoneUri(), makeTag(R.id.ringtone));
    }

    private void setVibrate(boolean vibrates) {
        Utils.setTintList(mBtnVibrate, mBtnVibrate.getDrawable(), mVibrateColors);
        mBtnVibrate.setChecked(vibrates);
    }

    private Uri getSelectedRingtoneUri() {

        String ringtone = getAlarm().ringtone();
        return ringtone.isEmpty() ?
                RingtoneManager.getActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_ALARM)
                : Uri.parse(ringtone);
    }

    private String makeTag(@IdRes int viewId) {
        return FragmentTagUtils.makeTag(EditAlarmFragment.class, viewId, mAlarm.getId());
    }

    public Alarm getAlarm() {

        return mAlarm;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(mAlarm.coordinates(), 17f));
        mMap.addCircle(new CircleOptions().center(mAlarm.coordinates()).radius(100));
    }
}
