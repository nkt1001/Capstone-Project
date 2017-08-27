package alarmiko.geoalarm.alarm.alarmiko.alarms.background;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import alarmiko.geoalarm.alarm.alarmiko.alarms.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.alarms.misc.AlarmController;
import alarmiko.geoalarm.alarm.alarmiko.alarms.misc.DaysOfWeek;
import alarmiko.geoalarm.alarm.alarmiko.alarms.ringtone.AlarmActivity;
import alarmiko.geoalarm.alarm.alarmiko.utils.ErrorUtils;

public class GeofenceTransitionsIntentService extends IntentService {
    private static final String TAG = "GeofenceTransitionsInte";

    private static final String ACTION_SCHEDULE_ALARM = "alarmiko.geoalarm.alarm.alarmiko.alarms.background.action.SCHEDULE_ALARM";
    private static final String ACTION_CANCEL_ALARM = "alarmiko.geoalarm.alarm.alarmiko.alarms.background.action.CANCEL_ALARM";

    private static final String EXTRA_ALARMS = "alarmiko.geoalarm.alarm.alarmiko.alarms.background.extra.ALARMS";
    private static final int CONNECT_ATTEMPTS = 3;
    private static final long TWENTY_FOUR_HOURS = 24L * 60L * 60L * 1000L;
    private static final int ERROR_REGISTER_FENCE = 485;
    private static final int ERROR_UNREGISTER_FENCE = 613;
    private static final int ERROR_CONNECTING_TO_GOOGLE = 101;

    private int mErrorCounter;
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
    public void onCreate() {
        super.onCreate();
        mAlarms = new ArrayList<>();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            mAction = intent.getAction();
            mErrorCounter = 0;
            if (ACTION_SCHEDULE_ALARM.equals(mAction)) {
                handleScheduleAlarm(intent);
            } else if (ACTION_CANCEL_ALARM.equals(mAction)) {
                handleCancelAlarm(intent);
            }
        }
    }

    private void handleCancelAlarm(Intent intent) {
        mAlarms = intent.getParcelableArrayListExtra(EXTRA_ALARMS);

        if (mAlarms == null || mAlarms.size() == 0) {
            return;
        }

        connectGoogleClient();
    }

    private void handleScheduleAlarm(Intent intent) {
        mAlarms = intent.getParcelableArrayListExtra(EXTRA_ALARMS);

        if (mAlarms == null || mAlarms.size() == 0) {
            return;
        }

        connectGoogleClient();
    }

    private void connectGoogleClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Awareness.API)
                .build();
        ConnectionResult result = mGoogleApiClient.blockingConnect();
        if (result.isSuccess()) {
            onConnected();
        } else if (++mErrorCounter <= CONNECT_ATTEMPTS) {
            connectGoogleClient();
        } else {

            if (mAlarms.size() > 0) {
                AlarmController alarmController = new AlarmController(getApplicationContext(), null);
                for (Alarm alarm : mAlarms) {
                    alarm.setEnabled(false);
                    alarmController.save(alarm);
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            sendErrorBroadCast(ERROR_CONNECTING_TO_GOOGLE, result);
            disconnectGoogleClient();
        }
    }

    private void disconnectGoogleClient() {
        if (mGoogleApiClient != null && (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting())) {
            mGoogleApiClient.disconnect();
        }
    }

    @Nullable
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

        Status result = Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .addFence(String.valueOf(alarm.getId()), fence, getFencePendingIntent(alarm))
                        .build())
                .await();

        if(result.isSuccess()) {
            Log.i(TAG, "Fence was successfully registered.");
            queryFence(alarm);
        } else {
            Log.e(TAG, "Fence could not be registered: " + result);
            alarm.setEnabled(false);
            new AlarmController(getApplicationContext(), null).save(alarm);
            if (mAlarms.size() == 1) {
                sendErrorBroadCast(ERROR_REGISTER_FENCE, result);
            }
        }
    }

    private void sendErrorBroadCast(int errorCode, Status status) {
        if (ERROR_REGISTER_FENCE == errorCode) {
            if (status.hasResolution()) {
                ErrorUtils.sendBroadcastError(getApplicationContext(),
                        ErrorUtils.ErrorData.RESOLUTION_ERROR_CODE_GEOFENCE_REGISTER, status);
            } else {
                ErrorUtils.sendBroadcastError(getApplicationContext(),
                        ErrorUtils.ErrorData.CRITICAL_ERROR_CODE_GEOFENCE_REGISTER, status);
            }
        } else if (ERROR_UNREGISTER_FENCE == errorCode) {
            if (status.hasResolution()) {
                ErrorUtils.sendBroadcastError(getApplicationContext(),
                        ErrorUtils.ErrorData.RESOLUTION_ERROR_CODE_GEOFENCE_UNREGISTER, status);
            } else {
                ErrorUtils.sendBroadcastError(getApplicationContext(),
                        ErrorUtils.ErrorData.CRITICAL_ERROR_CODE_GEOFENCE_UNREGISTER, status);
            }
        }
    }

    private void sendErrorBroadCast(int errorCode, ConnectionResult status) {
        if (ERROR_CONNECTING_TO_GOOGLE == errorCode) {
            if (status.hasResolution()) {
                ErrorUtils.sendBroadcastError(getApplicationContext(),
                        ErrorUtils.ErrorData.RESOLUTION_ERROR_CODE_GEOFENCE_REGISTER, status);
            } else {
                ErrorUtils.sendBroadcastError(getApplicationContext(),
                        ErrorUtils.ErrorData.CRITICAL_ERROR_CODE_GEOFENCE_REGISTER, status);
            }
        }
    }

    protected void unregisterFence(final Alarm alarm) {
        Log.d(TAG, "unregisterFence() called with: fenceKey = [" + alarm.getId() + "]");
        Status result = Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .removeFence(String.valueOf(alarm.getId()))
                        .build()).await();

        if (!result.isSuccess()) {
            Log.e(TAG, "unregisterFence: error");
            alarm.setEnabled(false);
            new AlarmController(getApplicationContext(), null).save(alarm);
            sendErrorBroadCast(ERROR_UNREGISTER_FENCE, result);
        }
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

    private PendingIntent getFencePendingIntent(Alarm alarm) {
        Intent intent = new Intent(FenceReceiver.FENCE_ACTION);
        intent.putExtra(AlarmActivity.EXTRA_RINGING_OBJECT, alarm);
        return PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
    }

    public void onConnected() {
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
            for (Alarm alarm : mAlarms) {
                unregisterFence(alarm);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectGoogleClient();
    }
}
