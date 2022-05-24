package com.vuzix.blade.devkit.gestures_sample;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.nfc.Tag;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "MY_APP_DEBUG_TAG";

    private static final String appName = "Glass_App";

    private static final UUID My_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    public String messageIn;
    public String getMessageIn() {
        return messageIn;
    }

    public BluetoothConnectionService(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    private class AcceptThread extends Thread{
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, My_UUID_INSECURE);
                Log.d(TAG, "AcceptThread: Setting up Server using: " + My_UUID_INSECURE);
            }catch (IOException e){

            }
            mmServerSocket = tmp;
        }
        public void run(){
            Log.d(TAG,"run: AcceptThread Running.");
            BluetoothSocket socket = null;

            try {
                Log.d(TAG,"run: RFCOM server socket start.....");
                socket = mmServerSocket.accept();
                Log.d(TAG,"run: RFCOM server socket accepted connection.");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(socket != null){
                connected(socket,mmDevice);
            }
            Log.i(TAG, "END mAcceptThread");
        }

        public void cancel(){
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try{
                mmServerSocket.close();
            }catch (IOException e){
                Log.e(TAG,"cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage());
            }
        }
    }

    //connects two devices
    private class ConnectThread extends Thread{
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid){
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread");

            try {
                Log.d(TAG,"ConnectThread: Trying to create InsecureRfcommSocket using UUID: " + My_UUID_INSECURE);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }
            mmSocket = tmp;

            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
                Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                try {
                    mmSocket.close();
                    Log.d(TAG,"run: Closed Socket.");
                } catch (IOException ioException) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + ioException.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + My_UUID_INSECURE);
            }
            connected(mmSocket,mmDevice);
        }

        public void cancel(){
            try{
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            }catch (IOException ioException){
                Log.e(TAG, "cancel: close() if mmSocket in Connectthread failed. " + ioException.getMessage());
            }
        }
    }


    public synchronized void start(){
        Log.d(TAG, "start");

        if(mConnectThread != null){
           mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mInsecureAcceptThread == null){
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startClient: Started.");

        mProgressDialog = mProgressDialog.show(mContext, "Connecting Bluetooth", "Please wait...", true);

        mConnectThread = new ConnectThread(device,uuid);
        mConnectThread.start();
    }

    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            Log.d(TAG,"ConnectedThread: Starting." );

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try{
                mProgressDialog.dismiss();
            }catch (NullPointerException e){
                e.printStackTrace();
            }

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;

            while(true){
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG,"InputStream: " + incomingMessage);
                    messageIn = incomingMessage;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    break;
                }
            }
        }

        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try{
                mmOutStream.write(bytes);
            }catch (IOException ioException){
                Log.e(TAG, "write: Error writing to output stream. " + ioException.getMessage());
            }
        }

        public void cancel(){
            try{
                mmSocket.close();
            }catch (IOException ioException){

            }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice){
        Log.d(TAG, "connected: Starting.");

        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        mConnectedThread.write(out);
    }

}
