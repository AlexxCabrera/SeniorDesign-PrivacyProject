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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener{
    //main page components
    private static final String TAG = "MainActivity";
    String incomingMessage;

    BluetoothAdapter mBluetoothAdapter;
    Button btnEnableDisable_Discoverable;
    BluetoothConnectionService mBluetoothConnection;
    Button btnStartConnection;
    ListView NewDevices;
    String keySequence;
    Button SendKeyBtn;
    Button DiffieBtn;
    Button genKey;

    RelativeLayout connectionPage;
    RelativeLayout keypadPage;

    //keypad components
    EditText pinInput;
    TextView tvTop;
    String pinOrder;
    //For randomizing the pin pad
    int seed;
    int max;
    int min;

    //Difie stuff
    String shareDiffie;
    int privKey;
    int secretKey;
    int commonPrime;

    //AES
    AESEnc Ecoder;

    public BluetoothConnectionService getmBluetoothConnection(){
        return mBluetoothConnection;
    }

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
                //can be temp. used to avoid flooding while in EE building. 'B' for blade
                //if(device.getName().charAt(0) == 'B') { //(idea) as an alternative, MAC address can be used. Search a data base and only show those who are stored
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
        keypadInit();

    }

    private void diffieInit(){
        while (true) {
            commonPrime = (int) (Math.random() * (127 - 2) + 2);
            if(isPrime(commonPrime)){
                break;
            }
        }

        String StrPrime = commonPrime + "";
        String primRoot = findPrimitive(commonPrime) + "";
        privKey = ThreadLocalRandom.current().nextInt(1, commonPrime + 1);
        int myPublic = (Integer.parseInt(primRoot)^privKey) % commonPrime;

        shareDiffie = commonPrime +" "+ primRoot+" "+myPublic;

        send(shareDiffie);


        Toast.makeText(MainActivity.this, "Prime:" +commonPrime + " root:" + primRoot + " private:" + privKey + " public:" + myPublic, Toast.LENGTH_SHORT).show();

    }

    private void init(){
        Button btnONOFF = (Button) findViewById(R.id.btnONOFF);

        SendKeyBtn = (Button) findViewById(R.id.sendKey);
        btnEnableDisable_Discoverable = (Button) findViewById(R.id.btnDiscoverable_on_off);
        NewDevices = (ListView) findViewById(R.id.NewDevices);
        mBTDevices = new ArrayList<>();

        connectionPage = (RelativeLayout) findViewById(R.id.ConnectionPage);
        keypadPage = (RelativeLayout) findViewById(R.id.keyPadPage);

        btnStartConnection = (Button) findViewById(R.id.connectionButton);
        genKey = (Button) findViewById(R.id.genKey);
        DiffieBtn = (Button) findViewById(R.id.sendDif);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        NewDevices.setOnItemClickListener(MainActivity.this);

        genKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incomingMessage = mBluetoothConnection.getMessageIn();
               // int otherPub = Integer.parseInt(incomingMessage);
                secretKey = (Integer.parseInt(incomingMessage)^privKey) % commonPrime;
                Toast.makeText(getApplicationContext(), "Others Public:" + incomingMessage + "Common key: " + secretKey, Toast.LENGTH_SHORT).show();
            }
        });

        DiffieBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                diffieInit();
            }
        });
        SendKeyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //here should be the key being sent for keypad

                String EcryptedText = Ecoder.encrypt(pinOrder,privKey + "");
                send(pinOrder);
                Toast.makeText(getApplicationContext(), "Encryption: " + EcryptedText, Toast.LENGTH_SHORT).show();
                connectionPage.setVisibility(View.GONE);
                keypadPage.setVisibility(View.VISIBLE);
                //OpenKeyPadActivity();
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
                SendKeyBtn.setVisibility(View.VISIBLE);
                startConnection();
            }
        });

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

    // Keypad Screen
    public void OpenKeyPadActivity() {
        Intent intent = new Intent(this, keyPad.class);
        startActivity(intent);
    }

    private void send(String text){
        byte[] bytes = text.getBytes(Charset.defaultCharset());

        //encrypt out text here
        //////////////////////////////////////

        mBluetoothConnection.write(bytes);
    }

    private void keypadInit(){
        // Button IDs
        Button btn0 = (Button) findViewById(R.id.btn0);
        Button btn1 = (Button) findViewById(R.id.btn1);
        Button btn2 = (Button) findViewById(R.id.btn2);
        Button btn3 = (Button) findViewById(R.id.btn3);
        Button btn4 = (Button) findViewById(R.id.btn4);
        Button btn5 = (Button) findViewById(R.id.btn5);
        Button btn6 = (Button) findViewById(R.id.btn6);
        Button btn7 = (Button) findViewById(R.id.btn7);
        Button btn8 = (Button) findViewById(R.id.btn8);
        Button btn9 = (Button) findViewById(R.id.btn9);
        Button btnBlank1 = (Button) findViewById(R.id.btnBlank1);
        Button btnBlank2 = (Button) findViewById(R.id.btnBlank2);
        Button btnBlank3 = (Button) findViewById(R.id.btnblank3);
        Button btnEnter = (Button) findViewById(R.id.btnEnter);
        Button btnClear = (Button) findViewById(R.id.btnClear);
        Button btnCancel = (Button) findViewById(R.id.btnCancel);

        // Edit Text IDs
        pinInput = (EditText) findViewById(R.id.pinInput);
        tvTop = (TextView) findViewById(R.id.tvTop);

        List<Integer> Numbers = new ArrayList<Integer>();
        Numbers.add(0);
        Numbers.add(1);
        Numbers.add(2);
        Numbers.add(3);
        Numbers.add(4);
        Numbers.add(5);
        Numbers.add(6);
        Numbers.add(7);
        Numbers.add(8);
        Numbers.add(9);

        // Shuffles collection
        max = 1;
        min = 100;
        seed = (int) (int)(Math.random()*(max-min+1)+min);;
        Collections.shuffle(Numbers,new Random(seed));

        pinOrder = "";
        for(int i = 0; i< Numbers.size(); i++){
            pinOrder = pinOrder + Numbers.get(i).toString();
        }


        List<Button> buttons = new ArrayList<Button>();
        buttons.add((Button) findViewById(R.id.btn0));
        buttons.add((Button) findViewById(R.id.btn1));
        buttons.add((Button) findViewById(R.id.btn2));
        buttons.add((Button) findViewById(R.id.btn3));
        buttons.add((Button) findViewById(R.id.btn4));
        buttons.add((Button) findViewById(R.id.btn5));
        buttons.add((Button) findViewById(R.id.btn6));
        buttons.add((Button) findViewById(R.id.btn7));
        buttons.add((Button) findViewById(R.id.btn8));
        buttons.add((Button) findViewById(R.id.btn9));

        // Assigns randoms values to buttons
        for (int i = 0; i < Numbers.size(); i++) {
            buttons.get(i).setText(Numbers.get(i).toString());
        }

        btn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btn0.getText();
                //pinInput.setText(btn0.getText());
                String btnID = btn0.getText().toString();
                pinInput.setText(pinInput.getText() + btnID);
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btn1.getText();
                //pinInput.setText(btn1.getText());
                String btnID = btn1.getText().toString();
                pinInput.setText(pinInput.getText() + btnID);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btn2.getText();
                //pinInput.setText(btn2.getText());
                String btnID = btn2.getText().toString();
                pinInput.setText(pinInput.getText() + btnID);
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btn3.getText();
                //pinInput.setText(btn3.getText());
                String btnID = btn3.getText().toString();
                pinInput.setText(pinInput.getText() + btnID);
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btn4.getText();
                //pinInput.setText(btn4.getText());
                String btnID = btn4.getText().toString();
                pinInput.setText(pinInput.getText() + btnID);
            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btn5.getText();
                //pinInput.setText(btn5.getText());
                String btnID = btn5.getText().toString();
                pinInput.setText(pinInput.getText() + btnID);
            }
        });

        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btn6.getText();
                //pinInput.setText(btn6.getText());
                String btnID = btn6.getText().toString();
                pinInput.setText(pinInput.getText() + btnID);

            }
        });

        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btn7.getText();
                //pinInput.setText(btn7.getText());
                String btnID = btn7.getText().toString();
                pinInput.setText(pinInput.getText() + btnID);
            }
        });

        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btn8.getText();
               // pinInput.setText(btn8.getText());
                String btnID = btn8.getText().toString();
                pinInput.setText(pinInput.getText() + btnID);
            }
        });

        btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btn9.getText();
                //pinInput.setText(btn9.getText());
                String btnID = btn9.getText().toString();
                pinInput.setText(pinInput.getText() + btnID);
            }
        });

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tempPin = pinInput.getText().toString();

                // Pin number for User 1
                if (tempPin.equals("1234")) {
                    //OpenAccountActivity();
                    Toast.makeText(getApplicationContext(), "Valid Pin", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid Pin", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinInput.setText("");
                pinInput.requestFocus();
            }
        });

        btnBlank1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnBlank1.getText();
            }
        });

        btnBlank2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnBlank2.getText();
            }
        });

        btnBlank3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnBlank3.getText();
            }
        });

    }

    static void findPrimefactors(HashSet<Integer> s, int n)
    {
        // Print the number of 2s that divide n
        while (n % 2 == 0)
        {
            s.add(2);
            n = n / 2;
        }

        // n must be odd at this point. So we can skip
        // one element (Note i = i +2)
        for (int i = 3; i <= Math.sqrt(n); i = i + 2)
        {
            // While i divides n, print i and divide n
            while (n % i == 0)
            {
                s.add(i);
                n = n / i;
            }
        }

        // This condition is to handle the case when
        // n is a prime number greater than 2
        if (n > 2)
        {
            s.add(n);
        }
    }

    private static boolean isPrime(int n) {
        int i;
        for(i=2;i<=Math.sqrt(n);i++){
            if(n % i == 0){
                return false;
            }
        }
        return true;
    }

    static int power(int x, int y, int p)
    {
        int res = 1;     // Initialize result

        x = x % p; // Update x if it is more than or
        // equal to p

        while (y > 0)
        {
            // If y is odd, multiply x with result
            if (y % 2 == 1)
            {
                res = (res * x) % p;
            }

            // y must be even now
            y = y >> 1; // y = y/2
            x = (x * x) % p;
        }
        return res;
    }
    static int findPrimitive(int n)
    {
        HashSet<Integer> s = new HashSet<Integer>();

        // Check if n is prime or not
        if (isPrime(n) == false)
        {
            return -1;
        }

        // Find value of Euler Totient function of n
        // Since n is a prime number, the value of Euler
        // Totient function is n-1 as there are n-1
        // relatively prime numbers.
        int phi = n - 1;

        // Find prime factors of phi and store in a set
        findPrimefactors(s, phi);

        // Check for every number from 2 to phi
        for (int r = 2; r <= phi; r++)
        {
            // Iterate through all prime factors of phi.
            // and check if we found a power with value 1
            boolean flag = false;
            for (Integer a : s)
            {

                // Check if r^((phi)/primefactors) mod n
                // is 1 or not
                if (power(r, phi / (a), n) == 1)
                {
                    flag = true;
                    break;
                }
            }

            // If there was no power with value 1.
            if (flag == false)
            {
                return r;
            }
        }

        // If no primitive root found
        return -1;
    }
}