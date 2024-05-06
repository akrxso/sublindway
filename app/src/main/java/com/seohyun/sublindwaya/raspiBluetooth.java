package com.seohyun.sublindwaya;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class raspiBluetooth extends AppCompatActivity {

    private static final String TAG = "raspiBluetooth";
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001133-0000-1000-8000-00805f9b34fb");
    private static final String DEVICE_NAME = "greenboogie";
    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket = null;
    private TextView connectResult;
    private ConnectedThread connectedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raspi_bluetooth);

        connectResult = findViewById(R.id.connectResult);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(TAG, "onCreate: BluetoothAdapter obtained");

        checkBluetoothState();
    }

    private void checkBluetoothState() {
        Log.d(TAG, "Checking Bluetooth state");
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth is not available");
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        } else if (!bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth is not enabled, requesting to enable");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void autoConnectToDevice(String deviceName) {
        Log.d(TAG, "Attempting to auto-connect to device: " + deviceName);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "BLUETOOTH_CONNECT permission not granted");
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(deviceName)) {
                    Log.d(TAG, "Device found, attempting to connect");
                    connectToDevice(device);
                    break;
                }
            }
        } else {
            Log.w(TAG, "No paired devices found");
            connectResult.setText("No paired devices found.");
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.d(TAG, "Connecting to device: " + device.getName());
        try {
            bluetoothSocket = createBluetoothSocket(device);
            bluetoothSocket.connect();
            connectResult.setText("Connected to " + device.getName());
            connectedThread = new ConnectedThread(bluetoothSocket);
            connectedThread.start();
        } catch (IOException e) {
            Log.e(TAG, "Connection failed: " + e.getMessage());
            connectResult.setText("Connection failed!");
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        Log.d(TAG, "Creating Bluetooth socket");
        try {
            final Method m = device.getClass().getMethod("createRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create RFComm Connection", e);
            throw new IOException(e);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Cleaning up resources");
        super.onDestroy();
        if (connectedThread != null) {
            connectedThread.cancel();
        }
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
