
package alarmiko.geoalarm.alarm.alarmiko.dialogs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;

import com.philliphsu.bottomsheetpickers.time.BottomSheetTimePickerDialog;
import com.philliphsu.bottomsheetpickers.time.grid.GridTimePickerDialog;
import com.philliphsu.bottomsheetpickers.time.numberpad.NumberPadTimePickerDialog;

import alarmiko.geoalarm.alarm.alarmiko.R;

public final class TimePickerDialogController extends DialogFragmentController<BottomSheetTimePickerDialog> {
    private static final String TAG = "TimePickerController";

    private final BottomSheetTimePickerDialog.OnTimeSetListener mListener;
    private final Context mContext;
    private final FragmentManager mFragmentManager;

    /**
     * @param context Used to read the user's preference for the style of the time picker dialog to show.
     */
    public TimePickerDialogController(FragmentManager fragmentManager, Context context,
                                      BottomSheetTimePickerDialog.OnTimeSetListener listener) {
        super(fragmentManager);
        mFragmentManager = fragmentManager;
        mContext = context;
        mListener = listener;
    }

    public void show(int initialHourOfDay, int initialMinute, String tag) {
        BottomSheetTimePickerDialog dialog = null;
        final String numpadStyle = mContext.getString(R.string.number_pad);
        final String gridStyle = mContext.getString(R.string.grid_selector);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String prefTimePickerStyle = prefs.getString(mContext.getString(R.string.key_time_picker_style), numpadStyle);
        boolean isNumpadStyle = prefTimePickerStyle.equals(numpadStyle);
        boolean isGridStyle = prefTimePickerStyle.equals(gridStyle);
        if (isNumpadStyle || isGridStyle) {
            final String themeLight = mContext.getString(R.string.theme_light);
            final String themeDark = mContext.getString(R.string.theme_dark);
            final String themeBlack = mContext.getString(R.string.theme_black);
            String prefTheme = prefs.getString(mContext.getString(R.string.key_theme), themeLight);
            
            final int dialogColorRes;
            if (prefTheme.equals(themeLight)) {
                dialogColorRes = android.R.color.white;
            } else if (prefTheme.equals(themeDark)) {
                dialogColorRes = android.R.color.white;
            } else if (prefTheme.equals(themeBlack)) {
                dialogColorRes = android.R.color.white;
            } else {
                dialogColorRes = 0;
            }
            final @ColorInt int dialogColor = ContextCompat.getColor(mContext, dialogColorRes);
            if (isNumpadStyle) {
                dialog = new NumberPadTimePickerDialog.Builder(mListener)
                        .setHeaderColor(dialogColor)
                        .setBackgroundColor(dialogColor)
                        .setThemeDark(true)
                        .build();
            } else {
                final int selectedColor = ContextCompat.getColor(mContext, R.color.colorAccent);
                final int unselectedColor = ContextCompat.getColor(mContext, android.R.color.white);
                dialog = new GridTimePickerDialog.Builder(
                        mListener,
                        initialHourOfDay,
                        initialMinute,
                        DateFormat.is24HourFormat(mContext))
                        .setHeaderColor(dialogColor)
                        .setBackgroundColor(dialogColor)
                        .setHeaderTextColorSelected(selectedColor)
                        .setHeaderTextColorUnselected(unselectedColor)
                        .setTimeSeparatorColor(unselectedColor)
                        .setHalfDayButtonColorSelected(selectedColor)
                        .setHalfDayButtonColorUnselected(unselectedColor)
                        .setThemeDark(true)
                        .build();
            }
        } else {
            SystemTimePickerDialog timepicker = SystemTimePickerDialog.newInstance(
                    mListener, initialHourOfDay, initialMinute, DateFormat.is24HourFormat(mContext));
            timepicker.show(mFragmentManager, tag);
            return;
        }
        show(dialog, tag);
    }

    @Override
    public void tryRestoreCallback(String tag) {
        // Can't use #findDialog()!
        DialogFragment picker = (DialogFragment) mFragmentManager.findFragmentByTag(tag);
        if (picker instanceof BottomSheetTimePickerDialog) {
            ((BottomSheetTimePickerDialog) picker).setOnTimeSetListener(mListener);
        } else if (picker instanceof SystemTimePickerDialog) {
            ((SystemTimePickerDialog) picker).setOnTimeSetListener(mListener);
        }
    }
}
