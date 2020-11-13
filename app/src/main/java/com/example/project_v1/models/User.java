package com.example.project_v1.models;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String email;
    private String password;
    private String username;
    private String key_id;
    private int NumberOfDevices;
    private List<Device> deviceList;    //as users and can multiple devices

    //default constructors
    public User() {
        email = null;
        password = null;
        username = null;
        key_id = null;
        deviceList = new ArrayList<>();
        NumberOfDevices = 0;
    }

    //constructor - email, password, and username
    public User (String email, String password, String username)
    {
        this.deviceList = new ArrayList<>();
        this.email = email.toLowerCase();
        this.password = password;
        this.username = username;
        NumberOfDevices = 0;
    }

    //constructor - email, password, username and NumberOfDevices
    public User (String email, String password, String username, int NumberOfDevices)
    {
        this.email = email.toLowerCase();
        this.password = password;
        this.username = username;
        this.NumberOfDevices = NumberOfDevices;
        this.deviceList.clear();
    }

    //constructor - email, password, username and one device
    public User (String email, String password, String username, Device device)
    {
        this.email = email.toLowerCase();
        this.password = password;
        this.username = username;
        this.deviceList = new ArrayList<>();
        this.deviceList.add(device);
    }

    //constructor - email, password, username and Multiple devices
    public User (String email, String password, String username, List<Device>  deviceList)
    {
        this.email = email.toLowerCase();
        this.password = password;
        this.username = username;

        //get all device
        this.deviceList = new ArrayList<>();
        for(int i=0; i<deviceList.size(); i++){
            this.deviceList.add(deviceList.get(i));
        }
    }


    //**Email Setter - Getter*****************************
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    //**Password Setter - Getter*****************************
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    //**Username Setter - Getter*****************************
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    //**key_id Setter - Getter*****************************
    public String getKey_id() {
        return key_id;
    }

    public void setKey_id(String key_id) {
        this.key_id = key_id;
    }

    //**Device Setters - Getters*****************************
    public Device getDevice(int position) { //return a particular device
        return deviceList.get(position);
    }

    //return all devices
    public List<Device> getAllDevices() {
        return deviceList;
    }

    //return the total number of devices connected to an user
    public Integer getNumberOfDevices() {
        return deviceList.size();
    }

    //set a particular device
    public void setDevice(int position, Device device) {
        deviceList.get(position).setName(device.getName());
        deviceList.get(position).setStatus(device.getStatus());
    }

    //set a particular device
    public void addDevice(Device device) {
        deviceList.add(device);
    }
}