package alarmiko.geoalarm.alarm.alarmiko.utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class MapUtils {
    private static final double RADIUS_OF_EARTH_METERS = 6371009;

    public static LatLng toRadiusLatLng(LatLng center, double radiusMeters) {
        double radiusAngle = Math.toDegrees(radiusMeters / RADIUS_OF_EARTH_METERS) /
                Math.cos(Math.toRadians(center.latitude));
        return new LatLng(center.latitude, center.longitude + radiusAngle);
    }

    public static double toRadiusMeters(LatLng center, LatLng radius) {
        float[] result = new float[1];
        Location.distanceBetween(center.latitude, center.longitude,
                radius.latitude, radius.longitude, result);
        return result[0];
    }
}
