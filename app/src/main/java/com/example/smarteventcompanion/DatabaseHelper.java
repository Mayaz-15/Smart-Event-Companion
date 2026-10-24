
package com.example.smarteventcompanion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "smart_event_companion.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_REGISTRATIONS = "registrations";
    private static final String COL_EVENT_ID = "event_id";
    private static final String COL_NAME = "event_name";
    private static final String COL_DATE = "event_date";
    private static final String COL_LOCATION = "event_location";
    private static final String COL_DESCRIPTION = "event_description";
    private static final String COL_REGISTERED_AT = "registered_at";

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(),
                DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_REGISTRATIONS + " ("
                + COL_EVENT_ID + " TEXT PRIMARY KEY, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_DATE + " TEXT NOT NULL, "
                + COL_LOCATION + " TEXT NOT NULL, "
                + COL_DESCRIPTION + " TEXT NOT NULL, "
                + COL_REGISTERED_AT + " INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        // Future database migrations can be added here.
    }

    public static boolean insertRegistration(
            Context context, String eventId) {

        if (eventId == null || eventId.trim().isEmpty()) {
            return false;
        }

        Event event = JsonHelper.getEventById(context, eventId);

        if (event == null) {
            Log.w(TAG, "Unknown event ID: " + eventId);
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COL_EVENT_ID, event.getId());
        values.put(COL_NAME, event.getName());
        values.put(COL_DATE, event.getDate());
        values.put(COL_LOCATION, event.getLocation());
        values.put(COL_DESCRIPTION, event.getDescription());
        values.put(COL_REGISTERED_AT, System.currentTimeMillis());

        try (DatabaseHelper helper = new DatabaseHelper(context)) {
            SQLiteDatabase db = helper.getWritableDatabase();

            long result = db.insertWithOnConflict(
                    TABLE_REGISTRATIONS,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
            );

            return result != -1;

        } catch (SQLiteException e) {
            Log.e(TAG, "Could not save registration", e);
            return false;
        }
    }

    public static List<Event> getRegisteredEvents(Context context) {

        List<Event> registered = new ArrayList<>();

        try (DatabaseHelper helper = new DatabaseHelper(context)) {
            SQLiteDatabase db = helper.getReadableDatabase();

            try (Cursor cursor = db.query(
                    TABLE_REGISTRATIONS,
                    new String[]{
                            COL_EVENT_ID,
                            COL_NAME,
                            COL_DATE,
                            COL_LOCATION,
                            COL_DESCRIPTION
                    },
                    null, null, null, null,
                    COL_REGISTERED_AT + " DESC, rowid DESC")) {

                int idCol = cursor.getColumnIndexOrThrow(COL_EVENT_ID);
                int nameCol = cursor.getColumnIndexOrThrow(COL_NAME);
                int dateCol = cursor.getColumnIndexOrThrow(COL_DATE);
                int locationCol = cursor.getColumnIndexOrThrow(COL_LOCATION);
                int descriptionCol = cursor.getColumnIndexOrThrow(COL_DESCRIPTION);

                while (cursor.moveToNext()) {
                    registered.add(new Event(
                            cursor.getString(idCol),
                            cursor.getString(nameCol),
                            cursor.getString(dateCol),
                            cursor.getString(locationCol),
                            cursor.getString(descriptionCol)
                    ));
                }
            }

        } catch (SQLiteException e) {
            Log.e(TAG, "Could not load events", e);
        }

        return registered;
    }

    public static boolean isRegistered(
            Context context, String eventId) {

        if (eventId == null || eventId.trim().isEmpty()) {
            return false;
        }

        try (DatabaseHelper helper = new DatabaseHelper(context)) {
            SQLiteDatabase db = helper.getReadableDatabase();

            try (Cursor cursor = db.query(
                    TABLE_REGISTRATIONS,
                    new String[]{COL_EVENT_ID},
                    COL_EVENT_ID + " = ?",
                    new String[]{eventId},
                    null, null, null, "1")) {

                return cursor.moveToFirst();
            }

        } catch (SQLiteException e) {
            Log.e(TAG, "Could not check registration", e);
            return false;
        }
    }
}
