package alarmiko.geoalarm.alarm.alarmiko.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;

import alarmiko.geoalarm.alarm.alarmiko.Alarm;
import alarmiko.geoalarm.alarm.alarmiko.R;
import alarmiko.geoalarm.alarm.alarmiko.background.GeofenceTransitionsIntentService;
import alarmiko.geoalarm.alarm.alarmiko.db.AlarmCursor;
import alarmiko.geoalarm.alarm.alarmiko.db.AlarmsTableManager;
import alarmiko.geoalarm.alarm.alarmiko.ui.Alarmiko;
import alarmiko.geoalarm.alarm.alarmiko.ui.MapsActivity;
import alarmiko.geoalarm.alarm.alarmiko.utils.CurrentLocationService;
import alarmiko.geoalarm.alarm.alarmiko.utils.MapUtils;

public class AlarmikoWidget extends AppWidgetProvider {

    private static final String TAG = "AlarmikoWidget";

    public static final String WIDGET_REFRESH_ACTION = "android.appwidget.action.APPWIDGET_UPDATE";

    static CurrentLocationService currentLocationService;

    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager) {

        final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        new Thread(new Runnable() {
            @Override
            public void run() {

                currentLocationService = new CurrentLocationService(context, new CurrentLocationService.CurrentLocationServiceCallback() {
                    @Override
                    public void currentLocation(@Nullable LatLng location, boolean isConnected) {
                        if (currentLocationService != null) {
                            currentLocationService.stopGettingLocation();
                        }
                    }
                });
                currentLocationService.startGettingLocation();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.alarmiko_widget);

                views.setViewVisibility(R.id.tv_alarm_switcher, View.VISIBLE);
                views.setViewVisibility(R.id.appwidget_divider_left, View.VISIBLE);
                views.setViewVisibility(R.id.appwidget_divider_right, View.VISIBLE);
                views.setViewVisibility(R.id.appwidget_tv_layout, View.VISIBLE);
                views.setViewVisibility(R.id.appwidget_text_distance, View.VISIBLE);
                views.setViewVisibility(R.id.appwidget_text_label, View.VISIBLE);
                views.setViewVisibility(R.id.tv_add_alarm, View.VISIBLE);
                views.setViewVisibility(R.id.appwidget_progressbar, View.GONE);
                views.setViewVisibility(R.id.tv_add_alarm_expanded, View.GONE);

                Intent addAlarmIntent = new Intent(context, MapsActivity.class);
                PendingIntent addAlarmPendingIntent = PendingIntent.getActivity(context, 0, addAlarmIntent, 0);
                views.setOnClickPendingIntent(R.id.tv_add_alarm, addAlarmPendingIntent);
                views.setOnClickPendingIntent(R.id.tv_add_alarm_expanded, addAlarmPendingIntent);

                AlarmsTableManager alarmsTableManager = new AlarmsTableManager(context);
                AlarmCursor cursor = alarmsTableManager.queryEnabledAlarms();

                int count = cursor == null ? 0 : cursor.getCount();

                if (count == 0) {
                    cursor = alarmsTableManager.queryItems();
                    count = cursor == null ? 0 : cursor.getCount();
                }

                if (count > 0) {

                    LatLng currentLocation = Alarmiko.getCurrentLocation() != null
                            ? Alarmiko.getCurrentLocation() : MapUtils.getLastKnownLocation(context);

                    Alarm closestAlarm = null;

                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        Alarm alarm = cursor.getItem();
                        if (currentLocation == null) {
                            closestAlarm = alarm;
                            break;
                        }

                        if (closestAlarm == null) {
                            closestAlarm = alarm;
                        }

                        double distanceNext = MapUtils.toRadiusMeters(currentLocation, alarm.coordinates());
                        double distanceCurrent = MapUtils.toRadiusMeters(currentLocation, closestAlarm.coordinates());

                        if (distanceCurrent > distanceNext) {
                            closestAlarm = alarm;
                        }

                        cursor.moveToNext();
                    }

                    if (closestAlarm != null) {
                        String label = !closestAlarm.label().isEmpty() ? closestAlarm.label() : closestAlarm.address();
                        views.setTextViewText(R.id.appwidget_text_label, label);
                        String distanceText = context.getString(R.string.distance_template,
                                (int) MapUtils.toRadiusMeters(currentLocation, closestAlarm.coordinates()));
                        views.setTextViewText(R.id.appwidget_text_distance, distanceText);

                        String enableString;
                        int drawableRes;
                        String action;
                        if (closestAlarm.isEnabled()) {
                            enableString = context.getString(R.string.dismiss_now);
                            drawableRes = R.drawable.ic_alarm_off_white_24dp;
                            action = GeofenceTransitionsIntentService.ACTION_CANCEL_ALARM_FROM_WIDGET;
                        } else {
                            enableString = context.getString(R.string.schedule_now);
                            drawableRes = R.drawable.ic_alarm_on_white_24dp;
                            action = GeofenceTransitionsIntentService.ACTION_SCHEDULE_ALARM_FROM_WIDGET;
                        }

                        views.setTextViewText(R.id.tv_alarm_switcher, enableString);
                        views.setTextViewCompoundDrawables(R.id.tv_alarm_switcher, 0, drawableRes, 0, 0);

                        Intent resetAlarmIntent = new Intent(context, GeofenceTransitionsIntentService.class);
                        resetAlarmIntent.setAction(action);

                        ArrayList<Alarm> alarms = new ArrayList<>();
                        alarms.add(closestAlarm);
                        Log.d(TAG, "run: " + alarms);
                        resetAlarmIntent.putExtra(GeofenceTransitionsIntentService.EXTRA_ALARMS, alarms);
                        PendingIntent resetAlarmPendingIntent = PendingIntent.getService(context, 0, resetAlarmIntent, 0);
                        views.setOnClickPendingIntent(R.id.tv_alarm_switcher, resetAlarmPendingIntent);
                    } else {
                        noAlarms(views, context);
                    }
                } else {
                    noAlarms(views, context);
                }

                if (cursor != null) {
                    cursor.close();
                }

                // Instruct the widget manager to update the widget
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ComponentName watchWidget = new ComponentName(context, AlarmikoWidget.class);
                        appWidgetManager.updateAppWidget(watchWidget, views);
                    }
                });
            }
        }).start();

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.alarmiko_widget);
        showLoading(views);
        ComponentName watchWidget = new ComponentName(context, AlarmikoWidget.class);
        appWidgetManager.updateAppWidget(watchWidget, views);
    }

    private static void showLoading(RemoteViews views) {
        views.setViewVisibility(R.id.tv_alarm_switcher, View.GONE);
        views.setViewVisibility(R.id.appwidget_divider_left, View.GONE);
        views.setViewVisibility(R.id.appwidget_divider_right, View.GONE);
        views.setViewVisibility(R.id.appwidget_tv_layout, View.GONE);
        views.setViewVisibility(R.id.tv_add_alarm, View.GONE);
        views.setViewVisibility(R.id.tv_add_alarm_expanded, View.GONE);
        views.setViewVisibility(R.id.appwidget_progressbar, View.VISIBLE);
    }

    private static void noAlarms(RemoteViews views, Context context) {
        views.setViewVisibility(R.id.tv_alarm_switcher, View.GONE);
        views.setViewVisibility(R.id.appwidget_text_distance, View.GONE);
        views.setViewVisibility(R.id.appwidget_divider_left, View.GONE);
        views.setViewVisibility(R.id.tv_add_alarm, View.GONE);
        views.setViewVisibility(R.id.tv_add_alarm_expanded, View.VISIBLE);
        views.setTextViewText(R.id.appwidget_text_label, context.getText(R.string.no_alarms));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Log.d(TAG, "onUpdate: " + Arrays.toString(appWidgetIds));
        updateAppWidget(context, appWidgetManager);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals(WIDGET_REFRESH_ACTION)) {
            Log.d(TAG, "onReceive: updating");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            updateAppWidget(context, appWidgetManager);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }
}

