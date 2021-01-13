package com.example.project_v1.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.project_v1.models.IndustrialDevice;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class dbHelper_AllDevices  extends SQLiteOpenHelper{

    private static SQLiteDatabase dbb;
    private Context context;
    private static final String TAG = "DatabaseHelper";
    private FirebaseDatabase database;
    public Context con;
    private static SQLiteDatabase db;
    private DatabaseReference mDatabase;
    private static final String USER = "user";

    private static final  String CREATE_TABLE_ALL_DEVICES = " CREATE TABLE IF NOT EXISTS "
            + Config.ALL_TABLE_NAME + " ("
            + Config.ALL_DEVICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Config.ALL_DEVICE_CODE + " TEXT NOT NULL, "
            + Config.ALL_DEVICE_NAME + " TEXT NOT NULL, "
            + Config.ALL_DEVICE_PASSWORD + " TEXT NOT NULL, "
            + Config.ALL_DEVICE_TIME + " TEXT NOT NULL, "
            + Config.ALL_DEVICE_USERID + " TEXT NOT NULL)";

    public dbHelper_AllDevices(Context context) {
        super(context, Config.DATABASE_NAME,null,Config.DATABASE_VERSION);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_ALL_DEVICES);        //create the Table Device
        dbb=sqLiteDatabase;
    }

    @Override
    public void onUpgrade (SQLiteDatabase sqLiteDatabase, int i, int i1){
        //do nothing yet
    }

    /*
     * Adding a device under the industry
     */
    public void add_Industrial_Device(IndustrialDevice device) {

        //access the data base
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        //no need to add the course_id, since it's auto incremental
        values.put( Config.ALL_DEVICE_CODE ,device.getCode());
        values.put( Config.ALL_DEVICE_NAME  , device.getEditedname());
        values.put( Config.ALL_DEVICE_PASSWORD  , device.getPassword());
        values.put( Config.ALL_DEVICE_TIME  , device.getTime());
        values.put( Config.ALL_DEVICE_USERID  , device.getUseruid());

        // insert row
        db.insert(Config.ALL_TABLE_NAME, null, values);
        db.close();
    }

    /*
     * update a device under the industry
     */
    public void update_Industrial_Device(IndustrialDevice device) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        //no need to add the course_id, since it's auto incremental
        values.put( Config.ALL_DEVICE_CODE ,device.getCode());
        values.put( Config.ALL_DEVICE_NAME  , device.getEditedname());
        values.put( Config.ALL_DEVICE_PASSWORD  , device.getPassword());
        values.put( Config.ALL_DEVICE_TIME  , device.getTime());
        values.put( Config.ALL_DEVICE_USERID  , device.getUseruid());

        // update row
        db.update( Config.ALL_TABLE_NAME,values, Config.ALL_DEVICE_CODE + "=" + device.getCode(), null);
        db.close();
    }

    /*
     * Edit an INDUSTRIAL device's name
     */
    public void editDeviceName(String device_name, String device_code ) {
        //access the data base
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put( Config.ALL_DEVICE_NAME ,device_name);
        // update only the device name
        db.update(Config.ALL_TABLE_NAME, values, Config.ALL_DEVICE_CODE+ "=" + device_code , null);
        db.close();
    }

    /*
     * Return true if the device name is available/ from a user stand point who has multiple devices connected to it
     */
    public boolean password_id_match(String device_password, String device_code){

        SQLiteDatabase db=getReadableDatabase();
        IndustrialDevice device = new IndustrialDevice();
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

            String sql = "SELECT FROM " + Config.ALL_TABLE_NAME + " WHERE " + "(" + Config.ALL_DEVICE_PASSWORD + "=" + String.valueOf(device_password) + ")";

            //cursor = db.query(Config.INDUS_TABLE_NAME, null, Config.INDUS_DEVICE_PASSWORD + "=?" ,new String[]{String.valueOf(device_password)},null,null,null);
            cursor = db.rawQuery(sql, null);
///
            if(cursor != null) {
                cursor.moveToFirst();   //point the first element of the table
                if(cursor.getString(cursor.getColumnIndex(Config.ALL_DEVICE_CODE)).equals(device_code)) {
                    available = true; //if cursor is not null, then the password and id match
                }else{
                    available = false; //if cursor is not null, then the password and id match
                }
            }
        }
        catch(Exception e){
            //in case of an error, return true
            available = false;      //device does not exist from an industry stand point
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
     * Return true if the device name is available/ from a user stand point who has multiple devices connected to it
     */
    public boolean is_the_device_connected_to_a_user(String device_code){

        SQLiteDatabase db=dbb;
        db=getReadableDatabase();

        IndustrialDevice device = new IndustrialDevice();
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
            cursor = db.query(Config.ALL_TABLE_NAME, null, Config.ALL_DEVICE_USERID+ "=" + String.valueOf(device_code)
                    ,null,null,null,null);

            if(cursor != null) {
                cursor.moveToFirst();   //point the first element of the table
                String userId = cursor.getString(cursor.getColumnIndex(Config.ALL_DEVICE_USERID));
                String editedname =  cursor.getString(cursor.getColumnIndex(Config.ALL_DEVICE_NAME));

                if(userId.equals("NEW") || editedname.equals("NEW")){
                    available = false; //if cursor is not null, then there is already a device with the name name
                }
                else
                {
                    available = false; //if cursor is not null, then there is already a device with the name name
                }
            }
        }
        catch(Exception e){
            //in case of an error, return true
            available = false;      //device does not exist from an industry stand point
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

}
