package com.example.smarteventcompanion;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Classic Bluetooth RFCOMM (both phones must run this app with the same UUID). */
public final class BluetoothChatService {
    // UUID identifies our application service, not a particular device.
    public static final UUID SERVICE_UUID = UUID.fromString("93d6979f-820e-4bdf-82a8-6d58a85ac1bf");
    private static final String SERVICE_NAME = "SmartEventCompanion";
    public interface Listener {
        void onStatus(String status);
        void onMessage(String message);
        void onError(String error);
    }
    private final BluetoothAdapter adapter;
    private final Listener listener;
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile boolean running;
    private BluetoothServerSocket server;
    private BluetoothSocket socket;
    private BufferedWriter writer;

    public BluetoothChatService(BluetoothAdapter adapter, Listener listener) {
        this.adapter = adapter;
        this.listener = listener;
    }
    private void status(String text) { main.post(() -> listener.onStatus(text)); }
    private void error(String text) { main.post(() -> listener.onError(text)); }
    private void message(String text) { main.post(() -> listener.onMessage(text)); }

    @SuppressLint("MissingPermission") // Caller requests BLUETOOTH_CONNECT before invoking.
    public synchronized void startHost() {
        stop();
        running = true;
        new Thread(() -> {
            BluetoothServerSocket listening = null;
            try {
                listening = adapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID);
                synchronized (this) { server = listening; }
                status("Waiting for another phone to connect...");
                BluetoothSocket accepted = listening.accept();
                if (running) useSocket(accepted); else accepted.close();
            } catch (IOException e) {
                if (running) error("Host failed: " + e.getMessage());
            } finally {
                closeQuietly(listening);
                synchronized (this) { if (server == listening) server = null; }
            }
        }, "bt-host").start();
    }

    @SuppressLint("MissingPermission") // Caller requests BLUETOOTH_CONNECT before invoking.
    public synchronized void connect(BluetoothDevice device) {
        stop();
        running = true;
        new Thread(() -> {
            BluetoothSocket candidate = null;
            try {
                status("Connecting...");
                candidate = device.createRfcommSocketToServiceRecord(SERVICE_UUID);
                candidate.connect();
                if (running) useSocket(candidate); else candidate.close();
            } catch (IOException e) {
                closeQuietly(candidate);
                if (running) error("Connection failed: " + e.getMessage());
            }
        }, "bt-connect").start();
    }

    private void useSocket(BluetoothSocket connected) {
        synchronized (this) { socket = connected; }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connected.getInputStream(), StandardCharsets.UTF_8));
            synchronized (this) { writer = new BufferedWriter(new OutputStreamWriter(connected.getOutputStream(), StandardCharsets.UTF_8)); }
            status("Connected. You can send an event or attendance message.");
            String line;
            while (running && (line = reader.readLine()) != null) {
                message(line);
            }
            if (running) status("Other phone disconnected.");
        } catch (IOException e) {
            if (running) error("Bluetooth disconnected: " + e.getMessage());
        } finally {
            synchronized (this) {
                if (socket == connected) { socket = null; writer = null; }
            }
            closeQuietly(connected);
        }
    }

    public void send(String text) {
        if (text == null || text.trim().isEmpty()) { error("Enter a message first."); return; }
        // Plain one-line UTF-8 message. Database integration can later parse this protocol.
        final String safe = text.replace("\r", " ").replace("\n", " ").trim();
        new Thread(() -> {
            try {
                synchronized (this) {
                    if (writer == null) { error("Connect two phones first."); return; }
                    writer.write(safe);
                    writer.newLine();
                    writer.flush();
                }
                status("Sent: " + safe);
            } catch (IOException e) { error("Send failed: " + e.getMessage()); }
        }, "bt-send").start();
    }

    public synchronized void stop() {
        running = false;
        closeQuietly(server);
        closeQuietly(socket);
        server = null;
        socket = null;
        writer = null;
    }
    private static void closeQuietly(java.io.Closeable c) {
        if (c != null) try { c.close(); } catch (IOException ignored) { }
    }
}
