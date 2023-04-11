package com.exambullet.locationapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "location.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "locations";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_OTHER_INFO = "other_info";
    public static final String COLUMN_VEHICLE="vehicle";
    public static final String COLUMN_TRIP_NUMBER="trip_number";
    public static final String COLUMN_ODOMETER_START="odometer_start";
    public static final String COLUMN_ODOMETER_END="odometer_end";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_TIMESTAMP + " INTEGER, " +
                COLUMN_OTHER_INFO + " TEXT," +
                COLUMN_TRIP_NUMBER + " INTEGER," +
                COLUMN_ODOMETER_START + " REAL," +
                COLUMN_ODOMETER_END + " REAL," +
                COLUMN_VEHICLE + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertLocation(Location location, String otherInfo,int tripNumber,double odometerStart, double odometerEnd) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, location.getLatitude());
        values.put(COLUMN_LONGITUDE, location.getLongitude());
        values.put(COLUMN_TIMESTAMP, location.getTime());
        values.put(COLUMN_OTHER_INFO, otherInfo);
        values.put(COLUMN_VEHICLE,"VEHICLE123456");
        if(tripNumber!=-1) values.put(COLUMN_TRIP_NUMBER,tripNumber);
        if(odometerStart!=-1) values.put(COLUMN_ODOMETER_START,odometerStart);
        if(odometerEnd!=-1) values.put(COLUMN_ODOMETER_END,odometerEnd);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    @SuppressLint("Range")
    public List<LocationData> getAllLocations() {
        List<LocationData> locations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {

            do {
                double latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE));
                long timestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP));
                String otherInfo = cursor.getString(cursor.getColumnIndex(COLUMN_OTHER_INFO));
                String vehicleData = cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE));
                 int tripNumber = (cursor.getColumnIndex(COLUMN_TRIP_NUMBER) == -1) ? -1 : cursor.getInt(cursor.getColumnIndex(COLUMN_TRIP_NUMBER));
                int odometerStart = cursor.getColumnIndex(COLUMN_ODOMETER_START) == -1 ? -1 : cursor.getInt(cursor.getColumnIndex(COLUMN_ODOMETER_START));
                int odometerEnd = cursor.getColumnIndex(COLUMN_ODOMETER_END) == -1 ? -1 : cursor.getInt(cursor.getColumnIndex(COLUMN_ODOMETER_END));

                Location location = new Location("");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                location.setTime(timestamp);

                LocationData locationData= new LocationData(location,otherInfo,vehicleData,tripNumber,odometerStart,odometerEnd);
                locations.add(locationData);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return locations;
    }
}
