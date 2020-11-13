package com.example.project_v1.modules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import com.example.project_v1.R;
import com.example.project_v1.database.DatabaseHelper;
import com.example.project_v1.dialogs.add_device;
import com.example.project_v1.models.Device;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class DeviceManagement extends AppCompatActivity {

    protected ListView devicesListView;                         //main listView
    private List<Device> deviceList;                           //contains all devices form a user
    protected FloatingActionButton addDeviceFloatingButton;     //add a device
    private FirebaseDatabase database;                          //All database data
    private DatabaseReference mDatabase;                        //user's info (name, email, password and devices)
    private static final String USER = "user";
    private  String userID;
    private DatabaseHelper dataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBaseHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_device_management);
        deviceList = new ArrayList<>();     //be sure the create an array list right at the beginning

        getSupportActionBar().setTitle("Devices Management");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Database related
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference(USER);

        devicesListView = findViewById(R.id.deviceListView);
        addDeviceFloatingButton= findViewById(R.id.floatingActionButton);

        //retrieve the user email
        Intent intent = getIntent();
        userID = intent.getStringExtra(("userID"));        //all user shall have one and only one email

        //determine if there are any devices related to the user.
        //        //if yes, show them all
        //        //if not, empty
        loadListView("Nothing",null);

        //To make it professional, the user name should be defined in the register page
        //as well as
        addDeviceFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context Context = v.getContext();
                add_device dialog = new add_device();
                //transfer the user in to the dialog page
                Bundle args = new Bundle();
                args.putString("userID", userID);
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "InsetDeviceFragment");    //open the dialog
            }
        });
    }

    //determine if there are any devices related to the user.
    //        //if yes, show them all
    //        //if not, empty
    public void loadListView(final String input_Device_Name,final String input_Device_State) {
        final myAdapter arrayAdapter = new myAdapter();
        final DatabaseReference user_data = FirebaseDatabase.getInstance().getReference(USER).child(userID);//user's data -  devices included

        //addValueEventListener() keep listening to query or database reference it is attached to.
        //addListenerForSingleValueEvent() executes onDataChange method immediately and
        //after executing that method once, it stops listening to the reference location it is attached to.
        //see - https://stackoverflow.com/questions/41579000/difference-between-addvalueeventlistener-and-addlistenerforsinglevalueevent
        user_data.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int numberOfDevices = 0;//as the name says
                //as the "onDataChange" methods is asynchronous, it was too risky to create another one in "add_device"
                //so to keep it to a minimum, the added device values are passed here and added to the firebase
                //added to the firebase as we fill up the list view
                //If adding a device, do not empty the deviceList,
                //as one cannot read data from firebase and write at the same time (Data not directly update)
                if (!input_Device_Name.equals("Nothing")) {
                    //now,register the customized user info in "REALTIME DATABASE"
                    Device dummyDevice = new Device(input_Device_Name, input_Device_State);
                    deviceList.add(dummyDevice);        //add the device in device list
                    mDatabase.child(userID).child("Devices").child("Device " + Integer.toString(deviceList.size())).setValue(dummyDevice);//add the device - name and status
                }
                //Normal operation mode of the DeviceManagementActivity
                else {
                    //delete all devices in list if not empty
                    //add the course to db
                    if (!deviceList.isEmpty()) {
                        deviceList.clear();
                    }

                    for (DataSnapshot userSnapshot : dataSnapshot.child("Devices").getChildren()) {
                        ++numberOfDevices;   //add one for every devices
                    }

                    if (numberOfDevices > 0) {
                        //for each data in related to each devices
                        for (DataSnapshot userSnapshot : dataSnapshot.child("Devices").getChildren()) {
                            //get the current values in database
                            //on should write
                            Device dummyDevice = new Device();
                            dummyDevice = userSnapshot.getValue(Device.class); //return a device type (name and status)
                            deviceList.add(dummyDevice); //deviceList will be used in the ListView
                            dataBaseHelper.addDevice(dummyDevice);  //add the device to db
                        }
                    }
                }
                //write all in list view
                devicesListView.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //do nothing
            }
        });
    }

    //an adapter is a bridge between UI component and data source that helps us to fill
    //data in the UI component. It holds the data and send the data to adapter view then
    // view can takes the data from the adapter view and shows the data on different
    // views like as list view, grid view, spinner etc.
    //https://abhiandroid.com/ui/baseadapter-tutorial-example.html
    class myAdapter extends BaseAdapter {
        @Override
        ////returns the total number of courses to be displayed in a list
        public int getCount() {
            if(deviceList.isEmpty()){
                return 0;
            }
            return deviceList.size();
        }

        @Override
        //This function is used to Get the data item associated with the specified position
        //in the data set to obtain the corresponding data of the specific location in the collection of data items.
        public Object getItem(int position) {
            return null;
        }

        @Override
        //As for the getItemId (int position), it returns the corresponding to the position item
        //ID. The function returns a long value of item position to the adapter.
        public long getItemId(int position) {
            return 0;
        }

        @Override
        //This function is automatically called when the list item view is ready to be displayed or about to be displayed.
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.deviceslayoutview, null);  //inflate the customized layout!
            final Switch sb =  view.findViewById(R.id.deviceState);

            final int i = position;

            //set the onclick listener
            sb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    //the future value of the switch
                    if(sb.isChecked()){
                        deviceList.get(i).setStatus("ON");
                        update_device_state(i,deviceList.get(i).getName(),"ON");
                    }
                    else{
                        deviceList.get(i).setStatus("OFF");
                        update_device_state(i,deviceList.get(i).getName(),"OFF");
                    }
                }
            });

            //do noting if the device list is empty!
            if(!deviceList.isEmpty()) {
                //set the device's name and state individually
                //For switch, see - https://www.tutlane.com/tutorial/android/android-switch-on-off-button-with-examples
                TextView deviceNameTextView = view.findViewById(R.id.deviceName);
                deviceNameTextView.setText(deviceList.get(position).getName());

                //set the state of the device
                if(deviceList.get(position).getStatus().equals("ON")){
                    sb.setChecked(true);
                }else{
                    sb.setChecked(false);
                }

            }
            return view;
        }
    }

    //Update the state of the device in the Firebase
    public void update_device_state(final int device_position,final String device_name, final String device_state){
        Device device = new Device(device_name,device_state);
        mDatabase.child(userID).child("Devices").child("Device " + Integer.toString(device_position + 1)).setValue(device);
    }
}