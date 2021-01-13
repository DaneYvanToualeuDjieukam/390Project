package com.example.project_v1.database;

public class Config {
    public static final String DATABASE_NAME = "devices.db";
    public static final int DATABASE_VERSION=1;

    //device related under the user
    public  static final String DEVICE_TABLE_NAME="device";
    public static final String COLUMN_DEVICE_ID=    "_id";
    public static final String COLUMN_DEVICE_STATE = "state";
    public static final String COLUMN_DEVICE_NAME= "name";
    public static final String COLUMN_DEVICE_POWER= "power";
    public static final String COLUMN_DEVICE_CODE = "CODE";

    //device related by teh industry
    public  static final String ALL_TABLE_NAME="Industrial_device";
    public static final String ALL_DEVICE_ID =    "id";
    public static final String ALL_DEVICE_CODE = "CODE";
    public static final String ALL_DEVICE_NAME = "Edited Name";
    public static final String ALL_DEVICE_PASSWORD = "Password";
    public static final String ALL_DEVICE_TIME = "Time";          //0 if not connected to wifi, 1 if connected
    public static final String ALL_DEVICE_USERID = "UserID";
}
