
package alarmiko.geoalarm.alarm.alarmiko.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import com.philliphsu.bottomsheetpickers.time.BottomSheetTimePickerDialog;

public class SystemTimePickerDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private BottomSheetTimePickerDialog.OnTimeSetListener mListener;
    private int mInitialHourOfDay;
    private int mInitialMinute;
    private boolean mIs24HourMode;

    public static SystemTimePickerDialog newInstance(BottomSheetTimePickerDialog.OnTimeSetListener l,
                                                     int hourOfDay, int minute, boolean is24HourMode) {
        SystemTimePickerDialog dialog = new SystemTimePickerDialog();
        dialog.mListener = l;
        dialog.mInitialHourOfDay = hourOfDay;
        dialog.mInitialMinute = minute;
        dialog.mIs24HourMode = is24HourMode;
        return dialog;
    }

    public void setOnTimeSetListener(BottomSheetTimePickerDialog.OnTimeSetListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new TimePickerDialog(getActivity(), this, mInitialHourOfDay, mInitialMinute, mIs24HourMode);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // http://stackoverflow.com/q/19452993/5055032
        // BUG PRE-LOLLIPOP! This is also called when the dialog is dismissed, so clicking
        // the 'Done' button will end up calling this twice!
        if (view.isShown() && mListener != null) {
            mListener.onTimeSet(view, hourOfDay, minute);
        }
    }
}
