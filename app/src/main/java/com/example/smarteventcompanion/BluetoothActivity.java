package com.example.smarteventcompanion;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {
    private BluetoothAdapter bluetooth;
    private TextView status;
    private ArrayAdapter<String> deviceList;
    private final ActivityResultLauncher<String> permissionRequest = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) updateStatus();
                else Toast.makeText(this, "Bluetooth permission is needed", Toast.LENGTH_SHORT).show();
            });
    private final ActivityResultLauncher<Intent> enableRequest = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> updateStatus());

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        status = findViewById(R.id.bluetoothStatus);
        ListView list = findViewById(R.id.bluetoothDeviceList);
        deviceList = new ArrayAdapter<>(this, R.layout.bluetooth_device_row);
        list.setAdapter(deviceList);
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetooth = manager == null ? null : manager.getAdapter();
        findViewById(R.id.bluetoothOn).setOnClickListener(v -> enableBluetooth());
        findViewById(R.id.bluetoothOff).setOnClickListener(v -> openBluetoothSettings());
        findViewById(R.id.bluetoothPaired).setOnClickListener(v -> showPaired());
        findViewById(R.id.bluetoothVisible).setOnClickListener(v -> makeDiscoverable());
        findViewById(R.id.openPhase2).setOnClickListener(v -> startActivity(new Intent(this, BluetoothPhase2Activity.class)));
        if (bluetooth == null) {
            status.setText("Bluetooth not supported on this device");
            for (int id : new int[]{R.id.bluetoothOn,R.id.bluetoothOff,R.id.bluetoothPaired,R.id.bluetoothVisible})
                findViewById(id).setEnabled(false);
        } else updateStatus();
    }
    private boolean hasConnectPermission() {
        return Build.VERSION.SDK_INT < 31 || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }
    private boolean requireConnectPermission() {
        if (hasConnectPermission()) return true;
        if (Build.VERSION.SDK_INT >= 31) permissionRequest.launch(Manifest.permission.BLUETOOTH_CONNECT);
        return false;
    }
    private void updateStatus() {
        if (bluetooth == null) return;
        if (!requireConnectPermission()) { status.setText("Bluetooth permission required"); return; }
        status.setText(bluetooth.isEnabled() ? "Bluetooth: ON" : "Bluetooth: OFF");
    }
    private void enableBluetooth() {
        if (!requireConnectPermission()) return;
        if (bluetooth.isEnabled()) { Toast.makeText(this,"Bluetooth already ON",Toast.LENGTH_SHORT).show(); return; }
        enableRequest.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
    }
    private void openBluetoothSettings() {
        startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
        Toast.makeText(this,"Use Settings to turn Bluetooth off",Toast.LENGTH_SHORT).show();
    }
    private void showPaired() {
        if (!requireConnectPermission()) return;
        deviceList.clear();
        if (!bluetooth.isEnabled()) { deviceList.add("Turn Bluetooth on first"); return; }
        Set<BluetoothDevice> paired = bluetooth.getBondedDevices();
        if (paired == null || paired.isEmpty()) { deviceList.add("No paired devices. Pair in Bluetooth Settings first."); return; }
        for (BluetoothDevice device : paired) {
            String name = device.getName();
            deviceList.add(name == null || name.isEmpty() ? "Unknown device" : name);
        }
    }
    private void makeDiscoverable() {
        if (!requireConnectPermission()) return;
        if (Build.VERSION.SDK_INT >= 31 && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            permissionRequest.launch(Manifest.permission.BLUETOOTH_ADVERTISE); return;
        }
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,120);
        enableRequest.launch(intent);
    }
    @Override protected void onResume() { super.onResume(); if (status != null && bluetooth != null) updateStatus(); }
}
