package alarmiko.geoalarm.alarm.alarmiko.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;

import java.lang.ref.WeakReference;

public class ErrorReceiver extends BroadcastReceiver {

    private static final String TAG = "ErrorReceiver";
    public static final String ACTION_ERROR_RECEIVED =
            "alarmiko.geoalarm.alarm.alarmiko.utils.action.ErrorReceiver.ACTION_ERROR_RECEIVED";

    private final WeakReference<ErrorHandler> mHandler;

    public interface ErrorHandler {
        void handleError(int errorCode, @Nullable Status status);
        void criticalError(int errorCode, @Nullable Status status);
        void connectionError(int errorCode, @Nullable ConnectionResult status);
    }

    public ErrorReceiver(ErrorHandler handler) {
        this.mHandler = new WeakReference<>(handler);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");

        if (intent != null) {
            int code = intent.getIntExtra(ErrorUtils.ErrorData.EXTRA_ERROR_CODE, 0);
            if (code == 0) {
                throw new IllegalStateException("Error code cannot be 0");
            }

            final byte[] bytes = intent.getByteArrayExtra(ErrorUtils.ErrorData.EXTRA_ERROR_STATUS);
            Status status = null;
            if (bytes != null) {
                if ((code & ErrorUtils.ErrorData.GOOGLE_CONNECTION_ERROR) > 0) {
                    ConnectionResult result = ParcelableUtil.unmarshall(bytes, ConnectionResult.CREATOR);
                    mHandler.get().connectionError(code, result);
                } else {
                    status = ParcelableUtil.unmarshall(bytes, Status.CREATOR);
                }
            }

            if ((code & ErrorUtils.ErrorData.CRITICAL_ERROR) > 0) {
                mHandler.get().criticalError(code, status);
            } else if ((code & ErrorUtils.ErrorData.RESOLUTION_ERROR) > 0){
                mHandler.get().handleError(code, status);
            }
        }
    }


}
