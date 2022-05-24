package com.vuzix.blade.devkit.gestures_sample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener{
    private static final String TAG = "MainActivity";
    BluetoothAdapter mBluetoothAdapter;
    Button btnEnableDisable_Discoverable;
    Button KeyPadBtn;
    Button diffieBtn;
    BluetoothConnectionService mBluetoothConnection;
    Button btnStartConnection;
    ListView NewDevices;
    RelativeLayout keyPad;

    AESDec Decoder;
    int secretKey;

    String incomingMessage;

    //Keypad slots
    //letter:column Row:number
    TextView a1;
    TextView a2;
    TextView a3;
    TextView a4;

    TextView b1;
    TextView b2;
    TextView b3;
    TextView b4;

    TextView c1;
    TextView c2;
    TextView c3;
    TextView c4;

    TextView d1;
    TextView d2;
    TextView d3;
    TextView d4;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    BluetoothDevice mBTDevice;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();

    public DeviceListAdapter mDeviceListAdapter;

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                //can be temp. used to avoid flooding while in EE building. 'T' for tablet
                //if(device.getName().charAt(0) == 'T'){ //(idea) as an alternative, MAC address can be used. Search a data base and only show those who are stored
                    mBTDevices.add(device);
                    Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                    mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                    NewDevices.setAdapter(mDeviceListAdapter);
                //}
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    mBTDevice = mDevice;
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void diffieInit(){
        incomingMessage = mBluetoothConnection.getMessageIn();
        String prime = "";
        String root = "";
        String otherPubKey = "";
        int flag = 0;
        for(int i =0; i < incomingMessage.length(); i++){
            if(incomingMessage.charAt(i) == ' '){
                flag++;
                continue;
            }
            if(flag == 0) {
                prime = prime + incomingMessage.charAt(i);
            }
            else if(flag == 1){
                root = root + incomingMessage.charAt(i);
            }
            else{
                otherPubKey = otherPubKey + incomingMessage.charAt(i);
            }
        }

        int PRIME = Integer.parseInt(prime);
        int myPrivate = ThreadLocalRandom.current().nextInt(1, PRIME + 1);
        int myPublic = (Integer.parseInt(root)^myPrivate) % PRIME;

        send(myPublic + "");

        secretKey = (Integer.parseInt(otherPubKey)^myPrivate) % PRIME;

        Toast.makeText(MainActivity.this, "Prime" + prime + " Root: " + root + " Other Pub:" + otherPubKey + " Common Key: " + secretKey , Toast.LENGTH_SHORT).show();

    }

    private void send(String text){
        byte[] bytes = text.getBytes(Charset.defaultCharset());

        //encrypt out text here
        //////////////////////////////////////

        mBluetoothConnection.write(bytes);
    }
    private void init(){
        Button btnONOFF = (Button) findViewById(R.id.btnONOFF);

        //default is all x's
        incomingMessage = "xxxxxxxxxx";

        //keypad values
        a1 = (TextView) findViewById(R.id.a1);
        a2 = (TextView) findViewById(R.id.a2);
        a3 = (TextView) findViewById(R.id.a3);
        a4 = (TextView) findViewById(R.id.a4);

        b1 = (TextView) findViewById(R.id.b1);
        b2 = (TextView) findViewById(R.id.b2);
        b3 = (TextView) findViewById(R.id.b3);
        b4 = (TextView) findViewById(R.id.b4);

        c1 = (TextView) findViewById(R.id.c1);
        c2 = (TextView) findViewById(R.id.c2);
        c3 = (TextView) findViewById(R.id.c3);
        c4 = (TextView) findViewById(R.id.c4);

        d1 = (TextView) findViewById(R.id.d1);
        d2 = (TextView) findViewById(R.id.d2);
        d3 = (TextView) findViewById(R.id.d3);
        d4 = (TextView) findViewById(R.id.d4);

        //keypad button and keypad
        KeyPadBtn = (Button) findViewById(R.id.keyPadBtn);
        keyPad = (RelativeLayout) findViewById(R.id.keyPad);
        diffieBtn = (Button) findViewById(R.id.getVals);


        btnEnableDisable_Discoverable = (Button) findViewById(R.id.btnDiscoverable_on_off);
        NewDevices = (ListView) findViewById(R.id.NewDevices);
        mBTDevices = new ArrayList<>();

        btnStartConnection = (Button) findViewById(R.id.connectionButton);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        NewDevices.setOnItemClickListener(MainActivity.this);

        diffieBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                diffieInit();
            }
        });

        KeyPadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //incomingMessage = mBluetoothConnection.getMessageIn();
                setKeyPadValues();
                //Toast.makeText(MainActivity.this, incomingMessage, Toast.LENGTH_SHORT).show();
            }
        });

        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth.");
                enableDisableBT();
            }
        });

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyPadBtn.setVisibility(View.VISIBLE);
                keyPad.setVisibility(View.VISIBLE);
                startConnection();
            }
        });
    }
    private void setKeyPadValues(){

        //incomingMessage should be sent from ATM as soon as connection is made.
        //Here we display the message as the keypad values
        incomingMessage = mBluetoothConnection.getMessageIn();

        Toast.makeText(getApplicationContext(), "Decryption: " + incomingMessage, Toast.LENGTH_SHORT).show();
        //decrypt incomingMessage here
        //////////////////////////////////////
        //String KEY = secretKey + "";
        //String DecryptionText = Decoder.decrypt(incomingMessage, KEY);

        //incomingMessage = DecryptionText;
        if(incomingMessage.length() > 10){
            incomingMessage = "0123456789";
            Toast.makeText(MainActivity.this,"keypad string was too long. set to default",Toast.LENGTH_SHORT).show();
        }

        a1.setText(Character.toString(incomingMessage.charAt(0)));
        a2.setText(Character.toString(incomingMessage.charAt(1)));
        a3.setText(Character.toString(incomingMessage.charAt(2)));

        //a4 remains empty
        //a4.setText();

        b1.setText(Character.toString(incomingMessage.charAt(3)));
        b2.setText(Character.toString(incomingMessage.charAt(4)));
        b3.setText(Character.toString(incomingMessage.charAt(5)));

        //b4 is enter
        //b4.setText();

        c1.setText(Character.toString(incomingMessage.charAt(6)));
        c2.setText(Character.toString(incomingMessage.charAt(7)));
        c3.setText(Character.toString(incomingMessage.charAt(8)));

        //c4 is clear
        //c4.setText();

        //d1 d3 are empty
       // d1.setText(Character.toString(incomingMessage.charAt(9)));
        d2.setText(Character.toString(incomingMessage.charAt(9)));
        //d3.setText(Character.toString(incomingMessage.charAt(11)));

        //d4 is cancel
        //d4.setText(Character.toString();
    }

    public void startConnection(){
        startBTConnection(mBTDevice,MY_UUID_INSECURE);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");

        mBluetoothConnection.startClient(device,uuid);
        NewDevices.setVisibility(View.GONE);
    }

    public void enableDisableBT(){
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT.");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }

    }

    public void btnEnableDisable_Discoverable(View view) {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);

    }

    public void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){


            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(i).createBond();

            mBTDevice = mBTDevices.get(i);
            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
        }
    }
}