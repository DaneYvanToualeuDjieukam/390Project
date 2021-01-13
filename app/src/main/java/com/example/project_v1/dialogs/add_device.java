package com.example.project_v1.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import com.example.project_v1.R;
import com.example.project_v1.database.dbHelper_AllDevices;
import com.example.project_v1.database.dbHelper_UserDevices;
import com.example.project_v1.modules.DeviceManagement;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class add_device extends AppCompatDialogFragment {
    protected EditText deviceName;
    protected EditText devicePassword;
    protected EditText deviceID;
    private static final String USER = "user";
    private String userID;
    private String userEmail;
    private dbHelper_UserDevices dataBaseHelper;
    private dbHelper_AllDevices dbHelper_AllDevices;
    private FirebaseDatabase database;                          //All database data
    private DatabaseReference mDatabase;//user's info (name, email, password and devices)
    private FirebaseAuth Fauth;
    private Context context;
    Activity activity ;
    String input_DeviceName = null;
    String input_DevicePassword=null;
    String input_DeviceID=null;

    public add_device(Context context) {
        this.context = context;
    }

    //Default constructor
    public add_device() {
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //set the Layout object size
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_add_device, null);

        dataBaseHelper = new dbHelper_UserDevices(getActivity());   //db link to devices related to the user
        dbHelper_AllDevices = new dbHelper_AllDevices(getActivity());//access all devices related to the company

        //fragment_insert_device object links
        deviceName = view.findViewById(R.id.deviceNameEditText);
        devicePassword = view.findViewById(R.id.devicePasswordEditText);
        deviceID= view.findViewById(R.id.deviceIDEditText);

        Fauth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("Devices");

        activity =((DeviceManagement) getActivity());   //other activity to be called

        builder.setView(view)
                .setTitle("ADD DEVICES")
                .setCancelable(false)
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        input_DeviceName = deviceName.getText().toString();
                        input_DevicePassword = devicePassword.getText().toString();
                        input_DeviceID = deviceID.getText().toString();

                        if ((!input_DeviceName.equals(null) || !input_DeviceName.isEmpty() || !(input_DeviceName.trim().length() <= 0))) {

                            //is the device's name already under the user's database?
                            if (dataBaseHelper.is_The_Device_Name_Available(input_DeviceName)) {

                                //define if the device exist
                                mDatabase.child(input_DeviceID).child("Trigger").setValue("Try")
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Write was successful! Therefore, the device exist (device id is written perfectly
                                                password_id_match_and_device_available();   //define if the device is currently connected to a user, if not, then proceed
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Write failed, then the device does not exist
                                                Toast.makeText(context, "Enter a valid device name", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                            else{
                                Toast.makeText(context, "The device name is not available", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(context, "Enter a valid device name", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return builder.create();
    }

    //define if the device is currently connected to a user, if not, then proceed
    private void password_id_match_and_device_available(){

        //In case you want to check the single value by adding addListenerForSingleValueEvent()
        ValueEventListener changeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { //Allows user to add new device if she signs out and back in (only 1)

                    // get the current status in database
                    String dummy_DeviceId = dataSnapshot.child("DeviceId").getValue(String.class);
                    String dummy_DevicePass = dataSnapshot.child("Password").getValue(String.class);
                    String dummy_UserIdPass = dataSnapshot.child("UserID").getValue(String.class);

                    if(dummy_DeviceId.equals(input_DeviceID) && dummy_DevicePass.equals(input_DevicePassword)){
                        if(dummy_UserIdPass.equals("NEW")){
                            //remove the OnDataChangeListener
                            mDatabase.child(input_DeviceID).removeEventListener(this);
                            //set the trigger to access the targeted device's information
                            mDatabase.child(input_DeviceID).child("Trigger").setValue("");
                            //add the device under the user
                            ((DeviceManagement) activity).loadListView(input_DeviceID, input_DeviceName, "OFF", "ON", "add_device");
                        }
                        else{
                            Toast.makeText(context, "Device already in use", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(context,"Invalid Device ID or Password",Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(context,"Invalid Device ID or Password",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {//do nothing
                Toast.makeText(context, "Invalid Device ID", Toast.LENGTH_SHORT).show();
            }
        };
        mDatabase.child(input_DeviceID).addValueEventListener(changeListener);

        //set the trigger to access the targeted device's information
        mDatabase.child(input_DeviceID).child("Trigger").setValue("TRIG");
    }
}