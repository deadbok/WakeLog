package net.groenholdt.wakelog.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by oblivion on 05/04/16.
 * <p/>
 * Great tutorial at: http://blog.cindypotvin.com/saving-to-a-sqlite-database-in-your-android-application/
 */
public class LogDatabaseHelper extends SQLiteOpenHelper
{
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    // The name of the database file on the file system
    public static final String DATABASE_NAME = "log.db";
    private static final String TAG = "LogDatabaseHelper";

    public LogDatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates the underlying database with the SQL_CREATE_TABLE queries from
     * the contract classes to create the tables and initialize the data.
     * The onCreate is triggered the first time someone tries to access
     * the database with the getReadableDatabase or
     * getWritableDatabase methods.
     *
     * @param db the database being accessed and that should be created.
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // Create the database to contain the data for the projects
        db.execSQL(DeviceContract.SQL_CREATE_TABLE);
        db.execSQL(LogContract.SQL_CREATE_TABLE);
    }

    /**
     * This method must be implemented if your application is upgraded and must
     * include the SQL query to upgrade the database from your old to your new
     * schema.
     *
     * @param db         the database being upgraded.
     * @param oldVersion the current version of the database before the upgrade.
     * @param newVersion the version of the database after the upgrade.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Logs that the database is being upgraded
        Log.i(TAG, "Upgrading database from version " + oldVersion + " to " +
                newVersion);
    }

    /**
     * Add a device.
     *
     * @param deviceName mDNS name of the device.
     * @param ipAddr     Last known IP address of the device.
     * @param syncTime   Time of last sync.
     */
    public void addDevice(String deviceName, int ipAddr, int syncTime)
    {
        SQLiteDatabase db = getWritableDatabase();

        // Create the database row for the project and keep its unique identifier
        ContentValues deviceValues = new ContentValues();

        deviceValues
                .put(DeviceContract.DeviceEntry.COLUMN_NAME_NAME, deviceName);
        deviceValues.put(DeviceContract.DeviceEntry.COLUMN_NAME_IP, ipAddr);
        deviceValues
                .put(DeviceContract.DeviceEntry.COLUMN_NAME_SYNC_TIME, syncTime);

        long deviceId =
                db.insert(DeviceContract.TABLE_NAME, null, deviceValues);
    }

    /**
     * Add a log entry.
     *
     * @param logTime Log time stamp.
     * @param type    Entry type.
     * @param device  Log device ID.
     */
    public void addLogEntry(String logTime, int type, int device)
    {
        SQLiteDatabase db = getWritableDatabase();

        // Create the database row for the project and keep its unique identifier
        ContentValues logValues = new ContentValues();

        logValues.put(LogContract.LogEntry.COLUMN_NAME_TIME, logTime);
        logValues.put(LogContract.LogEntry.COLUMN_NAME_TYPE, type);
        logValues.put(LogContract.LogEntry.COLUMN_NAME_DEVICE, device);

        long logId = db.insert(LogContract.TABLE_NAME, null, logValues);
    }

    private ArrayList<LogEntry> getLog(long deviceId)
    {
        ArrayList<LogEntry> log = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor logCursor = db.query(LogContract.TABLE_NAME,
                null,
                LogContract.LogEntry.COLUMN_NAME_DEVICE + "=?",
                new String[]{String.valueOf(deviceId)},
                null,
                null,
                LogContract.LogEntry._ID);

        while (logCursor.moveToNext())
        {
            LogEntry logEntry = new LogEntry();
            logEntry.setId(logCursor.getLong(logCursor
                    .getColumnIndex(LogContract.LogEntry._ID)));

            int time = logCursor.getInt(logCursor
                    .getColumnIndex(LogContract.LogEntry.COLUMN_NAME_TIME));
            logEntry.setTime(time);

            int type = logCursor.getInt(logCursor
                    .getColumnIndex(LogContract.LogEntry.COLUMN_NAME_TYPE));
            logEntry.setType(type);

            int device = logCursor.getInt(logCursor
                    .getColumnIndex(LogContract.LogEntry.COLUMN_NAME_DEVICE));
            logEntry.setDeviceId(device);

            log.add(logEntry);
        }

        logCursor.close();

        return (log);
    }

    public Device getDevice(long deviceId)
    {
        SQLiteDatabase db = getReadableDatabase();

        Cursor deviceCursor = db.query(DeviceContract.TABLE_NAME,
                null,
                DeviceContract.DeviceEntry._ID + "=?",
                new String[]{String.valueOf(deviceId)},
                null,
                null,
                null);
        deviceCursor.moveToNext();

        Device device = new Device();
        device.setId(deviceCursor.getLong(deviceCursor
                .getColumnIndex(DeviceContract.DeviceEntry._ID)));
        device.setName(deviceCursor.getString(deviceCursor
                .getColumnIndex(DeviceContract.DeviceEntry.COLUMN_NAME_NAME)));
        device.setIpAddr(deviceCursor.getInt(deviceCursor
                .getColumnIndex(DeviceContract.DeviceEntry.COLUMN_NAME_IP)));
        device.setSyncTime(deviceCursor.getInt(deviceCursor
                .getColumnIndex(DeviceContract.DeviceEntry.COLUMN_NAME_SYNC_TIME)));

        device.setLog(getLog(deviceId));

        deviceCursor.close();

        return (device);
    }

    public ArrayList<Device> getDevices()
    {
        ArrayList<Device> devices = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor deviceCursor =
                db.query(DeviceContract.TABLE_NAME, null, null, null, null, null, null);
        while (deviceCursor.moveToNext())
        {
            long deviceId = deviceCursor.getLong(deviceCursor
                    .getColumnIndex(DeviceContract.DeviceEntry._ID));

            Device device = new Device();
            device.setId(deviceCursor.getLong(deviceCursor
                    .getColumnIndex(DeviceContract.DeviceEntry._ID)));
            device.setName(deviceCursor.getString(deviceCursor
                    .getColumnIndex(DeviceContract.DeviceEntry.COLUMN_NAME_NAME)));
            device.setIpAddr(deviceCursor.getInt(deviceCursor
                    .getColumnIndex(DeviceContract.DeviceEntry.COLUMN_NAME_IP)));
            device.setSyncTime(deviceCursor.getInt(deviceCursor
                    .getColumnIndex(DeviceContract.DeviceEntry.COLUMN_NAME_SYNC_TIME)));

            device.setLog(getLog(deviceId));

            devices.add(device);
        }

        deviceCursor.close();

        return (devices);
    }

}