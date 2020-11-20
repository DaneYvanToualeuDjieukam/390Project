package com.example.project_v1.modules;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.project_v1.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Settings extends AppCompatActivity {

    private FirebaseDatabase database;                          //All database data
    private DatabaseReference mDatabase;                        //user's info (name, email, password and devices)
    private static final String USER = "user";
    private  String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Database related
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference(USER);

        Intent intent = getIntent();
        userID = intent.getStringExtra(("userID"));


        Button resetPasswordButton;
        EditText emailEditText;

        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        emailEditText = findViewById(R.id.emailEditText);


        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        emailEditText.setText(userID);


        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {





            }
        });

    }
}