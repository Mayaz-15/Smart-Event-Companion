package com.example.smarteventcompanion;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Standalone Phase 2 screen. Does not alter existing Phase 1 BluetoothActivity. */
public class BluetoothPhase2Activity extends AppCompatActivity {
    private BluetoothAdapter bluetooth;
    private BluetoothChatService chat;
    private TextView status, log;
    private EditText payload;
    private ArrayAdapter<String> pairedAdapter;
    private final List<BluetoothDevice> pairedDevices = new ArrayList<>();
    private boolean organizerMode = false;
    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), results -> {
                if (!hasPermissions()) show("Bluetooth permission denied. Grant Nearby devices permission.");
                else show("Permission granted. Choose Host or Paired Devices.");
            });
    private final ActivityResultLauncher<Intent> enableLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> show("Bluetooth enable request completed."));

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_phase2);
        status = findViewById(R.id.phase2Status);
        log = findViewById(R.id.phase2Log);
        payload = findViewById(R.id.phase2Message);
        ListView list = findViewById(R.id.phase2Devices);
        pairedAdapter = new ArrayAdapter<>(this, R.layout.bluetooth_device_row);
        list.setAdapter(pairedAdapter);
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetooth = manager == null ? null : manager.getAdapter();
        if (bluetooth == null) { show("Bluetooth unsupported on this phone."); return; }
        chat = new BluetoothChatService(bluetooth, new BluetoothChatService.Listener() {
            @Override public void onStatus(String text) { show(text); }
            @Override public void onMessage(String text) {
                log.append("\nReceived: " + text);
                // The host/organizer records well-formed attendance. Other messages remain chat only.
                if (organizerMode && text.startsWith("EVENT:")) saveIncomingAttendance(text);
            }
            @Override public void onError(String text) { show(text); }
        });
        findViewById(R.id.phase2Host).setOnClickListener(v -> {
            if (ready()) { organizerMode = true; chat.startHost(); }
        });
        findViewById(R.id.phase2Refresh).setOnClickListener(v -> loadPaired());
        findViewById(R.id.phase2Send).setOnClickListener(v -> {
            if (ready()) chat.send(payload.getText().toString());
        });
        findViewById(R.id.phase2Disconnect).setOnClickListener(v -> {
            chat.stop(); organizerMode = false; show("Disconnected.");
        });
        list.setOnItemClickListener((parent, view, position, id) -> {
            if (ready() && position < pairedDevices.size()) {
                organizerMode = false;
                chat.connect(pairedDevices.get(position));
            }
        });
        findViewById(R.id.phase2Attendance).setOnClickListener(v -> showAttendanceRecords());
        requestPermissionsIfNeeded();
    }

    private void saveIncomingAttendance(String text) {
        // Expected protocol: EVENT:1;ATTENDEE:Student3
        String[] fields = text.split(";", -1);
        if (fields.length != 2 || !fields[0].startsWith("EVENT:") ||
                !fields[1].startsWith("ATTENDEE:")) {
            log.append("\nAttendance not saved: use EVENT:1;ATTENDEE:Student3");
            return;
        }
        String eventId = fields[0].substring("EVENT:".length()).trim();
        String attendeeId = fields[1].substring("ATTENDEE:".length()).trim();
        if (eventId.isEmpty() || attendeeId.isEmpty()) {
            log.append("\nAttendance not saved: missing event or attendee ID.");
            return;
        }
        new Thread(() -> {
            boolean saved = DatabaseHelper.saveAttendance(getApplicationContext(), eventId, attendeeId);
            runOnUiThread(() -> log.append(saved
                    ? "\nAttendance saved in SQLite: " + eventId + " / " + attendeeId
                    : "\nAttendance not saved: verify event ID in events.json and database."));
        }, "attendance-save").start();
    }

    private void showAttendanceRecords() {
        new Thread(() -> {
            List<DatabaseHelper.AttendanceRecord> entries =
                    DatabaseHelper.getAttendanceRecords(getApplicationContext());
            StringBuilder result = new StringBuilder();
            if (entries.isEmpty()) result.append("No attendance records saved yet.");
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,
                    Locale.getDefault());
            for (DatabaseHelper.AttendanceRecord entry : entries) {
                result.append("Event: ").append(entry.eventId)
                        .append(" | Attendee: ").append(entry.attendeeId)
                        .append(" | ").append(df.format(new Date(entry.recordedAt))).append("\n");
            }
            runOnUiThread(() -> new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Saved attendance (SQLite)")
                    .setMessage(result.toString())
                    .setPositiveButton("Close", null).show());
        }, "attendance-read").start();
    }

    private boolean hasPermissions() {
        if (Build.VERSION.SDK_INT < 31) return true;
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermissionsIfNeeded() {
        if (!hasPermissions() && Build.VERSION.SDK_INT >= 31)
            permissionLauncher.launch(new String[]{Manifest.permission.BLUETOOTH_CONNECT});
    }
    private boolean ready() {
        if (bluetooth == null) { show("Bluetooth unsupported."); return false; }
        if (!hasPermissions()) { requestPermissionsIfNeeded(); return false; }
        if (!bluetooth.isEnabled()) {
            enableLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            show("Turn Bluetooth on, then retry.");
            return false;
        }
        return true;
    }
    private void loadPaired() {
        if (!ready()) return;
        pairedAdapter.clear();
        pairedDevices.clear();
        Set<BluetoothDevice> bonded = bluetooth.getBondedDevices();
        if (bonded != null) for (BluetoothDevice d : bonded) {
            pairedDevices.add(d);
            String name = d.getName();
            pairedAdapter.add((name == null ? "Unknown device" : name) + " (" + d.getAddress() + ")");
        }
        if (pairedDevices.isEmpty()) show("No paired phones. Pair both phones in Android Bluetooth Settings first.");
        else show("Tap the other phone in the list to connect.");
    }
    private void show(String text) { status.setText(text); }
    @Override protected void onDestroy() { if (chat != null) chat.stop(); super.onDestroy(); }
}
