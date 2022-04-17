package com.example.covi_safe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class FacultyPortal extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private CardView scanAttendance,cAttendance;
    FirebaseDatabase database,database2;
    DatabaseReference myRef,slotRef,attRef;
    public static String fId,selectedSlot,sData;
    Button button;
    int i=0,e1,e2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_portal);
        scanAttendance=findViewById(R.id.card1);
        cAttendance=findViewById(R.id.card2);
        mAuth=FirebaseAuth.getInstance();
        button=findViewById(R.id.logOut);
        database = FirebaseDatabase.getInstance("https://covi-safe-4f99a-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database.getReference().child("Faculties");



        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    String user= dataSnapshot.child("Email").getValue().toString();
                    if(user.equals(FacultyLoginPortal.email.getText().toString())){
                        fId=dataSnapshot.child("Reg").getValue().toString();
                        database2 = FirebaseDatabase.getInstance("https://covi-safe-4f99a-default-rtdb.asia-southeast1.firebasedatabase.app");
                        i=1;
                        slotRef=database2.getReference().child("Faculties").child(fId).child("Slots");
                        slotRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                AlertDialog.Builder mBuilder=new AlertDialog.Builder(FacultyPortal.this);
                                View mView=getLayoutInflater().inflate(R.layout.slot_select,null);
                                mBuilder.setTitle("Select Slot");
                                List<String> spinnerArray=new ArrayList<String>();
                                for(DataSnapshot dataSnapshot1:snapshot.getChildren()){
                                    spinnerArray.add(dataSnapshot1.getValue().toString());
                                }

                                ArrayAdapter<String> adapter=new ArrayAdapter<String>(FacultyPortal.this, android.R.layout.simple_spinner_item,spinnerArray);
                                Spinner mSpinner=(Spinner) mView.findViewById(R.id.spinner);
                                mSpinner.setAdapter(adapter);
                                mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        selectedSlot=mSpinner.getSelectedItem().toString();
                                    }
                                });
                                mBuilder.setView(mView);
                                AlertDialog dialog=mBuilder.create();
                                dialog.show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }
                if(i==0){
                    Toast.makeText(FacultyPortal.this,"Invalid EmailID",Toast.LENGTH_SHORT).show();
                    logout();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        cAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(FacultyPortal.this,Attendance.class);
                startActivity(intent);
            }
        });

        scanAttendance.setOnClickListener((View.OnClickListener) this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });


    }

    @Override
    public void onClick(View view) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan QR code");
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // if the intentResult is not null we'll set
                // the content and format of scan message
                try {
                    String sName="";
                    String sReg="";
                    String[] sSlots=new String[7];
                    sData=intentResult.getContents();
                    e1=sData.indexOf("{");
                    for(int i=0;i<e1;i++){
                        sName=sName+sData.charAt(i);
                    }
                    e2=sData.indexOf("[");
                    for(int i=e1+1;i<e2;i++){
                        sReg=sReg+sData.charAt(i);
                    }
                    int j=0;
                    String curSlot="";
                    for(int i=e2+1;i<sData.length();i=i+2){
                        curSlot=curSlot+sData.charAt(i);
                        curSlot=curSlot+sData.charAt(i+1);
                        sSlots[j]=curSlot;
                        curSlot="";
                        j+=1;
                    }
                    FirebaseDatabase database3;
                    Task<Void> attendanceRef;
                    LocalDate localDate=LocalDate.now();
                    boolean x= Arrays.asList(sSlots).contains(selectedSlot);
                    if(x){
                        database3=FirebaseDatabase.getInstance("https://covi-safe-4f99a-default-rtdb.asia-southeast1.firebasedatabase.app");
                        attendanceRef=database3.getReference().child("Faculties").child(fId).child(selectedSlot).child(localDate.toString()).child(sReg).setValue(sReg);
                    }
                    else{
                        Toast.makeText(getBaseContext(),"Student not present in this class",Toast.LENGTH_SHORT).show();
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }

                }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currUser =mAuth.getCurrentUser();
        if(currUser==null){
            startActivity(new Intent(FacultyPortal.this,FacultyLoginPortal.class));
        }

    }

    public String decrypt(@NonNull String data) {
        String key="";
        String msg="";
        String IV="";
        int e1,e2;
        e1=data.indexOf("12");
        e2=data.indexOf("21");
        for(int i=0;i<e1;i++){
            msg=msg+data.charAt(i);
        }
        for(int i=e1+2;i<e2;i++){
            key=key+data.charAt(i);
        }
        for(int i=e2+2;i<data.length();i++){
            IV=IV+data.charAt(i);
        }
        Toast.makeText(getBaseContext(),msg+key+IV,Toast.LENGTH_SHORT).show();
        byte[] decodedKey = Base64.getDecoder().decode(key);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        byte[] decodedIV=IV.getBytes(StandardCharsets.UTF_8);
        byte[] decodedMsg=msg.getBytes(StandardCharsets.UTF_8);
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec keySpec = new SecretKeySpec(originalKey.getEncoded(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(decodedIV);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decryptedText = cipher.doFinal(decodedMsg);
            return new String(decryptedText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void logout() {

        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(FacultyPortal.this,FacultyLoginPortal.class));
    }
}
