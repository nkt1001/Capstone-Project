package alarmiko.geoalarm.alarm.alarmiko;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import net.cachapa.expandablelayout.ExpandableLayout;

import alarmiko.geoalarm.alarm.alarmiko.alarms.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.alarms.misc.DaysOfWeek;
import alarmiko.geoalarm.alarm.alarmiko.dialogs.AddLabelDialog;
import alarmiko.geoalarm.alarm.alarmiko.dialogs.AddLabelDialogController;
import alarmiko.geoalarm.alarm.alarmiko.dialogs.RingtonePickerDialog;
import alarmiko.geoalarm.alarm.alarmiko.dialogs.RingtonePickerDialogController;
import alarmiko.geoalarm.alarm.alarmiko.ui.AlarmEditInterface;
import alarmiko.geoalarm.alarm.alarmiko.ui.TempCheckableImageButton;
import alarmiko.geoalarm.alarm.alarmiko.utils.ErrorReceiver;
import alarmiko.geoalarm.alarm.alarmiko.utils.FragmentTagUtils;
import alarmiko.geoalarm.alarm.alarmiko.utils.MapUtils;
import alarmiko.geoalarm.alarm.alarmiko.utils.Utils;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class EditAlarmFragment extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener,
        GoogleMap.OnCameraMoveStartedListener, ErrorReceiver.ErrorHandler {

    private static final String TAG = "EditAlarmFragment";

    private GoogleMap mMap;

    private static final String ARG_SECTION_NUMBER = "alarmiko.EditAlarmFragment.ARG_SECTION_NUMBER";
    @BindView(R.id.tv_street)
    TextView mTvStreet;
    @BindView(R.id.tv_label)
    TextView mTvLabel;
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

    private Alarm mAlarm;
    private AlarmEditInterface mListener;
    private AddLabelDialogController mAddLabelDialogController;
    private Drawable mCancelSnoozeDrawable;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AlarmEditInterface) {
            mListener = (AlarmEditInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() instanceof AlarmEditActivity) {
            ((AlarmEditActivity)getActivity()).addErrorListener(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() instanceof AlarmEditActivity) {
            ((AlarmEditActivity)getActivity()).removeErrorListener(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

        mCancelSnoozeDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_cancel_snooze);

        mDayToggleColors = new ColorStateList(states, dayToggleColors);
        mVibrateColors = new ColorStateList(states, vibrateColors);

        mAddLabelDialogController = new AddLabelDialogController(getFragmentManager(),
                new AddLabelDialog.OnLabelSetListener() {
                    @Override
                    public void onLabelSet(String label) {
                        final Alarm oldAlarm = getAlarm();
                        Alarm newAlarm = oldAlarm.toBuilder()
                                .label(label)
                                .build();
                        oldAlarm.copyMutableFieldsTo(newAlarm);
                        persistUpdatedAlarm(newAlarm, false, false);
                        bindLabel();
                    }
                }
        );

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
                        persistUpdatedAlarm(newAlarm, false, false);
                        bindRingtone();
                    }
                }
        );
    }

    private void bindLabel() {
        mTvLabel.setText(mAlarm.label());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        bindSwitcher();
        bindDays(mAlarm);
        bindRingtone();
        bindDismiss();
        setVibrate(mAlarm.vibrates());
        bindLabel();
        mTvStreet.setText(mAlarm.address());

        return rootView;
    }

    private void bindSwitcher() {
        mSwitchOnOff.setChecked(mAlarm.isEnabled());
    }

    @Override
    protected int contentLayout() {
        return R.layout.fragment_alarm_edit;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @OnClick(R.id.vibrate)
    void onVibrateToggled() {
        Log.d(TAG, "onVibrateToggled: ");
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
        persistUpdatedAlarm(newAlarm, false, false);
    }

    final void persistUpdatedAlarm(Alarm newAlarm, boolean showSnackbar, boolean schedule) {
        if (schedule) {
            if (newAlarm.isGeo()) {
                mListener.getAlarmController().scheduleGeo(newAlarm, showSnackbar);
            } else {
                mListener.getAlarmController().scheduleAlarm(newAlarm, true);
            }
        }
        mListener.getAlarmController().save(newAlarm);
        mAlarm = newAlarm;
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
        newAlarm.setRecurring(weekDayAtPosition, view.isChecked());
        // ---------------------------------------------------------------------------------
        persistUpdatedAlarm(newAlarm, true, false);
    }

    @OnClick(R.id.tv_editor_radius)
    void onExpandLayout() {
        if (mExpandableLayout.isExpanded()) {
            mExpandableLayout.collapse();
        } else {
            mExpandableLayout.expand();
        }
    }

    @OnClick(R.id.dismiss)
    void onDismissClicked() {
        Alarm alarm = getAlarm();
        if (alarm.isSnoozed()) {
            alarm.stopSnoozing();
            persistUpdatedAlarm(alarm, false, false);
            setVisibility(mDismissButton, false);
        }
    }

    @OnClick(R.id.tv_label)
    void onLabelClicked() {
        mAddLabelDialogController.show(mTvLabel.getText(), makeTag(R.id.tv_label));
    }

    @OnClick(R.id.ok)
    void onBtnOkClicked() {
        persistUpdatedAlarm(mAlarm, false, true);
        mListener.editFinished();
    }

    @OnClick(R.id.delete)
    void onBtnDeleteClicked() {
        mListener.onListItemDeleted(mAlarm);
        mListener.editFinished();
    }

    @OnClick(R.id.ringtone)
    void showRingtonePickerDialog() {

        mRingtonePickerController.show(getSelectedRingtoneUri(), makeTag(R.id.ringtone));
    }

    @OnCheckedChanged(R.id.editor_switch)
    void toggle(boolean checked) {

        if (mSwitchOnOff.isPressed()) {
            Alarm alarm = getAlarm();
            alarm.setEnabled(checked);
            if (alarm.isEnabled()) {
                persistUpdatedAlarm(alarm, true, true);
            } else {
                if (alarm.isGeo()) {
                    mListener.getAlarmController().cancelGeo(alarm, true, true);
                } else {
                    mListener.getAlarmController().cancelAlarm(alarm, true, false);
                }
            }
            mSwitchOnOff.setPressed(false);
        }
    }

    private void bindDays(Alarm alarm) {
        for (int i = 0; i < mBtnDays.length; i++) {
            mBtnDays[i].setTextColor(mDayToggleColors);
            int weekDay = DaysOfWeek.getInstance(getContext()).weekDayAt(i);
            String label = DaysOfWeek.getLabel(weekDay);
            mBtnDays[i].setTextOn(label);
            mBtnDays[i].setTextOff(label);
            mBtnDays[i].setChecked(alarm.isRecurring(weekDay));
        }
    }

    private void bindRingtone() {
        int iconTint = Utils.getTextColorFromThemeAttr(getContext(), R.attr.themedIconTint);

        Drawable ringtoneIcon = mBtnRingtone.getCompoundDrawablesRelative()[0/*start*/];
        ringtoneIcon = DrawableCompat.wrap(ringtoneIcon.mutate());
        DrawableCompat.setTint(ringtoneIcon, iconTint);
        mBtnRingtone.setCompoundDrawablesRelativeWithIntrinsicBounds(ringtoneIcon, null, null, null);

        String title = RingtoneManager.getRingtone(getContext(),
                getSelectedRingtoneUri()).getTitle(getContext());
        mBtnRingtone.setText(title);
    }

    private void bindDismiss() {
        boolean snoozed = mAlarm.isSnoozed();
        boolean visible = mAlarm.isEnabled() && snoozed;
        String buttonText = getContext().getString(R.string.cancel_snoozing);
        setVisibility(mDismissButton, visible);
        mDismissButton.setText(buttonText);
        mDismissButton.setCompoundDrawablesRelativeWithIntrinsicBounds(mCancelSnoozeDrawable, null, null, null);
    }

    private void setVibrate(boolean vibrates) {
        Utils.setTintList(mBtnVibrate, mBtnVibrate.getDrawable(), mVibrateColors);
        mBtnVibrate.setChecked(vibrates);
    }

    protected final void setVisibility(@NonNull View view, boolean visible) {
        view.setVisibility(visible ? VISIBLE : GONE);
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

        double zoom = mAlarm.zoom();

        if (zoom != 0) {

            mMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(mAlarm.coordinates(), (float) zoom));
        } else {
            mMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(mAlarm.coordinates(), 17f));
        }
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraIdleListener(this);
    }

    private void drawMapCircle() {

        Log.d(TAG, "drawMapCircle: ");
        mMap.setOnCameraIdleListener(null);
        mMap.setOnCameraMoveStartedListener(null);

        mMap.moveCamera(CameraUpdateFactory
                .newLatLng(mAlarm.coordinates()));

        mMap.clear();

        mMap.addMarker(new MarkerOptions().position(mAlarm.coordinates()));

        VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
        Point x = mMap.getProjection().toScreenLocation(visibleRegion.farRight);
        Point y = mMap.getProjection().toScreenLocation(visibleRegion.nearLeft);

//        MapUtils.
        Point radiusPoint = x.x < y.y ? y : x;

        LatLng radius = mMap.getProjection().fromScreenLocation(new Point((radiusPoint.x / 2), (radiusPoint.y / 2)));

        double radiusInMeters = MapUtils.toRadiusMeters(mAlarm.coordinates(), radius);
        double zoom = mMap.getCameraPosition().zoom;

        final Alarm oldAlarm = getAlarm();
        Alarm newAlarm = oldAlarm.toBuilder()
                .radius(radiusInMeters)
                .zoom(zoom)
                .build();
        oldAlarm.copyMutableFieldsTo(newAlarm);
        persistUpdatedAlarm(newAlarm, false, false);

        Log.d(TAG, "drawMapCircle: " + radiusInMeters);

        mMap.addCircle(new CircleOptions().center(mAlarm.coordinates()).radius(radiusInMeters));
        mMap.setOnCameraMoveStartedListener(EditAlarmFragment.this);

        MarkerOptions markerOptions = new MarkerOptions().position(MapUtils.toRadiusLatLng(mAlarm.coordinates(), mAlarm.radius()));
        mMap.addMarker(markerOptions);
    }

    @Override
    public void onCameraIdle() {
        drawMapCircle();
    }

    @Override
    public void onCameraMoveStarted(int i) {
        mMap.setOnCameraIdleListener(EditAlarmFragment.this);
    }

    @Override
    public void handleError(int errorCode, @Nullable Status status) {
        shutDownAlarm();
    }

    @Override
    public void criticalError(int errorCode, @Nullable Status status) {
        shutDownAlarm();
    }

    @Override
    public void connectionError(int errorCode, @Nullable ConnectionResult status) {
        shutDownAlarm();
    }

    private void shutDownAlarm() {
        mAlarm.setEnabled(false);
        bindSwitcher();
    }
}
