package alarmiko.geoalarm.alarm.alarmiko.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;

import alarmiko.geoalarm.alarm.alarmiko.R;
import alarmiko.geoalarm.alarm.alarmiko.ui.ErrorActivity;

import static alarmiko.geoalarm.alarm.alarmiko.utils.ErrorUtils.ErrorData.EXTRA_ERROR_CODE;
import static alarmiko.geoalarm.alarm.alarmiko.utils.ErrorUtils.ErrorData.EXTRA_ERROR_STATUS;

public class ErrorUtils {
    private static final String TAG = "ErrorUtils";

    public interface ErrorData {
        String EXTRA_ERROR_CODE = "alarmiko.geoalarm.alarm.alarmiko.utils.extra.ErrorData.EXTRA_ERROR_CODE";
        String EXTRA_ERROR_STATUS = "alarmiko.geoalarm.alarm.alarmiko.utils.extra.ErrorData.EXTRA_ERROR_STATUS";

        int CRITICAL_ERROR_CODE_LOCATION_UPDATE = 1;
        int CRITICAL_ERROR_CODE_GEOFENCE_REGISTER = 2;
        int CRITICAL_ERROR_CODE_GEOFENCE_UNREGISTER = 4;
        int CRITICAL_ERROR_CODE_GOOGLE_SERVICES = 8;
        int CRITICAL_ERROR_CODE_CONNECT_GOOGLE_SERVICES = 16;

        int RESOLUTION_ERROR_CODE_LOCATION_UPDATE = 32;
        int RESOLUTION_ERROR_CODE_GEOFENCE_REGISTER = 64;
        int RESOLUTION_ERROR_CODE_GEOFENCE_UNREGISTER = 128;
        int RESOLUTION_ERROR_CODE_GOOGLE_SERVICES = 256;
        int RESOLUTION_ERROR_CODE_CONNECT_GOOGLE_SERVICES = 512;

        int CRITICAL_ERROR = CRITICAL_ERROR_CODE_CONNECT_GOOGLE_SERVICES + CRITICAL_ERROR_CODE_LOCATION_UPDATE +
                CRITICAL_ERROR_CODE_GEOFENCE_REGISTER + CRITICAL_ERROR_CODE_GEOFENCE_UNREGISTER +
                CRITICAL_ERROR_CODE_GOOGLE_SERVICES;

        int RESOLUTION_ERROR = RESOLUTION_ERROR_CODE_LOCATION_UPDATE + RESOLUTION_ERROR_CODE_GEOFENCE_REGISTER +
                RESOLUTION_ERROR_CODE_GEOFENCE_UNREGISTER + RESOLUTION_ERROR_CODE_GOOGLE_SERVICES +
                RESOLUTION_ERROR_CODE_CONNECT_GOOGLE_SERVICES;

        int GOOGLE_CONNECTION_ERROR = CRITICAL_ERROR_CODE_CONNECT_GOOGLE_SERVICES + RESOLUTION_ERROR_CODE_CONNECT_GOOGLE_SERVICES;
    }

    public static void sendBroadcastError(Context context, int errorCode, @Nullable Status status) {
        Log.d(TAG, "sendBroadcastError() called with: context = [" + context + "], errorCode = [" + errorCode + "], status = [" + status + "]");

        Intent intent = new Intent(ErrorReceiver.ACTION_ERROR_RECEIVED);
        intent.putExtra(EXTRA_ERROR_CODE, errorCode);

        if (status != null) {
            intent.putExtra(EXTRA_ERROR_STATUS, status);
        }

        context.sendBroadcast(intent);
    }

    public static void sendBroadcastErrorConnetion(Context context, int errorCode, @Nullable ConnectionResult status) {
        Log.d(TAG, "sendBroadcastError() called with: context = [" + context + "], errorCode = [" + errorCode + "], status = [" + status + "]");

        Intent intent = new Intent(ErrorReceiver.ACTION_ERROR_RECEIVED);
        intent.putExtra(EXTRA_ERROR_CODE, errorCode);

        if (status != null) {
            intent.putExtra(EXTRA_ERROR_STATUS, status);
        }

        context.sendBroadcast(intent);
    }

    public static String generateErrorDescription(Context context, String action, int statusCode) {
        return context.getString(R.string.error_template, action, statusCode);
    }

    public static void navigateToErrorActivity(Context context, String description) {
        Intent intent = new Intent(context, ErrorActivity.class);
        intent.putExtra(ErrorActivity.EXTRA_DESCRIPTION, description);
        context.startActivity(intent);
    }
}
