package com.example.smarteventcompanion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

public class EventDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Event Details");
        }

        String eventId = getIntent().getStringExtra("event_id");
        Event event = JsonHelper.getEventById(this, eventId);

        TextView txtName = findViewById(R.id.txtEventName);
        TextView txtDate = findViewById(R.id.txtEventDate);
        TextView txtLocation = findViewById(R.id.txtEventLocation);
        TextView txtDescription = findViewById(R.id.txtEventDescription);
        Button btnRegister = findViewById(R.id.btnRegister);

        if (event != null) {
            txtName.setText(event.getName());
            txtDate.setText("Date: " + event.getDate());
            txtLocation.setText("Location: " + event.getLocation());
            txtDescription.setText(event.getDescription());

            btnRegister.setOnClickListener(v -> {
                Intent intent = new Intent(EventDetailsActivity.this, RegisterActivity.class);
                intent.putExtra("event_id", event.getId());
                intent.putExtra("event_name", event.getName());
                startActivity(intent);
            });
        }
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