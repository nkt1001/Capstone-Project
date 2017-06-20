
package alarmiko.geoalarm.alarm.alarmiko.utils;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;

import static android.text.format.DateFormat.getTimeFormat;


public final class TimeFormatUtils {

    private TimeFormatUtils() {}

    public static String formatTime(Context context, long millis) {
        return getTimeFormat(context).format(new Date(millis));
    }

    public static String formatTime(Context context, int hourOfDay, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        return formatTime(context, cal.getTimeInMillis());
    }
}
