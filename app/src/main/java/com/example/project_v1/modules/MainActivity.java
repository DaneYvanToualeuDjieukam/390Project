package com.example.project_v1.modules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.project_v1.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    protected EditText emailTextView;
    protected EditText passwordTextView;
    protected Button signinButton;
    protected Button registerButton;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get all button and text related to the mainActivity layout
        emailTextView=findViewById(R.id.emailTextView);
        passwordTextView=findViewById(R.id.passwordTextView);
        signinButton=findViewById(R.id.signinButton);
        registerButton=findViewById(R.id.registerButtonnn);
        fAuth= FirebaseAuth.getInstance();  //create a firebase weblink

        //go to register page
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });


        //Sign in page - define if the user's account is active
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String userEmail = emailTextView.getText().toString();
                final String password = passwordTextView.getText().toString();

                //if none of the emailTextView and passwordTextView are empty
                if (!(userEmail.equals(null) || userEmail.isEmpty() || userEmail.trim().length() <= 0) && !(password.equals(null) || password.isEmpty() || password.trim().length() <= 0)){
                    fAuth.signInWithEmailAndPassword(userEmail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Signed in", Toast.LENGTH_LONG).show();
                                //can only have one type of value per extra
                                //don't use the @, as users can have multiple emails in gmail/yahoo/hotmail,etc.
                                Intent intent = new Intent(getApplicationContext(), DeviceManagement.class);
                                FirebaseUser user = fAuth.getCurrentUser();
                                String dummyUID = fAuth.getUid().toString();
                                intent.putExtra("userID", dummyUID);       //pass the userID
                                startActivity(intent);

                            }
                            else {Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();}
                        }
                    });
                }
                else {Toast.makeText(MainActivity.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();}
            }

        });

    }
}