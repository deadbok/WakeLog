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
    public static final Uri URI_DEVICE =
            Uri.parse(SCHEME + AUTHORITY + "/" + PATH_DEVICES);


    public static final String PATH_LOGS = "logs";
    public static final String PATH_LOG = "logs/#";
    public static final String PATH_DEVICE_LOGS = "logs/device/#";
    public static final Uri URI_LOG =
            Uri.parse(SCHEME + AUTHORITY + "/" + PATH_LOGS);
    public static final Uri URI_DEVICE_LOGS =
            Uri.parse(SCHEME + AUTHORITY + "/" + PATH_DEVICE_LOGS);

    public static final int URI_CODE_DEVICES = 1;
    public static final int URI_CODE_DEVICE = 2;
    public static final int URI_CODE_DEVICE_LOGS = 3;

    public static final int URI_CODE_LOGS = 4;
    public static final int URI_CODE_LOG = 5;

    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);
    private static HashMap<String, String> values;

    static
    {
        uriMatcher.addURI(AUTHORITY, PATH_DEVICES, URI_CODE_DEVICES);
        uriMatcher.addURI(AUTHORITY, PATH_DEVICE, URI_CODE_DEVICE);

        uriMatcher.addURI(AUTHORITY, PATH_LOGS, URI_CODE_LOGS);
        uriMatcher.addURI(AUTHORITY, PATH_LOG, URI_CODE_LOG);
    }

    private LogDatabaseHelper dbHelper;


    @Override
    public boolean onCreate()
    {
        dbHelper = new LogDatabaseHelper(getContext());
        return (dbHelper != null);
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
            case URI_CODE_DEVICE_LOGS:
                return ("vnd.android.cursor.dir/vnd." + AUTHORITY + ".devicelogs");
            case URI_CODE_LOGS:
                return ("vnd.android.cursor.dir/vnd." + AUTHORITY + ".logs");
            case URI_CODE_LOG:
                return ("vnd.android.cursor.item/vnd." + AUTHORITY + ".log");
            default:
                return ("");
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int count;
        String[] id = {};

        int match = uriMatcher.match(uri);
        switch (match) {
            case URI_CODE_DEVICES:
                count = database.delete(DeviceContract.TABLE_NAME, null, null);
                //Delete all logs while we're at it.
                database.delete(LogContract.TABLE_NAME, null, null);
                getContext().getContentResolver()
                        .notifyChange(URI_DEVICE, null);
                getContext().getContentResolver().notifyChange(URI_LOG, null);
                Log.d(TAG, "Deleted all devices and logs");
                break;
            case URI_CODE_DEVICE:
                id[0] = uri.getLastPathSegment();
                Log.d(TAG, "Deleting device with id: " + id);

                count = database.delete(DeviceContract.TABLE_NAME, "_id = ?", id);
                Log.d(TAG, "Deleted " + String.valueOf(count) + "device(s)");

                int logs = database.delete(DeviceContract.TABLE_NAME, LogContract.LogEntry.COLUMN_NAME_DEVICE + " = ?", id);
                Log.d(TAG, "Deleted " + String.valueOf(logs) + "logs(s)");
                getContext().getContentResolver()
                        .notifyChange(URI_DEVICE, null);
                getContext().getContentResolver().notifyChange(URI_LOG, null);
                break;
            case URI_CODE_DEVICE_LOGS:
                id[0] = uri.getLastPathSegment();
                Log.d(TAG, "Deleting all logs for device with id: " + id);

                count = database.delete(DeviceContract.TABLE_NAME, LogContract.LogEntry.COLUMN_NAME_DEVICE + " = ?", id);
                Log.d(TAG, "Deleted " + String.valueOf(count) + "logs(s)");
                getContext().getContentResolver().notifyChange(URI_LOG, null);
                break;
            case URI_CODE_LOGS:
                Log.d(TAG, "Delete all logs");
                count = database.delete(LogContract.TABLE_NAME, null, null);
                getContext().getContentResolver()
                        .notifyChange(URI_LOG, null);
                Log.d(TAG, "Deleted " + String.valueOf(count) + " logs");
                break;
            case URI_CODE_LOG:
                id[0] = uri.getLastPathSegment();
                Log.d(TAG, "Deleting log with id: " + id);

                count = database.delete(LogContract.TABLE_NAME, "_id = ?", id);
                Log.d(TAG, "Deleted " + String.valueOf(count) + "logs(s)");
                getContext().getContentResolver().notifyChange(URI_LOG, null);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI :" + uri.toString());
        }
        return (count);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
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
                        .notifyChange(URI_DEVICE, null);
                Log.d(TAG, "Added device with id: " + String.valueOf(id));
                break;
            case URI_CODE_LOGS:
                id = database.insert(LogContract.TABLE_NAME, null, values);
                getContext().getContentResolver()
                        .notifyChange(URI_LOG, null);
                Log.d(TAG, "Added log entry with id: " + String.valueOf(id));
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI :" + uri.toString());
        }
        return (ContentUris.withAppendedId(URI_DEVICE, id));
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
            case URI_CODE_DEVICE_LOGS:

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
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int count;
        String[] id = {};

        int match = uriMatcher.match(uri);
        switch (match) {
            case URI_CODE_DEVICE:
                id[0] = uri.getLastPathSegment();
                Log.d(TAG, "Updating device with id: " + id);

                count = database.update(DeviceContract.TABLE_NAME, values, "_id = ?", id);
                Log.d(TAG, "Updated " + String.valueOf(count) + "device(s)");
                getContext().getContentResolver()
                        .notifyChange(URI_DEVICE, null);
                break;
            case URI_CODE_LOG:
                id[0] = uri.getLastPathSegment();
                Log.d(TAG, "Updating log with id: " + id);

                count = database.update(LogContract.TABLE_NAME, values, "_id = ?", id);
                Log.d(TAG, "Updated " + String.valueOf(count) + "logs(s)");
                getContext().getContentResolver().notifyChange(URI_LOG, null);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI :" + uri.toString());
        }
        return (count);
    }
}
