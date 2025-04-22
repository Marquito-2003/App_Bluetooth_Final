package com.example.appbluetoothfinal;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class AppState extends Application {
    private static AppState instance;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private ConnectedThread connectedThread;
    private Handler currentHandler;
    private boolean isConnected = false;

    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public static final int MESSAGE_READ = 4;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static synchronized AppState getInstance() {
        return instance;
    }
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }


    public boolean connect(String macAddress, Context context) {
        if (!checkBluetoothConnectPermission(context)) {
            Log.e("AppState", "Bluetooth permission not granted");
            return false;
        }

        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
            socket.connect();
            connectedThread = new ConnectedThread(socket);
            connectedThread.start();
            isConnected = true;
            return true;
        } catch (SecurityException e) {
            Log.e("AppState", "Security Exception: " + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.e("AppState", "Connection failed", e);
            disconnect();
            return false;
        }
    }

    public void disconnect() {
        isConnected = false;
        try {
            if (connectedThread != null) {
                connectedThread.cancel();
                connectedThread = null;
            }
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            Log.e("AppState", "Disconnect error", e);
        }
    }

    public void sendData(String data) {
        if (isConnected && connectedThread != null) {
            connectedThread.write(data.getBytes());
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public Set<BluetoothDevice> getPairedDevices(Context context) {
        if (!checkBluetoothConnectPermission(context)) {
            Log.e("AppState", "Permission denied for getPairedDevices");
            return null;
        }
        return bluetoothAdapter.getBondedDevices();
    }

    public boolean checkBluetoothConnectPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }


    public void setCurrentHandler(Handler handler) {
        this.currentHandler = handler;
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer = new byte[1024];

        ConnectedThread(BluetoothSocket socket) throws IOException {
            mmInStream = socket.getInputStream();
            mmOutStream = socket.getOutputStream();
        }

        public void run() {
            while (isConnected) {
                try {
                    int numBytes = mmInStream.read(mmBuffer);
                    String receivedData = new String(mmBuffer, 0, numBytes);
                    if (currentHandler != null) {
                        Message message = currentHandler.obtainMessage(
                                MESSAGE_READ, numBytes, -1, receivedData);
                        currentHandler.sendMessage(message);
                    }
                } catch (IOException e) {
                    Log.e("ConnectedThread", "Input stream disconnected", e);
                    disconnect();
                    break;
                }
            }
        }

        void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("ConnectedThread", "Error writing to output stream", e);
            }
        }

        void cancel() {
            try {
                mmInStream.close();
                mmOutStream.close();
            } catch (IOException e) {
                Log.e("ConnectedThread", "Error closing streams", e);
            }
        }
    }
}