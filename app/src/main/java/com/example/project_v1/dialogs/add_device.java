package com.example.project_v1.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDialogFragment;
import com.example.project_v1.R;
import com.example.project_v1.database.DatabaseHelper;
import com.example.project_v1.modules.DeviceManagement;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class add_device extends AppCompatDialogFragment {
    protected EditText deviceName;
    private static final String USER = "user";
    private String userID;
    private String userEmail;
    private DatabaseHelper dataBaseHelper;
    String input_DeviceName = null;

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

        //fragment_insert_device object links
        deviceName = view.findViewById(R.id.deviceNameEditText);

        builder.setView(view)
                .setTitle("ADD DEVICES")
                .setCancelable(false)
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //nothing happens
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        input_DeviceName = deviceName.getText().toString();
                        add_Device_If_Applicable(input_DeviceName);
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
                ((DeviceManagement) getActivity()).loadListView(device_name, "OFF", "YES", "add_device");
            }
            else {
                Toast.makeText(getActivity(), "The device name is not available", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getActivity(), "Enter a valid name", Toast.LENGTH_SHORT).show();
        }
    }
}