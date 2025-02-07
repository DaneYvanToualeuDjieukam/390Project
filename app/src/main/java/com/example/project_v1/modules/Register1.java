package com.example.project_v1.modules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.project_v1.R;
import com.example.project_v1.database.MyService;
import com.example.project_v1.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register1 extends AppCompatActivity {

    protected EditText emailEditText;
    protected EditText passwordEditText;
    protected EditText confirmPasswordEditText;
    protected EditText usernameEditText;
    protected Button registerButton;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private static final String USER = "user";
    private static final String TAG = "SignUp";
    private String user_KeyID = null;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register1);
        getSupportActionBar().setTitle(TAG);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();

        //get all editTexts from the layout
        emailEditText = findViewById(R.id.emailUserText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        usernameEditText = findViewById(R.id.userNameEditText);
        registerButton= findViewById(R.id.registerButtonnn);

        //set firebase datalink
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference(USER);
        mAuth = FirebaseAuth.getInstance();

        //just a lil fancy
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);   //make the progress bar invisible!

        //register button onclick listener
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email =emailEditText.getText().toString();
                final String password= passwordEditText.getText().toString();
                final String username = usernameEditText.getText().toString();

                //able to register the user in Firebase
                registerUser(email,password,username);
            }
        });

    }

    //add the user info in database
    private void registerUser(final String email,final String password,final String username) {
        if (isUserWellDefined()) {   //iff inputs were well defined
            progressBar.setVisibility(View.VISIBLE);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // Sign in success, update UI with the signed-in user's information
                            if (task.isSuccessful()) {

                                Toast.makeText(getApplicationContext(),"Sign Up Successful!",Toast.LENGTH_LONG).show();
                                FirebaseUser user= task.getResult().getUser(); //From Authentication (identifier,providers,created, SignIn,user uid)
                                user_KeyID = user.getUid();   //the user id was way too long for arduinoo

                                user=mAuth.getCurrentUser();
                              user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Register1.this, "Verification Email Sent", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Email not sent" + e.getMessage());

                                    }
                                });


                                //now,register the customized user info in "REALTIME DATABASE"
                                User thisUser = new User(email,password,username);
                                mDatabase.child(user_KeyID).setValue(thisUser);//add the username - title will be  a unique Key


                                //pass the userID to the deviceManagement Page
                                Intent intent  = new Intent(Register1.this,MainActivity.class);
                                //intent.putExtra("userID", user_KeyID);
                                intent.putExtra("userID", user_KeyID);
                                startActivity(intent);

                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(getApplicationContext(),"Email Already Exist",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            progressBar.setVisibility(View.GONE);
        }
    }

    //determines if the username - email and password are valid
    private boolean isUserWellDefined() {
        boolean allGood = true;  //default value
        final String name = usernameEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        final String pass = passwordEditText.getText().toString().trim();
        final String retypepass = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(retypepass) || !retypepass.equals(pass)) {
            confirmPasswordEditText.setError("Please Re-Type Password");
            //Toast.makeText(getApplicationContext(),"Please Enter Password",Toast.LENGTH_LONG).show();
            confirmPasswordEditText.requestFocus();
            allGood = false;;
        }
        if(TextUtils.isEmpty(pass)) {
            passwordEditText.setError("Please Enter Password");
            //Toast.makeText(getApplicationContext(),"Please Enter Password",Toast.LENGTH_LONG).show();
            passwordEditText.requestFocus();
            allGood = false;;
        }
        //Password should have a minimum of 6 characters for the "create user" to work
        if(pass.length()<6) {
            passwordEditText.setError("Min Password Length of 6 Characters");
            //Toast.makeText(getApplicationContext(),"Please Enter Password",Toast.LENGTH_LONG).show();
            passwordEditText.requestFocus();
            allGood = false;;
        }
        //if email is empty or doesn't match the typical email format
        if (TextUtils.isEmpty(email)) { //  || !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            emailEditText.setError("Please Enter Email");
            //Toast.makeText(getApplicationContext(),"Please Enter Email",Toast.LENGTH_LONG).show();
            emailEditText.requestFocus();
            allGood = false;;
        }
        if (TextUtils.isEmpty(name)) {
            usernameEditText.setError("Please Enter Name");
            //Toast.makeText(getApplicationContext(),"Please Enter Name",Toast.LENGTH_LONG).show();
            usernameEditText.requestFocus();
            allGood = false;
        }
        return allGood;
    }

    //add the user's to the database
    //each "main" user should have an unique email
    private void updateUI (FirebaseUser currentUser, String email){
        mDatabase.child(currentUser.getUid()).child("Username").setValue(email);//add the username
    }

    public void startService(){
        Intent serviceIntent = new Intent(this, MyService.class);
        startService(serviceIntent);
    }
}
