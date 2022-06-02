package com.example.covi_safe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Attendance extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseDatabase database1;
    DatabaseReference myRef;
    Spinner spinner,spinner2,spinner4;
    String selectedSlot,selectedDate,registrationNo;
    EditText editText;
    TextView textView;
    Button button,button2,button3,button4;
    int c;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        spinner=findViewById(R.id.slotSelect);
        editText=findViewById(R.id.registrationNo);
        button=findViewById(R.id.button);
        button2=findViewById(R.id.button2);
        button3=findViewById(R.id.button3);
        button4=findViewById(R.id.button4);
        spinner2=findViewById(R.id.dateSelect);
        spinner4=findViewById(R.id.spinner4);
        textView=findViewById(R.id.textView);
        String fid=FacultyPortal.fId;
        mAuth=FirebaseAuth.getInstance();
        database1 = FirebaseDatabase.getInstance("https://covi-safe-4f99a-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database1.getReference().child("Faculties").child(fid).child("Slots");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> spinnerArray=new ArrayList<String>();
                for(DataSnapshot dataSnapshot1:snapshot.getChildren()){
                    spinnerArray.add(dataSnapshot1.getValue().toString());
                }
                ArrayAdapter<String> adapter=new ArrayAdapter<String>(Attendance.this, android.R.layout.simple_spinner_item,spinnerArray);
                spinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedSlot=spinner.getSelectedItem().toString();
                editText.setVisibility(view.VISIBLE);
                spinner2.setVisibility(view.VISIBLE);
                button2.setVisibility(view.VISIBLE);
                textView.setText("Selected Slot is " + selectedSlot);
                spinner.setVisibility(view.INVISIBLE);
                button.setVisibility(view.INVISIBLE);
                button3.setVisibility(view.INVISIBLE);
                FirstStep(selectedSlot,fid,spinner2);
                            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDate=spinner2.getSelectedItem().toString();
                registrationNo=editText.getText().toString();
                Modify(selectedDate,registrationNo,selectedSlot,fid);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedSlot=spinner.getSelectedItem().toString();
                button4.setVisibility(view.VISIBLE);
                spinner4.setVisibility(view.VISIBLE);
                textView.setText("Selected Slot is " + selectedSlot);
                spinner.setVisibility(view.INVISIBLE);
                button.setVisibility(view.INVISIBLE);
                button3.setVisibility(view.INVISIBLE);
                CheckButton(selectedSlot,fid,spinner4);
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDate=spinner4.getSelectedItem().toString();
                Check(selectedDate,selectedSlot,fid);
            }
        });

    }
    public void FirstStep(String selectedSlot,String fid,Spinner spinner2){
        mAuth=FirebaseAuth.getInstance();
        database1 = FirebaseDatabase.getInstance("https://covi-safe-4f99a-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database1.getReference().child("Faculties").child(fid).child(selectedSlot);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> spinnerArray=new ArrayList<String>();
                for(DataSnapshot dataSnapshot1:snapshot.getChildren()){
                    spinnerArray.add(dataSnapshot1.getKey());
                }
                ArrayAdapter<String> adapter=new ArrayAdapter<String>(Attendance.this, android.R.layout.simple_spinner_item,spinnerArray);
                spinner2.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void Modify(String selectedDate,String registrationNo,String selectedSlot,String fid){
        c=0;
        mAuth=FirebaseAuth.getInstance();
        database1 = FirebaseDatabase.getInstance("https://covi-safe-4f99a-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database1.getReference().child("Faculties").child(fid).child(selectedSlot).child(selectedDate);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    if(dataSnapshot.getValue().toString().equals(registrationNo)){
                        c=1;
                        AlertDialog.Builder builder=new AlertDialog.Builder(Attendance.this);
                        builder.setTitle("Covi-Safe");
                        builder.setMessage("User is already present. Do you want to delete the record for the same?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                myRef.child(registrationNo).removeValue();
                                Toast.makeText(Attendance.this,"User deleted Successfully",Toast.LENGTH_SHORT).show();
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        break;
                    }
                }
                if(c==0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(Attendance.this);
                    builder.setTitle("Covi-Safe");
                    builder.setMessage("User is not present. Do you want to add the current student?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            myRef.child(registrationNo).setValue(registrationNo);
                            Toast.makeText(Attendance.this, "User added Successfully!!!!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog=builder.create();
                    alertDialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void CheckButton(String selectedSlot,String fid,Spinner spinner4) {
        mAuth=FirebaseAuth.getInstance();
        database1 = FirebaseDatabase.getInstance("https://covi-safe-4f99a-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database1.getReference().child("Faculties").child(fid).child(selectedSlot);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> spinnerArray=new ArrayList<String>();
                for(DataSnapshot dataSnapshot1:snapshot.getChildren()){
                    spinnerArray.add(dataSnapshot1.getKey());
                }
                ArrayAdapter<String> adapter=new ArrayAdapter<String>(Attendance.this, android.R.layout.simple_spinner_item,spinnerArray);
                spinner4.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void Check(String selectedDate,String selectedSlot,String fid){

        mAuth=FirebaseAuth.getInstance();
        database1 = FirebaseDatabase.getInstance("https://covi-safe-4f99a-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database1.getReference().child("Faculties").child(fid).child(selectedSlot).child(selectedDate);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Attendance.this);
                builder.setTitle("Attendance for date: "+ selectedDate);
                View rowList = getLayoutInflater().inflate(R.layout.row, null);
                listView = rowList.findViewById(R.id.listView);
                List<String> spinnerArray=new ArrayList<String>();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    spinnerArray.add(dataSnapshot.getValue().toString());
                }
                ArrayAdapter<String> adapter=new ArrayAdapter<String>(Attendance.this, android.R.layout.simple_spinner_item,spinnerArray);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                builder.setView(rowList);
                builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}
