package com.example.smarteventcompanion;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Register");
        }

        String eventId = getIntent().getStringExtra("event_id");
        String eventName = getIntent().getStringExtra("event_name");

        TextView txtConfirm = findViewById(R.id.txtConfirmName);
        txtConfirm.setText(eventName);

        Button btnConfirm = findViewById(R.id.btnConfirmRegister);
        btnConfirm.setOnClickListener(v -> {
            boolean success = DatabaseHelper.insertRegistration(this, eventId);
            if (success) {
                Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                finish(); // go back
            } else {
                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
            }
        });
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
