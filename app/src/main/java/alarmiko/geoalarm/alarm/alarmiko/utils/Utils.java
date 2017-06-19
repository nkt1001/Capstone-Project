package alarmiko.geoalarm.alarm.alarmiko.utils;

import android.content.Context;
import android.content.res.TypedArray;

/**
 * Created by nikitaemelyanov on 19.06.17.
 */

public class Utils {
    public static int getTextColorFromThemeAttr(Context context, int resid) {
        // http://stackoverflow.com/a/33839580/5055032
//        final TypedValue value = new TypedValue();
//        context.getTheme().resolveAttribute(resid, value, true);
//        TypedArray a = context.obtainStyledAttributes(value.data,
//                new int[] {resid});
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[] {resid});
        final int color = a.getColor(0/*index*/, 0/*defValue*/);
        a.recycle();
        return color;
        // Didn't work! Gave me white!
//        return getColorFromThemeAttr(context, android.R.attr.textColorPrimary);
    }
}
