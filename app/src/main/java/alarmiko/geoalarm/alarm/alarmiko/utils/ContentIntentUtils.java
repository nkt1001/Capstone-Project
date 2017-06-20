/*
 * Copyright 2017 Phillip Hsu
 *
 * This file is part of ClockPlus.
 *
 * ClockPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ClockPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClockPlus.  If not, see <http://www.gnu.org/licenses/>.
 */

package alarmiko.geoalarm.alarm.alarmiko.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import alarmiko.geoalarm.alarm.alarmiko.AlarmEditActivity;
import alarmiko.geoalarm.alarm.alarmiko.alarms.list.RecyclerViewFragment;

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
