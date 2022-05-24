package com.vuzix.blade.devkit.gestures_sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.SecretKey;

public class keyPad extends Activity {


    EditText pinInput;
    TextView tvTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Removes app title
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.key_pad);

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
        Collections.shuffle(Numbers);

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
                //pinInput.setText(btn8.getText());
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
                    OpenAccountActivity();
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

    // Account Activity
    public void OpenAccountActivity() {
        Intent intent = new Intent(this, AccountActivity.class);
        startActivity(intent);
    }

    /*
    // Login Activity
    public void OpenLoginActivity() {
        Intent intent = new Intent(this, AccountActivity.class);
        startActivity(intent);
    }*/

    // TOAST msg
    private void showToast(String tvTop) {
        Toast.makeText(this, tvTop, Toast.LENGTH_SHORT).show();
    }

}