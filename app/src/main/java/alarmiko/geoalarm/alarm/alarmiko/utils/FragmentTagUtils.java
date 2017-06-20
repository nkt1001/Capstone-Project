package alarmiko.geoalarm.alarm.alarmiko.utils;

import android.support.annotation.IdRes;

/**
 * Created by nikitaemelyanov on 20.06.17.
 */

public final class FragmentTagUtils {

    /**
     * For general use.
     */
    public static String makeTag(Class<?> cls, @IdRes int viewId) {
        return cls.getName() + ":" + viewId;
    }

    /**
     * A version suitable for our ViewHolders.
     */
    public static String makeTag(Class<?> cls, @IdRes int viewId, long itemId) {
        return makeTag(cls, viewId) + ":" + itemId;
    }

    private FragmentTagUtils() {}
}
