package com.example.smarteventcompanion;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.view.MenuItem;

public class RegisteredEventsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_events);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Registered Events");
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerRegistered);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Event> events = DatabaseHelper.getRegisteredEvents(this);
        RegisteredEventAdapter adapter = new RegisteredEventAdapter(events);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}