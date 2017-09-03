package alarmiko.geoalarm.alarm.alarmiko.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DB_NAME = "alarmiko.db";
    private static final int VERSION_1 = 1;

    private static DatabaseHelper sDatabaseHelper;

    public static DatabaseHelper getInstance(Context context) {
        if (sDatabaseHelper == null)
            sDatabaseHelper = new DatabaseHelper(context);
        return sDatabaseHelper;
    }

    private DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, VERSION_1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        AlarmsTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        AlarmsTable.onUpgrade(db, oldVersion, newVersion);
    }
}
