package com.android.bike.janus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class SendArduinoNotification {

    private final String TAG = "connectWithArduino";

    //Bluetooth Api variables
    private BluetoothAdapter BTAdapter;
    private OutputStream outStream;
    private InputStream inStream;

    public void connectArduino(){
        Log.i(TAG, "Connect to arduino");
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (BTAdapter == null) {
            Log.i(TAG, "Your phone does not support bluetooth!");
        } else if (!BTAdapter.isEnabled()) {
            Log.i(TAG, "Bluetooth is not enabled!");
        } else {
            BluetoothDevice mmDevice;
            Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();
            Log.i(TAG, "Paired Devices are:" + pairedDevices);
            BluetoothDevice[] devices = pairedDevices.toArray(new BluetoothDevice[pairedDevices.size()]);
            for (int i = 0; i < pairedDevices.size(); i++) {
                Log.i(TAG, "Device " + i + " : " + devices[i].getName());
            }
            if (pairedDevices.size() > 0) {
                for (int i = 0; i < pairedDevices.size(); i++) {
                    if (devices[i].getName().equals("HC-05")) {
                        Log.i(TAG, "HC05 Present!" + devices[i].getName());
                        mmDevice = devices[i];
                        connectBluetooth(mmDevice);
                        break;
                    }
                }
            }
        }
    }

    private void connectBluetooth(BluetoothDevice mmDevice) {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        BluetoothSocket mmSocket = null;
        try {
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            BTAdapter.cancelDiscovery();
            mmSocket.connect();
            Log.i(TAG, "Socket Connected");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outStream = mmSocket.getOutputStream();
            Log.i(TAG, "outStream Connected");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inStream = mmSocket.getInputStream();
            Log.i(TAG, "inStream Connected");
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendNotification();
    }

    private void sendNotification(){
        try {
            outStream.write("n".getBytes());
            Log.i(TAG, "Notification sent!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
