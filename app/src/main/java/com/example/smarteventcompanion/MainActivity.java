package com.example.smarteventcompanion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnViewEvents = findViewById(R.id.btnViewEvents);
        Button btnRegistered = findViewById(R.id.btnRegisteredEvents);

        btnViewEvents.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, EventListActivity.class)));

        findViewById(R.id.btnBluetooth).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, BluetoothActivity.class)));

        btnRegistered.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegisteredEventsActivity.class)));
    }
}