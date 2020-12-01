package com.example.project_v1.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.project_v1.R;
import com.example.project_v1.database.DatabaseHelper;
import com.example.project_v1.modules.DeviceManagement;
import com.example.project_v1.modules.DeviceManagement;
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
    private DatabaseHelper dataBaseHelper;

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

     activity =((DeviceManagement) getActivity());
        //set the Layout object size
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_add_device, null);

        //fragment_insert_device object links
        deviceName = view.findViewById(R.id.deviceNameEditText);
        devicePassword = view.findViewById(R.id.devicePasswordEditText);
        deviceID= view.findViewById(R.id.deviceIDEditText);

        Fauth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("Devices");

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
                        input_DevicePassword=devicePassword.getText().toString();
                        input_DeviceID=deviceID.getText().toString();



boolean k=false;
DatabaseReference ref =mDatabase;
ref.addListenerForSingleValueEvent(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        if(snapshot.hasChild(input_DeviceID)){




            if(snapshot.child(input_DeviceID).child("EditedName").getValue().equals("NEW")){
    mDatabase.child(input_DeviceID).child("EditedName").setValue(input_DeviceName);
    mDatabase.child(input_DeviceID).child("Password").setValue(input_DevicePassword);
    mDatabase.child(input_DeviceID).child("UserID").setValue(Fauth.getUid());
                mDatabase.child(input_DeviceID).child("Time").setValue("0");

                ((DeviceManagement) activity).startRepeating();


                add_Device_If_Applicable(input_DeviceName);

}else{Toast.makeText(context,"Device ALREADY IN Use",Toast.LENGTH_SHORT).show(); }


        }else{
            Toast.makeText(context,"Device Not Available",Toast.LENGTH_SHORT).show();}
    }





    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
});








                    }
                });
        return builder.create();
    }





    //is the device's name already in the database?
    private void add_Device_If_Applicable(final String device_name) {
        dataBaseHelper = new DatabaseHelper(getActivity());   //db link

        //only when it's truly empty
        //see - https://stackoverflow.com/questions/27086808/android-check-null-or-empty-string-in-android
        if (!(device_name.equals(null) || device_name.isEmpty() || device_name.trim().length() <= 0)) {
            //if the device name is available
            if (dataBaseHelper.is_The_Device_Name_Available(device_name)) {
                //Send the device to Load view to be added


                ((DeviceManagement) activity).loadListView(device_name, "OFF", "YES", "add_device");
            }
            else {
                Toast.makeText(context, "The device name is not available", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(context, "Enter a valid name", Toast.LENGTH_SHORT).show();
        }
    }






}