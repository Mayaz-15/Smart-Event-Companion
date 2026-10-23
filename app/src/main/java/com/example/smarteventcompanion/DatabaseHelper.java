package com.example.smarteventcompanion;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

// TODO: Student 4 replaces these methods with real SQLite
public class DatabaseHelper {

    public static boolean insertRegistration(Context context, String eventId) {
        return true; // fake success
    }

    public static List<Event> getRegisteredEvents(Context context) {
        // TEMPORARY: return first event as a demo
        List<Event> list = new ArrayList<>();
        return list;
    }

    public static boolean isRegistered(Context context, String eventId) {
        return false; // fake
    }
}