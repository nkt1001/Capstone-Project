package alarmiko.geoalarm.alarm.alarmiko.utils;

import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;

import alarmiko.geoalarm.alarm.alarmiko.list.ScrollHandler;

public class ActivityEventAdapter implements ScrollHandler, ErrorReceiver.ErrorHandler {
    @Override
    public void handleError(int errorCode, @Nullable Status status) {}

    @Override
    public void criticalError(int errorCode, @Nullable Status status) {}

    @Override
    public void connectionError(int errorCode, @Nullable ConnectionResult status) {}

    @Override
    public void setScrollToStableId(long id) {}

    @Override
    public void scrollToPosition(int position) {}
}
