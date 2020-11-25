package com.example.project_v1.modules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project_v1.R;
import com.example.project_v1.database.MyService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Settings extends AppCompatActivity {

    private FirebaseDatabase database;                          //All database data
    private DatabaseReference mDatabase;//user's info (name, email, password and devices)
    private FirebaseAuth mAuth;
    private FirebaseUser userrr;

    private static final String USER = "user";

    private static final String TAG = "CHANGE EMAIL";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Database related
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference(USER);
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        final String userUID = intent.getStringExtra(("userID"));


        final FirebaseUser userr = mAuth.getCurrentUser();

        if(userr==null){Toast.makeText(getApplicationContext(),"USER NULL", Toast.LENGTH_LONG).show();  }


        final TextView emailAddress;
        final TextView username;
        Button resetEmailButton;
        Button resetPasswordButton;
        Button signOutButton;



        emailAddress = findViewById(R.id.userUserTextview);
        username = findViewById(R.id.emailUserText);
        resetEmailButton= findViewById(R.id.resetEmailButton);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        signOutButton = findViewById(R.id.signOutButton);


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("user").child(userUID).child("username");

reference.addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        username.setText(snapshot.getValue().toString());
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
});

        DatabaseReference referencee = FirebaseDatabase.getInstance().getReference().child("user").child(userUID).child("email");

        referencee.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                emailAddress.setText(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });








        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            final EditText resetPassword = new EditText(view.getContext());

            final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
            passwordResetDialog.setTitle("Reset Password?");
            passwordResetDialog.setMessage("Enter New Password");
            passwordResetDialog.setView(resetPassword);

            passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialogInterface, int i) {
final String newPassword= resetPassword.getText().toString();
userr.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
    @Override
    public void onSuccess(Void aVoid) {
        String idd=userUID;

        FirebaseDatabase.getInstance().getReference().child("user").child(idd).child("password").setValue(newPassword);
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



        resetEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                final EditText resetEmail = new EditText(view.getContext());

                final AlertDialog.Builder emailResetDialog = new AlertDialog.Builder(view.getContext());
                emailResetDialog.setTitle("Reset Email?");
                emailResetDialog.setMessage("Enter New Email");
                emailResetDialog.setView(resetEmail);

                emailResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String newEmail= resetEmail.getText().toString();
                        userr.updateEmail(newEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                String idd=userUID;

                                FirebaseDatabase.getInstance().getReference().child("user").child(idd).child("email").setValue(newEmail);
                                Toast.makeText(Settings.this,"Email Reset Sucessfully",Toast.LENGTH_LONG).show();


                                userrr=mAuth.getCurrentUser();
                                Task<Void> verification_email_sent = userrr.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Settings.this, "Verification Email Sent", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Email not sent" + e.getMessage());

                                    }
                                });




                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Settings.this,"Email Reset Failed",Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                });


                emailResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                emailResetDialog.create().show();

            }







        });



        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stopService();

                Intent signout = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(signout);

            }
        });


        }



        public void stopService(){
        Intent serviceIntent = new Intent(this, MyService.class);
        stopService(serviceIntent);
        }



}
