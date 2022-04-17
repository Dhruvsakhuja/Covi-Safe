package com.example.covi_safe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class QRcode extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button button;
    private ImageView qrCode;
    private TextView nameDisplay, regDisplay;
    FirebaseDatabase database,database2;
    DatabaseReference myRef,slotRef;
    public static String sName,sReg,sData,sEData;
    KeyGenerator keyGenerator;
    SecretKey secretKey;
    public static byte[] IV = new byte[16];
    public static byte[] sBData;
    SecureRandom random;
    int i=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        mAuth=FirebaseAuth.getInstance();
        button=findViewById(R.id.logOut);

        nameDisplay=findViewById(R.id.name);
        regDisplay = findViewById(R.id.reg);

        database = FirebaseDatabase.getInstance("https://covi-safe-4f99a-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database.getReference().child("Students");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String user = dataSnapshot.child("Email").getValue().toString();
                    if(user.equals(StudentLoginPortal.email.getText().toString())){
                        sName=dataSnapshot.child("Name").getValue().toString();
                        sReg=dataSnapshot.child("Reg").getValue().toString();
                        nameDisplay.setText(sName);
                        regDisplay.setText(sReg);
                        database2 = FirebaseDatabase.getInstance("https://covi-safe-4f99a-default-rtdb.asia-southeast1.firebasedatabase.app");

                        slotRef=database2.getReference().child("Students").child(sReg).child("Slots");
                        slotRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String slots="[";
                                for(DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                                    slots = slots + dataSnapshot1.getValue().toString();
                                }
                                slots = slots;
                                sData = sName+"{"+sReg+slots;
                                try {
                                    keyGenerator = KeyGenerator.getInstance("AES");
                                    keyGenerator.init(256);
                                    secretKey = keyGenerator.generateKey();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                random = new SecureRandom();
                                random.nextBytes(IV);
                                try {
                                    sBData = encrypt(sData.getBytes(),secretKey,IV);
                                    sEData=new String(sBData,StandardCharsets.UTF_8);
                                    qrCodeGenerate(sData);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        i=1;
                        break;
                    }


                }
                if(i==0) {
                    logout();
                    Toast.makeText(QRcode.this, "Login Failed! Email Id not found", Toast.LENGTH_SHORT).show();
                }
            }

            private byte[] encrypt(byte[] plaintext, SecretKey key, byte[] IV) throws Exception {
                Cipher cipher = Cipher.getInstance("AES");
                SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
                IvParameterSpec ivSpec = new IvParameterSpec(IV);
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
                byte[] cipherText = cipher.doFinal(plaintext);
                return cipherText;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
             button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });


    }



    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currUser =mAuth.getCurrentUser();
        if(currUser==null){
            startActivity(new Intent(QRcode.this,StudentLoginPortal.class));
        }

    }
    public void logout() {

        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(QRcode.this,StudentLoginPortal.class));
    }

    public void qrCodeGenerate(String finalData){
        qrCode=findViewById(R.id.qrCode);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try{
            BitMatrix bitMatrix = qrCodeWriter.encode(finalData, BarcodeFormat.QR_CODE,200,200);
            Bitmap bitmap = Bitmap.createBitmap(200,200,Bitmap.Config.RGB_565);
            for(int x=0;x<200;x++)
                for(int y=0;y<200;y++){
                    bitmap.setPixel(x,y,bitMatrix.get(x,y)? Color.BLACK : Color.WHITE);
                }
            qrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


}