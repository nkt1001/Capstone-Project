
package alarmiko.geoalarm.alarm.alarmiko.misc;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;

import alarmiko.geoalarm.alarm.alarmiko.R;

public final class AlarmPreferences {
    private static final String TAG = "AlarmPreferences";

    private AlarmPreferences() {}

    public static int snoozeDuration(Context c) {
        return readPreference(c, R.string.key_snooze_duration, 10);
    }

    // TODO: Consider renaming to hoursToNotifyInAdvance()
    public static int hoursBeforeUpcoming(Context c) {
        return readPreference(c, R.string.key_notify_me_of_upcoming_alarms, 2);
    }

    public static int minutesToSilenceAfter(Context c) {
        return readPreference(c, R.string.key_silence_after, 15);
    }

    public static int firstDayOfWeek(Context c) {
        return readPreference(c, R.string.key_first_day_of_week, 0 /* Sunday */);
    }

    public static int dismissNowDistance(Context c) {
        return readPreference(c, R.string.key_dismiss_now_distance, 350);
    }

    public static int readPreference(Context c, @StringRes int key, int defaultValue) {
        String value = PreferenceManager.getDefaultSharedPreferences(c).getString(c.getString(key), null);
        return null == value ? defaultValue : Integer.parseInt(value);
    }
}
