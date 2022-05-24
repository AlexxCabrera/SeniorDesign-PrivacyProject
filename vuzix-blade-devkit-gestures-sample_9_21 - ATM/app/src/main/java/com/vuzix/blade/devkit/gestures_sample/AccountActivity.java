package com.vuzix.blade.devkit.gestures_sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;



public class AccountActivity extends Activity {

    KeyGenerator keyGenerator;
    SecretKey secretKey;
    byte[] IV = new byte[16];
    SecureRandom random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Button btnEncrypt = (Button) findViewById(R.id.btnEncrypt);
        Button btnDecrypt = (Button) findViewById(R.id.btnDecrypt);
        TextView TVmsg = (TextView) findViewById(R.id.TVmsg);
        TextView TVdmsg = (TextView) findViewById(R.id.TVdmsg);
        EditText ETmsg = (EditText) findViewById(R.id.ETmsg);
        EditText ETdmsg = (EditText) findViewById(R.id.ETdmsg);

        btnEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    byte[] encrypt = encrypt(ETmsg.getText().toString().getBytes(), secretKey, IV);
                    String encryptText = new String(encrypt, "UTF-8");
                    TVmsg.setText(encryptText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnDecrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    byte[] encrypt = encrypt(ETdmsg.getText().toString().getBytes(), secretKey, IV);
                    String decrypt = decrypt(encrypt, secretKey, IV);
                    TVdmsg.setText(decrypt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            secretKey = keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }

        random = new SecureRandom();
        random.nextBytes(IV);
    }

    public static String decrypt(byte[] cipherText, SecretKey key, byte[] IV) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decryptedText = cipher.doFinal(cipherText);
            return new String(decryptedText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encrypt(byte[] plaintext, SecretKey key, byte[] IV) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] cipherText = cipher.doFinal(plaintext);
        return cipherText;
    }

}
