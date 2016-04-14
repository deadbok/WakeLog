package net.groenholdt.wakelog.model;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;

public class LogDatabaseProvider extends ContentProvider
{
    public static final String TAG = "LogDatabaseProvider";
    public static final String SCHEME = "content://";
    public static final String AUTHORITY = "net.groenholdt.wakelog.provider";

    public static final String PATH_DEVICES = "devices";
    public static final String PATH_DEVICE = "devices/#";
    public static final String PATH_DEVICE_NAME = "devices/#/name";
    public static final String PATH_DEVICE_IP = "devices/#/ip";
    public static final String PATH_DEVICE_SYNC_TIME = "devices/#/sync_time";
    public static final Uri DEVICE_URI =
            Uri.parse(SCHEME + AUTHORITY + "/" + PATH_DEVICES);

    public static final String PATH_LOGS = "logs";
    public static final String PATH_LOG = "logs/#";
    public static final String PATH_LOG_TIME = "logs/#/time";
    public static final String PATH_LOG_TYPE = "logs/#/type";
    public static final String PATH_LOG_DEVICE = "devices/#/device";
    public static final Uri LOG_URI =
            Uri.parse(SCHEME + AUTHORITY + "/" + PATH_LOGS);

    public static final int URI_CODE_DEVICES = 1;
    public static final int URI_CODE_DEVICE = 2;
    public static final int URI_CODE_DEVICE_NAME = 3;
    public static final int URI_CODE_DEVICE_IP = 4;
    public static final int URI_CODE_DEVICE_SYNC_TIME = 5;

    public static final int URI_CODE_LOGS = 5;
    public static final int URI_CODE_LOG = 6;
    public static final int URI_CODE_LOG_TIME = 7;
    public static final int URI_CODE_LOG_TYPE = 8;
    public static final int URI_CODE_LOG_DEVICE = 9;
    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);
    private static HashMap<String, String> values;

    static
    {
        uriMatcher.addURI(AUTHORITY, PATH_DEVICES, URI_CODE_DEVICES);
        uriMatcher.addURI(AUTHORITY, PATH_DEVICE, URI_CODE_DEVICE);
        //uriMatcher.addURI(AUTHORITY, PATH_DEVICE_NAME, URI_CODE_DEVICE_NAME);
        //uriMatcher.addURI(AUTHORITY, PATH_DEVICE_IP, URI_CODE_DEVICE_IP);
        //uriMatcher.addURI(AUTHORITY, PATH_DEVICE_SYNC_TIME, URI_CODE_DEVICE_SYNC_TIME);

        uriMatcher.addURI(AUTHORITY, PATH_LOGS, URI_CODE_LOGS);
        uriMatcher.addURI(AUTHORITY, PATH_LOG, URI_CODE_LOG);
        //uriMatcher.addURI(AUTHORITY, PATH_LOG_TIME, URI_CODE_LOG_TIME);
        //uriMatcher.addURI(AUTHORITY, PATH_LOG_TYPE, URI_CODE_LOG_TYPE);
        //uriMatcher.addURI(AUTHORITY, PATH_LOG_DEVICE, URI_CODE_LOG_DEVICE);
    }

    private LogDatabaseHelper dbHelper;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri)
    {
        int match = uriMatcher.match(uri);
        switch (match)
        {
            case URI_CODE_DEVICES:
                return ("vnd.android.cursor.dir/vnd." + AUTHORITY + ".devices");
            case URI_CODE_DEVICE:
                return ("vnd.android.cursor.item/vnd." + AUTHORITY + ".device");
            case URI_CODE_LOGS:
                return ("vnd.android.cursor.dir/vnd." + AUTHORITY + ".logs");
            case URI_CODE_LOG:
                return ("vnd.android.cursor.item/vnd." + AUTHORITY + ".log");
            default:
                return ("");
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long id;

        int match = uriMatcher.match(uri);
        switch (match)
        {
            case URI_CODE_DEVICES:
                values.put(DeviceContract.DeviceEntry.COLUMN_NAME_IP, 0);
                values.put(DeviceContract.DeviceEntry.COLUMN_NAME_SYNC_TIME, 0);
                id = database.insert(DeviceContract.TABLE_NAME, null, values);
                getContext().getContentResolver()
                        .notifyChange(DEVICE_URI, null);
                Log.d(TAG, "Added device with id: " + String.valueOf(id));
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI :" + uri.toString());
        }
        return (ContentUris.withAppendedId(DEVICE_URI, id));
    }

    @Override
    public boolean onCreate()
    {
        dbHelper = new LogDatabaseHelper(getContext());
        return (dbHelper != null);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder
    )
    {
        Log.d(TAG, "Query: " + uri.toString());
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        int match = uriMatcher.match(uri);
        switch (match)
        {
            case URI_CODE_DEVICES:
                qBuilder.setTables(DeviceContract.TABLE_NAME);
                break;
            case URI_CODE_DEVICE:
                qBuilder.setTables(DeviceContract.TABLE_NAME);
                //TODO sanitise input.
                //http://developer.android.com/guide/topics/providers/content-provider-basics.html#Injection
                qBuilder.appendWhere(DeviceContract.DeviceEntry._ID + "=" +
                        uri.getFragment());
                Log.d(TAG, "ID: " + uri.getFragment());
                break;
            case URI_CODE_LOGS:
                qBuilder.setTables(LogContract.TABLE_NAME);
                break;
            case URI_CODE_LOG:
                qBuilder.setTables(LogContract.TABLE_NAME);
                //TODO sanitise input.
                qBuilder.appendWhere(
                        LogContract.LogEntry._ID + "=" + uri.getFragment());
                Log.d(TAG, "ID: " + uri.getFragment());
                break;
        }

        // Make the query.
        Cursor cursor = qBuilder.query(database,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs
    )
    {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
