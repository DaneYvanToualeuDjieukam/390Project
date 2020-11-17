package com.example.project_v1.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDialogFragment;
import com.example.project_v1.R;
import com.example.project_v1.database.DatabaseHelper;
import com.example.project_v1.models.Device;
import com.example.project_v1.modules.DeviceManagement;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class delete_device extends AppCompatDialogFragment {
    protected TextView deviceNameTextView;
    private static final String USER = "user";
    private String userID;
    private DatabaseHelper dataBaseHelper;
    String input_DeviceName = null;
    private Device device;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private int deviceList_size;

    //Default constructor
    public delete_device() {
        device = null;
        userID = null;
        deviceList_size = 0;
        //do nothing
    }

    public delete_device(Device device, String userID, int deviceList_size) {
        this.device = device;
        this.userID = userID;
        this.deviceList_size = deviceList_size;
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
        View view = inflater.inflate(R.layout.fragment_delete_device, null);

        //fragment_insert_device object links
        deviceNameTextView = view.findViewById(R.id.deviceNameTextView);
        deviceNameTextView.setText(device.getName());

        // firebase setup
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference(USER);

        builder.setView(view)
                .setTitle("CONFIRM DELETE")
                .setCancelable(false)
               // .setMessage("Remove the Device: " + device.getName())
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //nothing happens
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //remove device from firebase
                        ((DeviceManagement) getActivity()).loadListView(device.getName(), device.getStatus(), device.getPower(), "delete_device");
                    }
                });
        return builder.create();
    }
}