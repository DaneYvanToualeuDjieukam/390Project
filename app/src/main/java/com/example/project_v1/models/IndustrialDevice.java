package com.example.project_v1.models;

public class IndustrialDevice {

    private String code;   //id used in all devices - should perhaps be long to avoid a potential carrying capacity
    private String editedname; //as is there a fire/smoke?
    private String password; // define if the device is powered on or off
    private String time; //as is there a fire/smoke?
    private String useruid; // define if the device is powered on or off

    //default constructor
    public IndustrialDevice() {
        this.code = null;
        this.editedname = null;
        this.password = null;
        this.time = null;
        this.useruid = null;
    }

    //Constructor - name, smokeDetected, and fireDetected
    public IndustrialDevice(String code, String editedname, String password, String time, String useruid) {
        this.code = code;
        this.editedname = editedname;
        this.password = password;
        this.time = time;
        this.useruid = useruid;
    }

    //Device Name - setter and getter *********
    public String getCode() {
        return code;
    }

    public void setCode(String name) {
        this.code = name;
    }

    //Device Editedname - setter and getter *********
    public String getEditedname() {
        return editedname;
    }

    public void setEditedname(String status) {
        this.editedname = status;
    }

    //Device Password - setter and getter *********
    public String getPassword() {
        return password;
    }

    public void setPassword(String power) {
        this.password = power;
    }

    //Device Time - setter and getter *********
    public String getTime() {
        return time;
    }

    public void setTime(String power) {
        this.time = power;
    }

    //Device Useruid - setter and getter *********
    public String getUseruid() {
        return useruid;
    }
//
    public void setUseruid(String power) {
        this.useruid = power;
    }

    //Return the device as a Device Object
    public IndustrialDevice return_Device(){
        IndustrialDevice dummy = new IndustrialDevice(code, editedname, password, time, useruid);
        return dummy;
    }

}

