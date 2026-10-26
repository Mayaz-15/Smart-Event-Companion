
package com.example.smarteventcompanion;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import org.json.JSONException;

public class JsonHelper {

    public static List<Event> loadEvents(Context context) {
        List<Event> events = new ArrayList<>();

        try (InputStream inputStream =
                     context.getAssets().open("events.json")) {

            byte[] buffer = new byte[inputStream.available()];
            int offset = 0;

            while (offset < buffer.length) {
                int count = inputStream.read(
                        buffer, offset, buffer.length - offset);
                if (count == -1) {
                    throw new IOException("Incomplete JSON file");
                }
                offset += count;
            }

            String json = new String(
                    buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);

                String id = object.getString("id");
                String name = object.getString("name");
                String date = object.getString("date");
                String location = object.getString("location");
                String description =
                        object.getString("description");

                events.add(new Event(
                        id, name, date, location, description));
            }

        } catch (IOException | JSONException e) {
            android.util.Log.e(
                    "JsonHelper", "Failed to load events", e);
        }

        return events;
    }

    public static Event getEventById(
            Context context, String id) {

        for (Event e : loadEvents(context)) {
            if (e.getId().equals(id)) {
                return e;
            }
        }
        return null;
    }
}
