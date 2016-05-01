package net.groenholdt.wakelog;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import net.groenholdt.wakelog.model.Device;
import net.groenholdt.wakelog.model.DeviceContract;
import net.groenholdt.wakelog.model.LogContract;
import net.groenholdt.wakelog.model.LogDatabaseHelper;
import net.groenholdt.wakelog.model.LogDatabaseProvider;
import net.groenholdt.wakelog.model.LogEntry;
import net.groenholdt.wakelog.protocol.DeviceDiscover;
import net.groenholdt.wakelog.protocol.DeviceDiscoverListener;
import net.groenholdt.wakelog.protocol.JSONWebSockerLisentener;
import net.groenholdt.wakelog.protocol.JSONWebSocket;

import java.net.InetAddress;

public class LogActivity extends AppCompatActivity
        implements DeviceDiscoverListener, JSONWebSockerLisentener
{
    private static final String TAG = "LogActivity";
    private static final int LOG_LOADER_ID = 2;
    private LogDatabaseHelper database;
    private SimpleCursorAdapter logAdapter;
    private Device device;
    private DeviceDiscover discoverer;
    private JSONWebSocket ws;

    private LoaderManager.LoaderCallbacks<Cursor> logLoader =
            new LoaderManager.LoaderCallbacks<Cursor>()
            {
                // Create and return the actual cursor loader for the contacts data
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args)
                {
                    // Define the columns to retrieve
                    String[] projection =
                            {LogContract.LogEntry._ID, LogContract.LogEntry.COLUMN_NAME_TIME,
                             LogContract.LogEntry.COLUMN_NAME_TYPE,
                             LogContract.LogEntry.COLUMN_NAME_DEVICE};
                    // Construct the loader
                    Log.d(TAG, "Creating loader for device id: " +
                               String.valueOf(args.getLong("device_id", 0)));
                    CursorLoader cursorLoader =
                            new CursorLoader(LogActivity.this,
                                             LogDatabaseProvider.URI_LOG,
                                             projection,
                                             LogContract.LogEntry.COLUMN_NAME_DEVICE + " = " +
                                             args.getLong("device_id", 0),
                                             null,
                                             null
                            );

                    return cursorLoader;
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
                {
                    logAdapter.swapCursor(cursor);
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader)
                {
                    logAdapter.swapCursor(null);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        database = new LogDatabaseHelper(this);
        device = database.getDevice(getIntent().getLongExtra("device_id", 0));

        logAdapter = new SimpleCursorAdapter(this,
                                             R.layout.log_list_item,
                                             null,
                                             new String[]{LogContract.LogEntry.COLUMN_NAME_TIME,
                                                          LogContract.LogEntry.COLUMN_NAME_TYPE},
                                             new int[]{R.id.log_time, R.id.log_type},
                                             0);

        logAdapter.setViewBinder(new LogView());

        ListView listView = (ListView) findViewById(R.id.logView);
        if (logAdapter != null)
        {
            listView.setAdapter(logAdapter);
        }


        FloatingActionButton fab =
                (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (LogActivity.this.discoverer == null)
                {
                    LogActivity.this.discoverer =
                            new DeviceDiscover(LogActivity.this, device.getName(),
                                               LogActivity.this);
                }
                LogActivity.this.discoverer.start();
            }
        });

        getSupportLoaderManager()
                .initLoader(LOG_LOADER_ID, getIntent().getExtras(), logLoader);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_log, menu);
        return (true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.device_delete:
                Uri uri =
                        ContentUris.withAppendedId(LogDatabaseProvider.URI_DEVICE, device.getId());
                getContentResolver().delete(uri, null, null);
                //Close this log activity as the device is gone.
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG, "Pausing.");
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        Log.d(TAG, "Resuming.");
        super.onResume();
    }

    public void onResolved(InetAddress addr, int port)
    {
        Log.d(TAG, "Device resolved: " + addr.toString() + ":" + port);
        Toast.makeText(this,
                       device.getName() + " found.", Toast.LENGTH_LONG).show();

        discoverer.stop();

        ws = new JSONWebSocket(this, addr, port, this);
    }

    public void onResolveFailed()
    {
        Log.d(TAG, "Resolve failed.");
        Toast.makeText(this,
                       device.getName() + " not found.", Toast.LENGTH_SHORT).show();
    }

    public void onOpen()
    {
        Log.i(TAG, "Getting new logs.");
        ws.getLog();
    }

    public void onLogEntry(LogEntry entry)
    {
        Log.d(TAG, "Adding log entry to database: " + entry.toString());
        try
        {

            //Insert log entry.
            ContentValues logValues = new ContentValues();

            logValues
                    .put(LogContract.LogEntry.COLUMN_NAME_TIME, entry.getTime() * 1000);
            logValues.put(LogContract.LogEntry.COLUMN_NAME_TYPE, entry.getType());
            logValues
                    .put(LogContract.LogEntry.COLUMN_NAME_DEVICE, device.getId());

            getContentResolver()
                    .insert(LogDatabaseProvider.URI_LOG, logValues);

            //Update sync time.
            ContentValues deviceValues = new ContentValues();
            deviceValues.put(DeviceContract.DeviceEntry.COLUMN_NAME_SYNC_TIME,
                             System.currentTimeMillis());
            Uri uri = ContentUris.withAppendedId(LogDatabaseProvider.URI_DEVICE, device.getId());
            getContentResolver().update(uri, deviceValues, null, null);
        }
        catch (SQLException e)
        {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }
}
