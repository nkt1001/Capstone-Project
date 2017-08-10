
package alarmiko.geoalarm.alarm.alarmiko.alarms.data;

import android.database.sqlite.SQLiteDatabase;

public final class AlarmsTable {

    private AlarmsTable() {}

    // TODO: Consider defining index constants for each column,
    // and then removing all cursor getColumnIndex() calls.
    public static final String TABLE_ALARMS = "alarms";

    // TODO: Consider implementing BaseColumns instead to get _id column.
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_MINUTES = "minutes";
    public static final String COLUMN_LAT = "latitude";
    public static final String COLUMN_LNG = "longitude";
    public static final String COLUMN_RADIUS = "radius";
    public static final String COLUMN_ZOOM = "zoom";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_LABEL = "label";
    public static final String COLUMN_RINGTONE = "ringtone";
    public static final String COLUMN_VIBRATES = "vibrates";
    public static final String COLUMN_ENABLED = "enabled";

    public static final String COLUMN_SNOOZING_UNTIL_MILLIS = "snoozing_until_millis";
    public static final String COLUMN_SUNDAY = "sunday";
    public static final String COLUMN_MONDAY = "monday";
    public static final String COLUMN_TUESDAY = "tuesday";
    public static final String COLUMN_WEDNESDAY = "wednesday";
    public static final String COLUMN_THURSDAY = "thursday";
    public static final String COLUMN_FRIDAY = "friday";
    public static final String COLUMN_SATURDAY = "saturday";
    public static final String COLUMN_IGNORE_UPCOMING_RING_TIME = "ignore_upcoming_ring_time";

    public static final String NEW_SORT_ORDER = COLUMN_HOUR + " ASC, "
            + COLUMN_MINUTES + " ASC, "
            // TOneverDO: Sort COLUMN_ENABLED or else alarms could be reordered
            // if you toggle them on/off, which looks confusing.
            // TODO: Figure out how to get the order to be:
            // No recurring days ->
            // Recurring earlier in user's weekday order ->
            // Recurring everyday
            // As written now, this is incorrect! For one, it assumes
            // the standard week order (starting on Sunday).
            // DESC gives us (Sunday -> Saturday -> No recurring days),
            // ASC gives us the reverse (No recurring days -> Saturday -> Sunday).
            // TODO: If assuming standard week order, try ASC for all days but
            // write COLUMN_SATURDAY first, then COLUMN_FRIDAY, ... , COLUMN_SUNDAY.
            // Check if that gives us (No recurring days -> Sunday -> Saturday).
//            + COLUMN_SUNDAY + " DESC, "
//            + COLUMN_MONDAY + " DESC, "
//            + COLUMN_TUESDAY + " DESC, "
//            + COLUMN_WEDNESDAY + " DESC, "
//            + COLUMN_THURSDAY + " DESC, "
//            + COLUMN_FRIDAY + " DESC, "
//            + COLUMN_SATURDAY + " DESC, "
            // All else equal, newer alarms first
            + COLUMN_ID + " DESC"; // TODO: If duplicate alarm times disallowed, delete this

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ALARMS + " ("
                // https://sqlite.org/autoinc.html
                // If the AUTOINCREMENT keyword appears after INTEGER PRIMARY KEY, that changes the
                // automatic ROWID assignment algorithm to prevent the reuse of ROWIDs over the
                // lifetime of the database. In other words, the purpose of AUTOINCREMENT is to
                // prevent the reuse of ROWIDs from previously deleted rows.
                + COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_HOUR + " INTEGER NOT NULL, "
                + COLUMN_MINUTES + " INTEGER NOT NULL, "
                + COLUMN_LABEL + " TEXT, "
                + COLUMN_RINGTONE + " TEXT NOT NULL, "
                + COLUMN_VIBRATES + " INTEGER NOT NULL, "
                + COLUMN_ENABLED + " INTEGER NOT NULL, "
                + COLUMN_LAT + " REAL DEFAULT 0, "
                + COLUMN_LNG + " REAL DEFAULT 0, "
                + COLUMN_RADIUS + " REAL, "
                + COLUMN_ZOOM + " REAL, "
                + COLUMN_ADDRESS + " TEXT, "
                + COLUMN_SNOOZING_UNTIL_MILLIS + " INTEGER, "
                + COLUMN_SUNDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_MONDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_TUESDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_WEDNESDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_THURSDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_FRIDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_SATURDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_IGNORE_UPCOMING_RING_TIME + " INTEGER NOT NULL);");
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARMS);
        onCreate(db);
    }
}
