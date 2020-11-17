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
}