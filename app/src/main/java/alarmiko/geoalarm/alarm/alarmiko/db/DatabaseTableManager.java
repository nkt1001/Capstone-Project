
package alarmiko.geoalarm.alarm.alarmiko.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import alarmiko.geoalarm.alarm.alarmiko.utils.LocalBroadcastHelper;

public abstract class DatabaseTableManager<T extends ObjectWithId> {
    private static final String COLUMN_ID = "_id";

    private final SQLiteOpenHelper mDbHelper;
    private final Context mAppContext;

    public DatabaseTableManager(Context context) {
        // Internally uses the app context
        mDbHelper = DatabaseHelper.getInstance(context);
        mAppContext = context.getApplicationContext();
    }

    protected abstract String getTableName();

    protected abstract ContentValues toContentValues(T item);

    protected abstract String getOnContentChangeAction();

    protected String getQuerySortOrder() {
        return null;
    }

    public long insertItem(T item) {
        long id = mDbHelper.getWritableDatabase().insert(
                getTableName(), null, toContentValues(item));
        item.setId(id);
        notifyContentChanged();
        return id;
    }

    public int updateItem(long id, T newItem) {
        newItem.setId(id);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(getTableName(),
                toContentValues(newItem),
                COLUMN_ID + " = " + id,
                null);
//        if (rowsUpdated == 0) {
//            throw new IllegalStateException("wtf?");
//        }
        notifyContentChanged();
        return rowsUpdated;
    }

    public int deleteItem(T item) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(getTableName(),
                COLUMN_ID + " = " + item.getId(),
                null);
        notifyContentChanged();
        return rowsDeleted;
    }

    public Cursor queryItem(long id) {
        Cursor c = queryItems(COLUMN_ID + " = " + id, "1");
        c.moveToFirst();
        return c;
    }

    public Cursor queryItems() {
        // Select all rows and columns
        return queryItems(null, null);
    }

    protected Cursor queryItems(String where, String limit) {
        return mDbHelper.getReadableDatabase().query(getTableName(),
                null, // All columns
                where, // Selection, i.e. where COLUMN_* = [value we're looking for]
                null, // selection args, none b/c id already specified in selection
                null, // group by
                null, // having
                getQuerySortOrder(), // order/sort by
                limit); // limit
    }

    /**
     * Deletes all rows in this table.
     */
    public final void clear() {
        mDbHelper.getWritableDatabase().delete(getTableName(), null/*all rows*/, null);
        notifyContentChanged();
    }

    private void notifyContentChanged() {
        LocalBroadcastHelper.sendBroadcast(mAppContext, getOnContentChangeAction());
    }
}
