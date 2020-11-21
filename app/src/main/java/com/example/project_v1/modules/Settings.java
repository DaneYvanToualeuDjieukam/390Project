package com.example.project_v1.modules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project_v1.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Settings extends AppCompatActivity {

    private FirebaseDatabase database;                          //All database data
    private DatabaseReference mDatabase;//user's info (name, email, password and devices)
    private FirebaseAuth mAuth;

    private static final String USER = "user";
    private  String userID;
    private String UID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Database related
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference(USER);
        mAuth = FirebaseAuth.getInstance();


        Intent intent = getIntent();
        userID = intent.getStringExtra(("userID"));
        UID=userID;


        final FirebaseUser userr = mAuth.getCurrentUser();

        if(userr==null){Toast.makeText(getApplicationContext(),"USER NULL", Toast.LENGTH_LONG).show();  }

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

            final EditText resetPassword = new EditText(view.getContext());

            final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
            passwordResetDialog.setTitle("Reset Password?");
            passwordResetDialog.setMessage("Enter Your Email To Receive Reset Link");
            passwordResetDialog.setView(resetPassword);

            passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialogInterface, int i) {
String newPassword= resetPassword.getText().toString();
userr.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
    @Override
    public void onSuccess(Void aVoid) {
        Toast.makeText(Settings.this,"Password Reset Sucessfully",Toast.LENGTH_LONG).show();

    }
}).addOnFailureListener(new OnFailureListener() {
    @Override
    public void onFailure(@NonNull Exception e) {
        Toast.makeText(Settings.this,"Password Reset Failed",Toast.LENGTH_LONG).show();

    }
});
                }
            });


                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
passwordResetDialog.create().show();

            }







            });
        }

    }
