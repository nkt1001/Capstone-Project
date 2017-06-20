
package alarmiko.geoalarm.alarm.alarmiko.utils;

import android.support.design.widget.Snackbar;
import android.view.View;

public final class DelayedSnackbarHandler {
    private static Snackbar snackbar;
    private static String message;

    private DelayedSnackbarHandler() {}

    /**
     * Saves a reference to the given Snackbar, so that you can
     * call {@link #show()} at a later time.
     */
    public static void prepareSnackbar(Snackbar sb) {
        snackbar = sb;
    }

    /**
     * Shows the Snackbar previously prepared with
     * {@link #prepareSnackbar(Snackbar)}
     */
    public static void show() {
        if (snackbar != null) {
            snackbar.show();
            snackbar = null;
        }
    }

    /**
     * Saves a static reference to the message, so that you can
     * call {@link #makeAndShow(View)} at a later time.
     */
    public static void prepareMessage(String msg) {
        message = msg;
    }

    /**
     * Makes a Snackbar with the message previously prepared with
     * {@link #prepareMessage(String)} and shows it.
     */
    public static void makeAndShow(View snackbarAnchor) {
        if (snackbarAnchor != null && message != null) {
            Snackbar.make(snackbarAnchor, message, Snackbar.LENGTH_LONG).show();
            message = null;
        }
    }
}
