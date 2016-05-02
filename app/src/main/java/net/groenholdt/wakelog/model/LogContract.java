package net.groenholdt.wakelog.model;

import android.provider.BaseColumns;

/**
 * Created by oblivion on 05/04/16.
 * <p/>
 * Great tutorial at: http://blog.cindypotvin.com/saving-to-a-sqlite-database-in-your-android-application/
 */
public final class LogContract
{
    public static final String TABLE_NAME = "log";

    public static final String SQL_CREATE_TABLE = "CREATE TABLE "
                                                  + LogContract.TABLE_NAME + " ("
                                                  + LogEntry._ID +
                                                  " INTEGER PRIMARY KEY AUTOINCREMENT,"
                                                  + LogEntry.COLUMN_NAME_TIME +
                                                  " INTEGER DEFAULT 0,"
                                                  + LogEntry.COLUMN_NAME_TYPE +
                                                  " INTEGER DEFAULT 0,"
                                                  + LogEntry.COLUMN_NAME_DEVICE + " INTEGER,"
                                                  + "FOREIGN KEY (" + LogEntry.COLUMN_NAME_DEVICE +
                                                  ") "
                                                  + "REFERENCES devices(" +
                                                  DeviceContract.DeviceEntry._ID + "));";

    public static abstract class LogEntry implements BaseColumns
    {
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_DEVICE = "device";
    }
}
