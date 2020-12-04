package com.example.project_v1.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.example.project_v1.models.Device;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;

import static com.example.project_v1.database.Config.COLUMN_DEVICE_ID;

public class DatabaseHelper extends SQLiteOpenHelper {


    private Context context;
    private static final String TAG = "DatabaseHelper";
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private static final String USER = "user";
    private static final  String CREATE_TABLE_DEVICE = " CREATE TABLE IF NOT EXISTS "
            + Config.DEVICE_TABLE_NAME + " ("
            + Config.COLUMN_DEVICE_NAME + " TEXT NOT NULL, "
            + Config.COLUMN_DEVICE_STATE + " TEXT NOT NULL, "
            + Config.COLUMN_DEVICE_POWER + " TEXT NOT NULL, "
            + Config.COLUMN_DEVICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT)";



    public  DatabaseHelper (Context context) {
        super(context, Config.DATABASE_NAME,null,Config.DATABASE_VERSION);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_DEVICE);        //create the Table Device
sqLiteDatabase.close();


    }

    @Override
    public void onUpgrade (SQLiteDatabase sqLiteDatabase, int i, int i1){
        //do nothing yet
    }


    //************************************* DEVICES OPERATIONS **************************************
    /*
     * Adding a device
     */
    public void addDevice(Device device) {
        //access the data base

        SQLiteDatabase db;
        db=getWritableDatabase();


        //put the values in Course Table
        ContentValues values = new ContentValues();
        //no need to add the course_id, since it's auto incremental
        values.put( Config.COLUMN_DEVICE_NAME , device.getName());
        values.put( Config.COLUMN_DEVICE_STATE  , device.getStatus());
        values.put( Config.COLUMN_DEVICE_POWER  , device.getStatus());
        // insert row
        db.insert(Config.DEVICE_TABLE_NAME, null, values);
        if(db!=null)
        db.close();
    }

    /*
     * Deleting a device
     */
    public void delete_Device(String device_Name) {
        //Here whereClause is optional, passing null will delete all rows in table.
        //delete function will return number of affected row if whereClause passed otherwise will return 0.
        //see - https://abhiandroid.com/database/operation-sqlite.html

        SQLiteDatabase db;
        db=getWritableDatabase();
        //or use teh following
        //db.delete(TABLE_COURSES, KEY_COURSE_ID + " = " + String.valueOf(key_course),
        //       new String[] { String.valueOf(key_course) });
        //delete the course
        db.delete(Config.DEVICE_TABLE_NAME, Config.DEVICE_TABLE_NAME + " = " + device_Name, null);
        if(db!=null)
        db.close();
    }

    /*
     * Edit a device's name
     */
    public void editDeviceName(String device_name) {
        //access the data base
        SQLiteDatabase db;
        db=getWritableDatabase();


        ContentValues values = new ContentValues();

        values.put( Config.COLUMN_DEVICE_NAME ,device_name);
        // update only the device name
        db.update(Config.DEVICE_TABLE_NAME, values, Config.COLUMN_DEVICE_NAME+ "=" + device_name , null);
        if(db!=null)
        db.close();
    }

    /*
     * Edit a device's state
     */
    public void update_Device_Status(String device_name, String device_state) {
        //access the data base

        SQLiteDatabase db;
        db=getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put( Config.COLUMN_DEVICE_STATE ,device_state);
        // update only the device name

        db.update(Config.DEVICE_TABLE_NAME, values, Config.COLUMN_DEVICE_NAME+ "=" + device_name , null);
        if(db!=null)
        db.close();
    }

    /*
     * Edit a device's state
     */
    public void update_Device_Power(String device_name, String device_power) {
        //access the data base
        SQLiteDatabase db;
        db=getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put( Config.COLUMN_DEVICE_STATE ,device_power);
        // update only the device name
        db.update(Config.DEVICE_TABLE_NAME, values, Config.COLUMN_DEVICE_NAME+ "=" + device_name , null);
       if(db!=null)
        db.close();
    }

    /*
     * Return the user's devices info
     */
    public List<Device> getAllDevices(){
        SQLiteDatabase db;
        db=getReadableDatabase();
        List <Device> deviceList =new ArrayList<>();
        Cursor cursor = null;

        //error handle in case there is nothing in the db
        try{
            //Link the cursor to the Table_Courses
            //see - https://stackoverflow.com/questions/10600670/sqlitedatabase-query-method
            //cursor at "before - first" element of the table
            cursor = db.query(Config.DEVICE_TABLE_NAME, null, null,
                    null, null,null,null);

            if(cursor != null) {
                cursor.moveToFirst();   //point the first element of the table

                do{
                    //int id = cursor.getInt(cursor.getColumnIndex(COLUMN_DEVICE_ID));
                    String device_Name = cursor.getString(cursor.getColumnIndex(Config.COLUMN_DEVICE_NAME));
                    String device_State = cursor.getString(cursor.getColumnIndex(Config.COLUMN_DEVICE_STATE));
                    String device_Power = cursor.getString(cursor.getColumnIndex(Config.COLUMN_DEVICE_POWER));
                    deviceList.add(new Device(device_Name, device_State, device_Power));
                } while(cursor.moveToNext());
            }
        }
        catch(Exception e){
            //output the error msg if one
            Toast.makeText(context, "operation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        finally {//if no error - keep going
            if(cursor != null)
                cursor.close();
        }
        if(db!=null)
        db.close();
        return deviceList;
    }

    /*
     * Return true if the device name is available
     */
    public boolean is_The_Device_Name_Available(String device_Name){

             SQLiteDatabase   db=getReadableDatabase();


        Device device = new Device();
        boolean available = false;
        Cursor cursor = null;

        //error handle in case there is nothing in the db
        try{
            //Link the cursor to the Table_Courses - and only ge the values with the proper course ID
            //see - https://stackoverflow.com/questions/10600670/sqlitedatabase-query-method
            //String selectQuery = "SELECT * FROM " + TABLE_ASSIGNMENTS + " WHERE "
            //        + COURSE_ID + " = " + String.valueOf(key_course);
            //or use the following for the same output
            //COURSE_ID + " = " + String.valueOf(key_course)
            cursor = db.query(Config.DEVICE_TABLE_NAME, null, Config.COLUMN_DEVICE_NAME+ "=" + String.valueOf(device_Name)
                    ,null,null,null,null);

            if(cursor != null) {
                available = false; //if cursor is not null, then there is already a device with the name name
            }
        }
        catch(Exception e){
            //in case of an error, return true
            available = true;
        }
        finally //if no error - keep going
        {
            if(cursor != null)
                cursor.close();
            if(db!=null)
            db.close();
        }

        return available;
    }

    /*
     * Return the user's devices info
     */
    public Device getSingularDevices(int key_device){
        SQLiteDatabase db;
        db=getReadableDatabase();
        Device device = new Device();
        Cursor cursor = null;

        //error handle in case there is nothing in the db
        try{
            //Link the cursor to the Table_Courses - and only ge the values with the proper course ID
            //see - https://stackoverflow.com/questions/10600670/sqlitedatabase-query-method
            //String selectQuery = "SELECT * FROM " + TABLE_ASSIGNMENTS + " WHERE "
            //        + COURSE_ID + " = " + String.valueOf(key_course);
            //or use the following for the same output
            //COURSE_ID + " = " + String.valueOf(key_course)
            cursor = db.query(Config.DEVICE_TABLE_NAME, null, Config.COLUMN_DEVICE_ID+ "=" + String.valueOf(key_device)
                    ,null,null,null,null);

            if(cursor != null) {
                cursor.moveToFirst();   //point the first element of the table
                String device_Name = cursor.getString(cursor.getColumnIndex(Config.COLUMN_DEVICE_NAME));
                String device_State =  cursor.getString(cursor.getColumnIndex(Config.COLUMN_DEVICE_STATE));
                String device_Power =  cursor.getString(cursor.getColumnIndex(Config.COLUMN_DEVICE_POWER));
                device.setName(device_Name);
                device.setStatus(device_State);
                device.setPower(device_Power);
            }
        }
        catch(Exception e){
            //output the error msg if one
            //Toast.makeText(context, "operation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        finally //if no error - keep going
        {
            if(cursor != null)
                cursor.close();
            if(db!=null)
            db.close();
        }

        return device;
    }
}







