
package alarmiko.geoalarm.alarm.alarmiko.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import alarmiko.geoalarm.alarm.alarmiko.list.RecyclerViewFragment;
import alarmiko.geoalarm.alarm.alarmiko.ui.AlarmEditActivity;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;

public final class ContentIntentUtils {

    public static PendingIntent create(@NonNull Context context, long stableId) {
        // TODO: 20.06.17 Send to Edit Alarm Activity
        Intent intent = new Intent(context, AlarmEditActivity.class)
                .setAction(RecyclerViewFragment.ACTION_SCROLL_TO_STABLE_ID)
                .putExtra(RecyclerViewFragment.EXTRA_SCROLL_TO_STABLE_ID, stableId);
        return PendingIntent.getActivity(context, (int) stableId, intent, FLAG_CANCEL_CURRENT);
    }

    private ContentIntentUtils() {}
}
