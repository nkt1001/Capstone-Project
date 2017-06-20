package alarmiko.geoalarm.alarm.alarmiko.dialogs;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.philliphsu.bottomsheetpickers.BottomSheetPickerDialog;

public abstract class BottomSheetTimePickerDialog extends BottomSheetPickerDialog {
    private static final String TAG = "BottomSheetTimePickerDialog";

    private OnTimeSetListener mCallback;

    /**
     * The callback interface used to indicate the user is done filling in
     * the time (they clicked on the 'Set' button).
     */
    public interface OnTimeSetListener {
        /**
         * @param viewGroup The view associated with this listener.
         * @param hourOfDay The hour that was set.
         * @param minute The minute that was set.
         */
        void onTimeSet(ViewGroup viewGroup, int hourOfDay, int minute);
    }

    public final void setOnTimeSetListener(OnTimeSetListener callback) {
        mCallback = callback;
    }

    protected final void onTimeSet(ViewGroup vg, int hourOfDay, int minute) {
        if (mCallback != null) {
            mCallback.onTimeSet(vg, hourOfDay, minute);
        }
        dismiss();
    }

    protected static abstract class Builder extends BottomSheetPickerDialog.Builder {
        protected final OnTimeSetListener mListener;
        protected final boolean mIs24HourMode;

        protected Builder(OnTimeSetListener listener) {
            this(listener, false);
        }

        protected Builder(OnTimeSetListener listener, boolean is24HourMode) {
            mListener = listener;
            mIs24HourMode = is24HourMode;
        }

        @Override
        protected final void super_build(@NonNull BottomSheetPickerDialog dialog) {
            super.super_build(dialog);
        }
    }
}
