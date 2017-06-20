package alarmiko.geoalarm.alarm.alarmiko.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.widget.ImageView;

public class Utils {
    public static int getTextColorFromThemeAttr(Context context, int resid) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[] {resid});
        final int color = a.getColor(0/*index*/, 0/*defValue*/);
        a.recycle();
        return color;
    }
    public static void setTintList(ImageView target, Drawable drawable, ColorStateList tintList) {
        drawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTintList(drawable, tintList);
        target.setImageDrawable(drawable);
    }
}
