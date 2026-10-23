package com.example.smarteventcompanion;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

// TODO: Student 2 replaces loadEvents() body with real JSON parsing
public class JsonHelper {

    public static List<Event> loadEvents(Context context) {
        // TEMPORARY MOCK DATA
        List<Event> events = new ArrayList<>();
        events.add(new Event("1", "AI Workshop", "25/10/2026",
                "University Hall", "Introduction to Artificial Intelligence."));
        events.add(new Event("2", "Tech Conference", "02/11/2026",
                "Main Auditorium", "Annual technology conference."));
        events.add(new Event("3", "Career Fair", "10/11/2026",
                "Student Center", "Meet potential employers."));
        return events;
    }

    public static Event getEventById(Context context, String id) {
        for (Event e : loadEvents(context)) {
            if (e.getId().equals(id)) return e;
        }
        return null;
    }
}