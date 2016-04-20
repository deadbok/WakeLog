package net.groenholdt.wakelog.model;

import android.provider.BaseColumns;

/**
 * Created by oblivion on 03/04/16.
 * <p/>
 * Great tutorial at: http://blog.cindypotvin.com/saving-to-a-sqlite-database-in-your-android-application/
 */
public final class DeviceContract
{
    public static final String TABLE_NAME = "devices";

    public static final String SQL_CREATE_TABLE = "CREATE TABLE "
            + DeviceContract.TABLE_NAME + " ("
            + DeviceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DeviceEntry.COLUMN_NAME_NAME + " TEXT,"
            + DeviceEntry.COLUMN_NAME_IP + " INTEGER DEFAULT 0,"
            + DeviceEntry.COLUMN_NAME_SYNC_TIME + " INTEGER DEFAULT 0,"
            //+ DeviceEntry.COLUMN_NAME_LOG + " INTEGER,"
            + "FOREIGN KEY (" + DeviceEntry.COLUMN_NAME_LOG + ") "
            + "REFERENCES projects(" + LogContract.LogEntry._ID + "));";

    //BaseColumns supplies _ID and _COUNT entries.
    public static abstract class DeviceEntry implements BaseColumns
    {
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_IP = "ip_address";
        public static final String COLUMN_NAME_SYNC_TIME = "sync_time";
        //TODO Get rid of this column, it is not needed.
        public static final String COLUMN_NAME_LOG = "log";
    }
}
