package com.example.project_v1.models;

public class Device {

    protected Integer id;   //id used in all devices - should perhaps be long to avoid a potential carrying capacity
    private String name;
    private String status; //as is there a fire/smoke?

    //default constructor
    public Device() {
        this.name = null;
        this.status = null;
    }

    //Constructor - name, smokeDetected, and fireDetected
    public Device(String name, String status) {
        this.name = name;
        this.status = status;
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

}
