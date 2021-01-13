package com.example.project_v1.models;

public class Device {

    //protected Integer id;   //id used in all devices - should perhaps be long to avoid a potential carrying capacity
    private String name;
    private String status; //as is there a fire/smoke?
    private String power; // define if the device is powered on or off
    private String id;

    //default constructor
    public Device() {
        this.name = null;
        this.status = null;
        this.power = null;
        this.id = null;
    }

    //Constructor - name, smokeDetected, and fireDetected
    public Device(String id, String name, String status, String Power) {
        this.name = name;
        this.status = status;
        this.power = Power;
        this.id = id;
    }

    //Device Name - setter and getter *********
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //Fire/smoke detected - setter and getter *********
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    //Device Power - setter and getter *********
    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    //Device Power - setter and getter *********
    public String returnID() {
        return id;
    }

    public void initializeID(String power) {
        this.id = id;
    }

    //Return the device as a Device Object
    public Device return_Device(){
        Device dummy = new Device(id, name, status, power);
        return dummy;
    }

}

