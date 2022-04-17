package com.example.covi_safe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Selection extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
    }

    public void facultyClick(View view) {
        Intent intent = new Intent(Selection.this,FacultyLoginPortal.class);
        startActivity(intent);
    }

    public void studentClick(View view) {
        Intent intent= new Intent(Selection.this,StudentLoginPortal.class);
        startActivity(intent);
    }
}