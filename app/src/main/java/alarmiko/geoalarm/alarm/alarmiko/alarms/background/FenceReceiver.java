package alarmiko.geoalarm.alarm.alarmiko.alarms.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.awareness.fence.FenceState;

public class FenceReceiver extends BroadcastReceiver {

    public static final String FENCE_ACTION = "alarmiko.geoalarm.alarm.alarmiko.alarms.background.action.FenceReceiver.ACTION_FENCE";

    private static final String TAG = "FenceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        FenceState fenceState = FenceState.extract(intent);

        Log.d(TAG, "onReceive: fence key " + fenceState.getFenceKey());
        switch (fenceState.getCurrentState()) {
            case FenceState.TRUE:
                Log.i(TAG, "Headphones are plugged in.");
                break;
            case FenceState.FALSE:
                Log.i(TAG, "Headphones are NOT plugged in.");
                break;
            case FenceState.UNKNOWN:
                Log.i(TAG, "The headphone fence is in an unknown state.");
                break;
        }
    }
}
