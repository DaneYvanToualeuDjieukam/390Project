package com.example.project_v1.models;


import android.graphics.Color;
import android.view.View;
import android.widget.Switch;
import android.widget.ToggleButton;
import com.example.project_v1.R;

public class ViewHolder {

    public ToggleButton toggle_Name_Power;
    public Switch switch_Ring_State;

    // default constructor
    public ViewHolder(){
        toggle_Name_Power = null;
        switch_Ring_State = null;
    }

    // constructor
    public ViewHolder(View view){
        this.toggle_Name_Power = (ToggleButton) view.findViewById(R.id.deviceName);
        this.switch_Ring_State = (Switch) view.findViewById(R.id.deviceState);
        view.setTag(this);
    }

    // return the state of the toggle button
    public boolean toggle_Name_Power_Function(){
        if(toggle_Name_Power.isChecked()){
            return true;
        }
        return false;
    }

    // return the state of the switch
    public boolean switch_Ring_State_Function(){
        if(switch_Ring_State.isChecked()){
            return true;
        }
        return false;
    }

    /*
    // when toggle is pressed (off), put a gray background color and disable to ring switch
    public void turn_device_Off(){
        switch_Ring_State.setChecked(false); // set the device ring as false
        switch_Ring_State.setEnabled(false); // To disable a Switch use following method
        switch_Ring_State.setClickable(false); // To make switch not clickable use
        toggle_Name_Power.setBackgroundColor(Color.argb(62, 186, 186, 186)); //gray color
    }

    // when toggle is pressed (on), put a green background color and enable to ring switch
    public void turn_device_on(){
        switch_Ring_State.setChecked(false); // set the device ring as false
        switch_Ring_State.setEnabled(true); // To enable a Switch use following method
        switch_Ring_State.setClickable(true); // To make switch clickable use
        toggle_Name_Power.setBackgroundColor(Color.argb(100, 126, 251, 161)); // green color
    }
    */
}