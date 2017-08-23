package alarmiko.geoalarm.alarm.alarmiko.alarms.background;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceQueryRequest;
import com.google.android.gms.awareness.fence.FenceQueryResult;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceStateMap;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.awareness.fence.TimeFence;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import alarmiko.geoalarm.alarm.alarmiko.alarms.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.alarms.misc.DaysOfWeek;

public class GeofenceTransitionsIntentService extends IntentService implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = "GeofenceTransitionsInte";

    private static final String ACTION_SCHEDULE_ALARM = "alarmiko.geoalarm.alarm.alarmiko.alarms.background.action.SCHEDULE_ALARM";
    private static final String ACTION_CANCEL_ALARM = "alarmiko.geoalarm.alarm.alarmiko.alarms.background.action.CANCEL_ALARM";

    private static final String EXTRA_ALARMS = "alarmiko.geoalarm.alarm.alarmiko.alarms.background.extra.ALARMS";
    private static final int CONNECT_ATTEMPTS = 3;
    private static final long TWENTY_FOUR_HOURS = 24L * 60L * 60L * 1000L;

    private int mErrorConter;
    private String mAction;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<Alarm> mAlarms;

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    public static void scheduleGeoAlarm(Context context, ArrayList<Alarm> alarms) {
        Intent intent = new Intent(context.getApplicationContext(), GeofenceTransitionsIntentService.class);
        intent.setAction(ACTION_SCHEDULE_ALARM);
        intent.putParcelableArrayListExtra(EXTRA_ALARMS, alarms);
        context.startService(intent);
    }

    public static void cancelGeoAlarm(Context context, ArrayList<Alarm> alarms) {
        Intent intent = new Intent(context.getApplicationContext(), GeofenceTransitionsIntentService.class);
        intent.setAction(ACTION_CANCEL_ALARM);
        intent.putParcelableArrayListExtra(EXTRA_ALARMS, alarms);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            mAction = intent.getAction();
            mErrorConter = 0;
            if (ACTION_SCHEDULE_ALARM.equals(mAction)) {
                handleScheduleAlarm(intent);
            } else if (ACTION_CANCEL_ALARM.equals(mAction)) {
                handleCancelAlarm(intent);
            }
        }
    }

    private void handleCancelAlarm(Intent intent) {
        mAlarms = intent.getParcelableArrayListExtra(EXTRA_ALARMS);

        if (mAlarms.size() == 0) {
            return;
        }

        connectGoogleClient();
    }

    private void handleScheduleAlarm(Intent intent) {
        mAlarms = intent.getParcelableArrayListExtra(EXTRA_ALARMS);

        if (mAlarms.size() == 0) {
            return;
        }

        connectGoogleClient();
    }

    private void connectGoogleClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext(), this, this)
                .addApi(Awareness.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void disconnectGoogleClient() {
        if (mGoogleApiClient != null && (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting())) {
            mGoogleApiClient.disconnect();
        }
    }

    private AwarenessFence getAwarenessFence(Alarm alarm) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        AwarenessFence basicFence = LocationFence.in(alarm.coordinates().latitude, alarm.coordinates().longitude, alarm.radius(), 0L);

        if (!alarm.hasRecurrence()) {
            return basicFence;
        } else {
            List<AwarenessFence> fences = new ArrayList<>();
            fences.add(basicFence);

            for (int i = 0; i < DaysOfWeek.NUM_DAYS; i++) {
                if (alarm.isRecurring(i)) {
                    AwarenessFence fence = null;
                    switch (i) {
                        case DaysOfWeek.MONDAY:
                            fence = TimeFence.inMondayInterval(TimeZone.getDefault(), 0L, TWENTY_FOUR_HOURS);
                            break;
                        case DaysOfWeek.TUESDAY:
                            fence = TimeFence.inTuesdayInterval(TimeZone.getDefault(), 0L, TWENTY_FOUR_HOURS);
                            break;
                        case DaysOfWeek.WEDNESDAY:
                            fence = TimeFence.inWednesdayInterval(TimeZone.getDefault(), 0L, TWENTY_FOUR_HOURS);
                            break;
                        case DaysOfWeek.THURSDAY:
                            fence = TimeFence.inThursdayInterval(TimeZone.getDefault(), 0L, TWENTY_FOUR_HOURS);
                            break;
                        case DaysOfWeek.FRIDAY:
                            fence = TimeFence.inFridayInterval(TimeZone.getDefault(), 0L, TWENTY_FOUR_HOURS);
                            break;
                        case DaysOfWeek.SATURDAY:
                            fence = TimeFence.inSaturdayInterval(TimeZone.getDefault(), 0L, TWENTY_FOUR_HOURS);
                            break;
                        case DaysOfWeek.SUNDAY:
                            fence = TimeFence.inSundayInterval(TimeZone.getDefault(), 0L, TWENTY_FOUR_HOURS);
                            break;
                    }

                    if (fence != null) {
                        fences.add(fence);
                    }
                }
            }

            return AwarenessFence.and(fences);
        }
    }

    protected void registerFence(final Alarm alarm, final AwarenessFence fence) {

        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .addFence(String.valueOf(alarm.getId()), fence, getFencePendingIntent())
                        .build())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if(status.isSuccess()) {
                            Log.i(TAG, "Fence was successfully registered.");
                            queryFence(alarm);
                        } else {
                            Log.e(TAG, "Fence could not be registered: " + status);
                        }
                    }
                });
    }

    protected void unregisterFence(final String fenceKey) {
        Log.d(TAG, "unregisterFence() called with: fenceKey = [" + fenceKey + "]");
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .removeFence(fenceKey)
                        .build()).setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {
                Log.i(TAG, "Fence " + fenceKey + " successfully removed.");
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Log.i(TAG, "Fence " + fenceKey + " could NOT be removed.");
            }
        });
    }

    private void queryFence(final Alarm alarm) {
        Log.d(TAG, "queryFence: " + alarm);

        Awareness.FenceApi.queryFences(mGoogleApiClient,
                FenceQueryRequest.forFences(String.valueOf(alarm.getId())))
                .setResultCallback(new ResultCallback<FenceQueryResult>() {
                    @Override
                    public void onResult(@NonNull FenceQueryResult fenceQueryResult) {
                        if (!fenceQueryResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Could not query fence: " + alarm);
                            return;
                        }
                        FenceStateMap map = fenceQueryResult.getFenceStateMap();
                        for (String fenceKey : map.getFenceKeys()) {
                            FenceState fenceState = map.getFenceState(fenceKey);
                            Log.i(TAG, "Fence " + fenceKey + ": "
                                    + fenceState.getCurrentState()
                                    + ", was="
                                    + fenceState.getPreviousState()
                                    + ", lastUpdateTime="
                                    +fenceState.getLastFenceUpdateTimeMillis());
                        }
                    }
                });
    }

    private PendingIntent getFencePendingIntent() {
        Intent intent = new Intent(FenceReceiver.FENCE_ACTION);
        return PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            disconnectGoogleClient();
            return;
        }

        if (ACTION_SCHEDULE_ALARM.equals(mAction)) {
            for (Alarm alarm : mAlarms) {
                AwarenessFence fence = getAwarenessFence(alarm);
                if (fence == null) {
                    break;
                }
                registerFence(alarm, fence);
            }
        } else if (ACTION_CANCEL_ALARM.equals(mAction)) {
            Log.d(TAG, "onConnected: cancel");
            for (Alarm alarm : mAlarms) {
                unregisterFence(String.valueOf(alarm.getId()));
            }
        }

//        disconnectGoogleClient();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onConnectionSuspended(int i) {}
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (++mErrorConter <= CONNECT_ATTEMPTS) {
            connectGoogleClient();
        } else {
            disconnectGoogleClient();
        }
    }
}
