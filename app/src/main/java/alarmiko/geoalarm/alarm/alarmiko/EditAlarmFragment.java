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
import android.support.v4.app.Fragment;
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
import alarmiko.geoalarm.alarm.alarmiko.alarms.misc.AlarmPreferences;
import alarmiko.geoalarm.alarm.alarmiko.alarms.misc.DaysOfWeek;
import alarmiko.geoalarm.alarm.alarmiko.dialogs.AddLabelDialog;
import alarmiko.geoalarm.alarm.alarmiko.dialogs.AddLabelDialogController;
import alarmiko.geoalarm.alarm.alarmiko.dialogs.RingtonePickerDialog;
import alarmiko.geoalarm.alarm.alarmiko.dialogs.RingtonePickerDialogController;
import alarmiko.geoalarm.alarm.alarmiko.ui.AlarmEditInterface;
import alarmiko.geoalarm.alarm.alarmiko.ui.TempCheckableImageButton;
import alarmiko.geoalarm.alarm.alarmiko.utils.CurrentLocationService;
import alarmiko.geoalarm.alarm.alarmiko.utils.FragmentTagUtils;
import alarmiko.geoalarm.alarm.alarmiko.utils.MapUtils;
import alarmiko.geoalarm.alarm.alarmiko.utils.Utils;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import static alarmiko.geoalarm.alarm.alarmiko.utils.TimeFormatUtils.formatTime;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * A placeholder fragment containing a simple view.
 */
public class EditAlarmFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener,
        GoogleMap.OnCameraMoveStartedListener, CurrentLocationService.CurrentLocationServiceCallback {

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
//    private AlarmController mAlarmController;

    private Alarm mAlarm;
    private AlarmEditInterface mListener;
    private AddLabelDialogController mAddLabelDialogController;

//    private AsyncAlarmsTableUpdateHandler mAsyncUpdateHandler;

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
            ((AlarmEditActivity)getActivity()).addOnLocationChangedListener(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() instanceof AlarmEditActivity) {
            ((AlarmEditActivity)getActivity()).removeOnLocationChangedListener(this);
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
                        persistUpdatedAlarm(newAlarm, false);
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
                        persistUpdatedAlarm(newAlarm, false);
                        bindRingtone();
                    }
                }
        );

//        mAlarmController = new AlarmController(getActivity(), null);
    }

    private void bindLabel() {
        mTvLabel.setText(mAlarm.label());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_alarm_edit, container, false);
        ButterKnife.bind(this, rootView);

        mSwitchOnOff.setChecked(mAlarm.isEnabled());
        bindDays(mAlarm);
        bindRingtone();
        setVibrate(mAlarm.vibrates());
        bindLabel();
        mTvStreet.setText(mAlarm.address());

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
        persistUpdatedAlarm(newAlarm, false);
    }

    final void persistUpdatedAlarm(Alarm newAlarm, boolean showSnackbar) {
        if (newAlarm.isGeo()) {
            mListener.getAlarmController().scheduleGeo(newAlarm, showSnackbar);
        } else {
            mListener.getAlarmController().scheduleAlarm(newAlarm, true);
        }
//        mListener.getAlarmController().scheduleAlarm(newAlarm, showSnackbar);
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
        persistUpdatedAlarm(newAlarm, true);
    }

    @OnClick(R.id.tv_editor_radius)
    void onExpandLayout() {
        if (mExpandableLayout.isExpanded()) {
            mExpandableLayout.collapse();
        } else {
            mExpandableLayout.expand();
        }
    }

    @OnClick(R.id.tv_label)
    void onLabelClicked() {
        mAddLabelDialogController.show(mTvLabel.getText(), makeTag(R.id.tv_label));
    }

    @OnClick(R.id.ok)
    void onBtnOkClicked() {
        mListener.editFinished();
    }

    @OnClick(R.id.delete)
    void onBtnDeleteClicked() {
        mListener.onListItemDeleted(mAlarm);
        mListener.editFinished();
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

    @OnCheckedChanged(R.id.editor_switch)
    void toggle(boolean checked) {

        if (mSwitchOnOff.isPressed()) {
            Alarm alarm = getAlarm();
            alarm.setEnabled(checked);
            if (alarm.isEnabled()) {
                persistUpdatedAlarm(alarm, true);
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
        persistUpdatedAlarm(newAlarm, false);

        Log.d(TAG, "drawMapCircle: " + radiusInMeters);

        mMap.addCircle(new CircleOptions().center(mAlarm.coordinates()).radius(radiusInMeters));
        mMap.setOnCameraMoveStartedListener(EditAlarmFragment.this);

        MarkerOptions markerOptions = new MarkerOptions().position(MapUtils.toRadiusLatLng(mAlarm.coordinates(), mAlarm.radius()));
        mMap.addMarker(markerOptions);
    }

    @Override
    public void onCameraIdle() {
        Log.d(TAG, "onCameraIdle: ");
        drawMapCircle();
    }

    @Override
    public void onCameraMoveStarted(int i) {
        Log.d(TAG, "onCameraMoveStarted: ");
        mMap.setOnCameraIdleListener(EditAlarmFragment.this);
    }

    @Override
    public void currentLocation(@Nullable LatLng location, boolean isConnected) {

        if (!isConnected || location == null) {
            return;
        }

        final int upcomingDistance = AlarmPreferences.dismissNowDistance(getContext());
        boolean upcoming = Utils.isUpcoming(Alarmiko.getCurrentLocation(), mAlarm.coordinates(), mAlarm.radius(), upcomingDistance);
        boolean snoozed = mAlarm.isSnoozed();
        boolean visible = mAlarm.isEnabled() && (upcoming || snoozed);
        String buttonText = snoozed
                ? getContext().getString(R.string.title_snoozing_until, formatTime(getContext(), mAlarm.snoozingUntil()))
                : getContext().getString(R.string.dismiss_now);
        setVisibility(mDismissButton, visible);
        mDismissButton.setText(buttonText);
        Drawable icon = upcoming ? ContextCompat.getDrawable(getContext(), R.drawable.ic_dismiss_alarm_24dp)
                : ContextCompat.getDrawable(getContext(), R.drawable.ic_cancel_snooze);
        mDismissButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
    }
}
