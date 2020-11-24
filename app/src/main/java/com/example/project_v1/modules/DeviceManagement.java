package com.example.project_v1.modules;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.example.project_v1.R;
import com.example.project_v1.database.DatabaseHelper;
import com.example.project_v1.dialogs.add_device;
import com.example.project_v1.dialogs.delete_device;
import com.example.project_v1.models.Device;
import com.example.project_v1.models.ViewHolder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class DeviceManagement extends AppCompatActivity {

    Button settingsButton;
    protected ListView devicesListView;                         //main listView
    private List<Device> deviceList;                           //contains all devices form a user
    protected FloatingActionButton addDeviceFloatingButton;     //add a device
    private FirebaseDatabase database;                          //All database data
    private DatabaseReference mDatabase;                        //user's info (name, email, password and devices)
    private static final String USER = "user";
    private  String userID;
    private DatabaseHelper dataBaseHelper;
    private boolean skip_unwanted_onDataChangeListener; //as the name says  "see function set_The_Listeners
    final myAdapter arrayAdapter = new myAdapter();
    protected List<ViewHolder> viewHolderList;
String userUID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBaseHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_device_management);
        deviceList = new ArrayList<>();//be sure the create an array list right at the beginning
        viewHolderList = new ArrayList<>();  // store all the view of the listview

        getSupportActionBar().setTitle("Devices Management");


        //Database related
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference(USER);

        devicesListView = findViewById(R.id.deviceListView);
        addDeviceFloatingButton= findViewById(R.id.floatingActionButton);
        settingsButton=findViewById(R.id.settingsButton);


        //retrieve the user email
        Intent intent = getIntent();
        userID = intent.getStringExtra(("userID"));        //all user shall have one and only one email
        userUID = userID;

        //determine if there are any devices related to the user.
        //        //if yes, show them all
        //        //if not, empty
        loadListView("Nothing",null, null, "normal");
        skip_unwanted_onDataChangeListener = true;
        //  Set the different onclick
        set_The_Listeners();
        accessSettings();
        push_notification();


    }

    private void push_notification() {





        mDatabase.child(userUID).child("Devices").addChildEventListener(new ChildEventListener() {
                                            @Override
                                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                            }

                                            @Override
                                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                                String change =snapshot.getRef().getKey().toString();   //GETS NAME OF DEVICE INFO CHANGED

                                                String check = snapshot.child("Status").getValue().toString();  //CHECKS IF STATUS "ON"


                                                if(check=="ON")
                                                 notification(change);

                                            }

                                            @Override
                                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                                            }

                                            @Override
                                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });


                mDatabase.child(userID).child("Devices").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void notification(String changedInfo){

       PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = Build.VERSION.SDK_INT >= 20 ? pm.isInteractive() : pm.isScreenOn(); // check if screen is on

        if (!isScreenOn) {

        if (!isScreenOn) {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "myApp:notificationLock");
            wl.acquire(3000); //set your time in milliseconds
        }



        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel("n","n", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

    NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"n")

            .setContentTitle("Fire Detection System")
            .setContentText("FDS has detected a FIRE or SMOKE!" +"/n"+"Device:"+changedInfo)
            .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
managerCompat.notify(999,builder.build());


    }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel("n","n", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"n")

                .setContentTitle("Fire Detection System")
                .setContentText("FDS has detected a FIRE or SMOKE!" +"\n"+"Device:"+changedInfo)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(999,builder.build());

    }








































    public void accessSettings(){
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(DeviceManagement.this,Settings.class);
              ;intent.putExtra("userID", userUID);
                startActivity(intent);
            }
        });

    }

    private void set_The_Listeners() {

        // To make it professional, the user name should be defined in the register page
        addDeviceFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context Context = v.getContext();
                add_device dialog = new add_device();
                dialog.show(getSupportFragmentManager(), "InsertDeviceFragment");    //open the dialog
            }
        });

        // Remember, a user can stop the device, but not make it ring on purpose!
        // as the device is on, the arduino will make it ring or not
        // as the device is off, the arduino will do nothing until it's put back "ON"
        // On a Firebase user'status change,
        // loop though all device and change their values in deviceList
        // update list view
        mDatabase.child(userID).child("Devices").addValueEventListener(new ValueEventListener() {
            final myAdapter arrayAdapter = new myAdapter();     //array adaptor man

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!skip_unwanted_onDataChangeListener){
                        int counter = 0; //the order in the deviceList is the same as in the firebase/database
                        //loop through all devices as there is no way to determine which one was changed
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            //get the current status in database
                            String dummy_state = userSnapshot.child("Status").getValue(String.class);
                            deviceList.get(counter).setStatus(dummy_state);
                            ++counter;
                        }
                        devicesListView.setAdapter(arrayAdapter);//write all in list view
                    }
                skip_unwanted_onDataChangeListener = false;     // activate the firebase, onDataChange listener
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {//do nothing
            }
        });
    }

    //determine if there are any devices related to the user.
    //        //if yes, show them all
    //        //if not, empty
    public void loadListView(final String input_Device_Name, final String input_Device_State, final String input_Device_Power, final String action_performed) {
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
                if (action_performed.equals("add_device")) {
                    //now,register the customized user info in "REALTIME DATABASE"
                    Device dummyDevice = new Device(input_Device_Name, input_Device_State, input_Device_Power);
                    deviceList.add(dummyDevice);        //add the device in device list

                    //add the device name - and set the status as a child
                    skip_unwanted_onDataChangeListener = true;// deactivate the firebase, onDataChange listener
                    mDatabase.child(userID).child("numberOfDevices").setValue(Integer.toString(deviceList.size()));
                    mDatabase.child(userID).child("Devices").child(dummyDevice.getName()).child("Power").setValue(dummyDevice.getStatus());
                    mDatabase.child(userID).child("Devices").child(dummyDevice.getName()).child("Status").setValue(dummyDevice.getStatus());
                }
                //Normal operation mode of the DeviceManagementActivity
                else if(action_performed.equals("delete_device")){
                    Device dummyDevice = new Device(input_Device_Name, input_Device_State, input_Device_Power);

                    // loop throught the deviceList to get the targeted device
                    for(int i = 0; i < deviceList.size(); i++){
                        if(deviceList.get(i).getName().equals(input_Device_Name)){
                            deviceList.remove(i);   //remove the device from the list
                            break;
                        }
                    }

                    skip_unwanted_onDataChangeListener = true;// deactivate the firebase, onDataChange listener
                    mDatabase.child(userID).child("Devices").child(input_Device_Name).removeValue();
                    mDatabase.child(userID).child("numberOfDevices").setValue(Integer.toString(deviceList.size()));
                }
                else{
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
                            String dummy_name = userSnapshot.getKey();    //return the name of the device
                            String dummy_power = userSnapshot.child("Power").getValue(String.class);
                            String dummy_state = userSnapshot.child("Status").getValue(String.class);
                            Device dummyDevice = new Device(dummy_name, dummy_state, dummy_power);; //return a device type (name and status)
                            deviceList.add(dummyDevice); //deviceList will be used in the ListView
                            dataBaseHelper.addDevice(dummyDevice);  //add the device to db
                        }
                    }
                }
                skip_unwanted_onDataChangeListener = false;
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
            ViewHolder viewHolder = new ViewHolder(view);   // each "layout" will have their own view and button associated to it
            final Switch sb =  view.findViewById(R.id.deviceState);
            final ToggleButton deviceNameToggleButton= view.findViewById(R.id.deviceName);
            final int i = position;
            skip_unwanted_onDataChangeListener = true;

            // set the onclick listener
            // change the background color if power is on (green) or off (gray)
            // set a tag (position related) to find the corresponding object in ListView
            viewHolder.toggle_Name_Power.setTag(position);
            viewHolder.toggle_Name_Power.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(!skip_unwanted_onDataChangeListener) {
                        if (isChecked) {
                            buttonView.setEnabled(true); // To enable a Switch use following method
                            buttonView.setClickable(true); // To make switch clickable use
                            buttonView.setChecked(false); // set the device ring as false
                            buttonView.setBackgroundColor(Color.argb(100, 126, 251, 161)); // green color
                            update_device_states((Integer) buttonView.getTag(), "OFF", "ON");     // update in firebase as well as in android
                        } else {
                            buttonView.setChecked(false); // set the device ring as false
                            buttonView.setEnabled(false); // To disable a Switch use following method
                            buttonView.setClickable(false); // To make switch not clickable use
                            buttonView.setBackgroundColor(Color.argb(62, 186, 186, 186)); //gray color
                            update_device_states((Integer) buttonView.getTag(), "OFF", "OFF");     // update in firebase as well as in android
                        }
                    }
                    skip_unwanted_onDataChangeListener = false;
                }
            });

            // set the onclick listener
            // update the ring state in android and firebase
            // set a tag (position related) to find the corresponding device object in ListView
            viewHolder.switch_Ring_State.setTag(position);
            viewHolder.switch_Ring_State.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // to prevent unwanted updates
                    if(!skip_unwanted_onDataChangeListener) {
                        //only if the device is not put in "off mode"
                        //if (deviceList.get((Integer)buttonView.getTag()).getPower() == "ON") {
                        //the future value of the switch
                        if (sb.isChecked()) {
                            skip_unwanted_onDataChangeListener = true;
                            update_device_states((Integer) buttonView.getTag(), "ON", "ON");
                        } else {
                            skip_unwanted_onDataChangeListener = true;
                            update_device_states((Integer) buttonView.getTag(), "OFF", "ON");
                        }
                    }
                }
               // }
            });

            // Set the toggle button as clickable -
            // On long click, output  a message box to confirm the delete option
            viewHolder.toggle_Name_Power.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View arg0) {
                    View view = getLayoutInflater().inflate(R.layout.deviceslayoutview, null);  //inflate the customized layout
                    delete_device delete_device = new delete_device( deviceList.get((Integer) arg0.getTag()).return_Device());
                    //transfer the user in to the dialog page
                    delete_device.show(getSupportFragmentManager(), "DeleteDeviceFragment");    //open the dialog
                    return true;
                }
            });

            //do noting if the device list is empty!
            if(!deviceList.isEmpty()) {
                //set the device's name and state individually
                //For switch, see - https://www.tutlane.com/tutorial/android/android-switch-on-off-button-with-examples
                deviceNameToggleButton.setTextColor(Color.argb(100, 0, 0, 0));  //black text color by default
                deviceNameToggleButton.setText(deviceList.get(position).getName());     // default name
                deviceNameToggleButton.setTextOff(deviceList.get(position).getName());  // name when pressed off
                deviceNameToggleButton.setTextOn(deviceList.get(position).getName());   // name when pressed onN

                if(deviceList.get(position).getPower().equals("ON")) {
                    sb.setEnabled(true); // To enable a Switch use following method
                    sb.setClickable(true); // To make switch clickable use
                    deviceNameToggleButton.setBackgroundColor(Color.argb(100, 126, 251, 161)); // green color
                    deviceNameToggleButton.setChecked(true);
                    //set the state of the device
                    if (deviceList.get(position).getStatus().equals("ON")) {
                        sb.setChecked(true);
                    } else {
                        sb.setChecked(false);
                    }
                }
                else{
                    sb.setChecked(false);
                    sb.setEnabled(false); // To enable a Switch use following method
                    sb.setClickable(false); // To make switch clickable use
                    deviceNameToggleButton.setBackgroundColor(Color.argb(62, 186, 186, 186)); //gray color
                    deviceNameToggleButton.setChecked(false);
                }

                viewHolderList.add(viewHolder); // add the view object
            }
            skip_unwanted_onDataChangeListener = false;
            return view;
        }

        //Update the state of the device in the Firebase
        public void update_device_states( int position, String device_state, String device_power){
            deviceList.get(position).setStatus(device_state);   //set the ring state in android
            deviceList.get(position).setPower(device_power);    // set the power state in android
            // set the ring state in firebase
            mDatabase.child(userID).child("Devices").child(deviceList.get(position).getName()).child("Status").setValue(device_state);
            // set the power state in firebase
            mDatabase.child(userID).child("Devices").child(deviceList.get(position).getName()).child("Power").setValue(device_power);
        }

    }
}