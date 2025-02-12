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
import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.ToggleButton;
import com.example.project_v1.R;
import com.example.project_v1.database.dbHelper_AllDevices;
import com.example.project_v1.database.dbHelper_UserDevices;
import com.example.project_v1.dialogs.add_device;
import com.example.project_v1.dialogs.delete_device;
import com.example.project_v1.models.Device;
import com.example.project_v1.models.IndustrialDevice;
import com.example.project_v1.models.ViewHolder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class DeviceManagement extends AppCompatActivity {

    private Handler mHandler = new Handler();
    private static Integer timestamp=1;
    Button settingsButton;
    protected ListView devicesListView;                         //main listView
    private static List<Device> deviceList;                           //contains all devices form a user // needs to be static so that user can sign out and come back in and add device
    protected FloatingActionButton addDeviceFloatingButton;     //add a device
    private FirebaseDatabase database;                          //All database data
    private DatabaseReference mDatabase;            //user's info (name, email, password and devices)
    private FirebaseDatabase adatabase;                          //All database data
    private DatabaseReference amDatabase;               //user's info (name, email, password and devices)
    private FirebaseAuth Fauth;
    private static final String USER = "user";
    private static String userID;
    private dbHelper_UserDevices dataBaseHelper;
    private dbHelper_AllDevices dbHelper_AllDevices;
    private boolean skip_unwanted_onDataChangeListener; //as the name says  "see function set_The_Listeners
    final myAdapter arrayAdapter = new myAdapter();
    protected List<ViewHolder> viewHolderList;
    public Context con;
    String userUID;

    public DeviceManagement() {
        super();
    }

    @Override
    protected void onResume() {
        super.onResume();
        userID = Fauth.getUid();
        userUID=userID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBaseHelper = new dbHelper_UserDevices(this);      //access the devices related to the user
        dbHelper_AllDevices = new dbHelper_AllDevices(this);    //access all devices related to the company
        setContentView(R.layout.activity_device_management);
        deviceList = new ArrayList<>();//be sure the create an array list right at the beginning
        viewHolderList = new ArrayList<>();  // store all the view of the listview
        Fauth = FirebaseAuth.getInstance();

        getSupportActionBar().setTitle("Devices Management");

        //user Database related
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference(USER);

        //All Devices (industrial) related
        adatabase = FirebaseDatabase.getInstance();
        amDatabase = adatabase.getReference("Devices");

        devicesListView = findViewById(R.id.deviceListView);
        addDeviceFloatingButton= findViewById(R.id.floatingActionButton);
        settingsButton=findViewById(R.id.settingsButton);

        //retrieve the user email
        Intent intent = getIntent();
        userID = intent.getStringExtra(("userID"));        //all user shall have one and only one email
        userUID = userID;

        //determine if there are any devices related to the user.
        //loadListView got 3 modes
        //  -   add device
        //  -   delete device
        //  -   update from the device / normal mode
        loadListView("Nothing", "Nothing",null, null, "normal");
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
                FirebaseAuth mAuth;
                mAuth=FirebaseAuth.getInstance();

                if(mAuth.getCurrentUser()!=null && userUID.equals(mAuth.getCurrentUser().getUid())){
                    Boolean gg=false;
                    String change =snapshot.getRef().getKey().toString();   //GETS NAME OF DEVICE INFO CHANGED
                    String check = snapshot.child("Status").getValue().toString();  //CHECKS IF STATUS "ON"

                    if(check.contains("N")){
                        gg=true;
                    }

                    if(gg==true){
                        notification(change);}
                }
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

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
                add_device dialog = new add_device(v.getContext());
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
                if(dataSnapshot.exists())   //Allows user to add new device if she signs out and back in (only 1)
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
    public void loadListView(final String input_device_id, final String input_Device_Name, final String input_Device_State, final String input_Device_Power, final String action_performed) {

        if(userID==null || userID.equals( "null")){
            userID = Fauth.getUid();
            userUID=userID;
        }

/*
        //listen all the industrial devices under the "Devices" folder
        final DatabaseReference device_data = FirebaseDatabase.getInstance().getReference("Devices");
        device_data.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //be sure one can only add a device one (iff a device is not connected)
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String dummy_id = userSnapshot.getKey();    //return the device id
                    String dummy_name = userSnapshot.child("EditedName").getValue(String.class);
                    String dummy_password = userSnapshot.child("Password").getValue(String.class);
                    String dummy_time = userSnapshot.child("Time").getValue(String.class);
                    String dummy_userUID = userSnapshot.child("UserID").getValue(String.class);
                    IndustrialDevice dummyDevice = new IndustrialDevice(dummy_id, dummy_name, dummy_password, dummy_time, dummy_userUID);
                    // if it return false, then the device is not already in database
                    if(!dbHelper_AllDevices.password_id_match(dummy_password, dummy_id)) {
                        //To add the device to teh database an industrial device
                        if (dummy_userUID.equals("NEW") || dummy_name.equals("NEW")) {
                            ; //return a device type (name and status)
                            dbHelper_AllDevices.add_Industrial_Device(dummyDevice);  //add the device to db
                        }

                        //someone updated our device... is it the Time?
                        else if (dummy_userUID.equals(userUID) && dummy_time.equals("1")) {
                            mDatabase.child(input_device_id).child("Time").setValue("0");
                        }
                    }
                    else{
                        dbHelper_AllDevices.update_Industrial_Device(dummyDevice);
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //do nothing
            }
        });
    */
        //listen to the device_ under the "user" folder
        final DatabaseReference user_data = mDatabase.child(userID);//user's data -  devices included
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
                //as one cannot read data from firebase and write at the same time (Data not directly updated)
                if (action_performed.equals("add_device")) {
                    boolean deviceFound = false;

                    // loop throught the deviceList to get the targeted device
                    // sometimes it iterates twice, so just verify first
                    for(int i = 0; i < deviceList.size(); i++){
                        if(deviceList.get(i).getName().equals(input_Device_Name)){
                            deviceFound = true;
                            break;
                        }
                    }

                    if(!deviceFound) {
                        //now,register the customized user info in "REALTIME DATABASE"
                        Device dummyDevice = new Device(input_device_id, input_Device_Name, input_Device_State, input_Device_Power);
                        deviceList.add(dummyDevice);        //add the device in device list
//
                        //add the device name - and set the status as a child
                        skip_unwanted_onDataChangeListener = true;// deactivate the firebase, onDataChange listener
                        mDatabase.child(userID).child("numberOfDevices").setValue(Integer.toString(deviceList.size()));
                        mDatabase.child(userID).child("Devices").child(dummyDevice.getName()).child("Power").setValue(dummyDevice.getPower());
                        mDatabase.child(userID).child("Devices").child(dummyDevice.getName()).child("Status").setValue(dummyDevice.getStatus());
                        mDatabase.child(userID).child("Devices").child(dummyDevice.getName()).child("ID").setValue(dummyDevice.returnID());
                        mDatabase.child(userID).child("Devices").child(dummyDevice.getName()).child("Status").setValue("OFF");

                        //edit the user info under the "Device" Folder
                        amDatabase.child(input_device_id).child("EditedName").setValue(input_Device_Name);
                        amDatabase.child(input_device_id).child("UserID").setValue(userID);
                        amDatabase.child(input_device_id).child("Time").setValue("0");
                    }
                }
                //Normal operation mode of the DeviceManagementActivity
                else if(action_performed.equals("delete_device")){
                    Device dummyDevice = new Device(input_device_id,input_Device_Name, input_Device_State, input_Device_Power);

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
                            String dummy_id = userSnapshot.child("Status").getValue(String.class);
                            Device dummyDevice = new Device(dummy_id,dummy_name, dummy_state, dummy_power);; //return a device type (name and status)
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
                            mDatabase.child(userUID).child("Devices").child(deviceList.get((Integer) buttonView.getTag()).getName()).child("ring_state").setValue("OFF");
                        } else {
                            skip_unwanted_onDataChangeListener = true;
                            update_device_states((Integer) buttonView.getTag(), "OFF", "ON");
                            mDatabase.child(userUID).child("Devices").child(deviceList.get((Integer) buttonView.getTag()).getName()).child("ring_state").setValue("ON");
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

    protected void notificationDeviceFailed(){
        PowerManager pm = (PowerManager) getApplication().getBaseContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = Build.VERSION.SDK_INT >= 20 ? pm.isInteractive() : pm.isScreenOn(); // check if screen is on

        if (!isScreenOn) {
            if (!isScreenOn) {
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "myApp:notificationLock");
                wl.acquire(3000); //set your time in milliseconds
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("n","n", NotificationManager.IMPORTANCE_DEFAULT);
                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"n")
                              .setContentTitle("Fire Detection System")
                              .setContentText("Device Connection Failed")
                              .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                              .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.notify(999,builder.build());
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("n","n", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"n")
                          .setContentTitle("Fire Detection System")
                          .setContentText("Device Connection Failed")
                          .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                          .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(999,builder.build());
    }

     public void startRepeating(){
        getApplicationContext();
        mHandler.postDelayed(mNotificationRunnable,150000);
        /*mNotificationRunnable.run();*/
    }

    public void stopRepeating(){
        mHandler.removeCallbacks(mNotificationRunnable);
    }

    public Runnable mNotificationRunnable = new Runnable() {
        @Override
        public void run() {
            mDatabase = database.getReference("Devices");
            DatabaseReference reference=mDatabase;

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.hasChild("Kul78vB")){
                       if(Fauth.getUid().equals(snapshot.child("Kul78vB").child("UserID").getValue().toString()) ){
                        //IF arduino sends integer, than change c to a integer.
                       String c = snapshot.child("Kul78vB").child("Time").getValue().toString();
                       Integer k= Integer.parseInt(c);
                       if(k!=timestamp){
                           notificationDeviceFailed(); /*Toast.makeText(getApplicationContext(),"Good",Toast.LENGTH_SHORT).show();*/
                       }
                       else{
                           amDatabase.child("Kul78vB").child("Time").setValue("0");  /*Toast.makeText(getApplicationContext(),"Bad",Toast.LENGTH_SHORT).show(); */
                       }
                    }
                }}

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            mHandler.postDelayed(this,120000);
        }
    };
}