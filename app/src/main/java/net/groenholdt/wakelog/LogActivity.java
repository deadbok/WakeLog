package net.groenholdt.wakelog;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import net.groenholdt.wakelog.model.Device;
import net.groenholdt.wakelog.model.DeviceContract;
import net.groenholdt.wakelog.model.LogContract;
import net.groenholdt.wakelog.model.LogDatabaseHelper;
import net.groenholdt.wakelog.model.LogDatabaseProvider;
import net.groenholdt.wakelog.model.LogEntry;
import net.groenholdt.wakelog.protocol.DeviceDiscover;
import net.groenholdt.wakelog.protocol.JSONWebSocket;

import java.net.InetAddress;

public class LogActivity extends AppCompatActivity
        implements DeviceDiscover.DeviceDiscoverListener, JSONWebSocket.JSONWebSocketListener
{
    private static final String TAG = "LogActivity";
    private static final int LOG_LOADER_ID = 2;
    private SimpleCursorAdapter logAdapter;
    private final LoaderManager.LoaderCallbacks<Cursor> logLoader =
            new LoaderManager.LoaderCallbacks<Cursor>()
            {
                private static final String TAG = "LogActivity.logLoader";

                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args)
                {
                    Log.d(TAG, "Creating loader with ID: " + id);
                    //Check if we know the loader ID.
                    switch (id)
                    {
                        case LOG_LOADER_ID:
                            Log.d(TAG, "Creating log group loader.");

                            // Define the columns to retrieve
                            String[] projection =
                                    {LogContract.LogEntry._ID,
                                     LogContract.LogEntry.COLUMN_NAME_TIME,
                                     LogContract.LogEntry.COLUMN_NAME_TYPE,
                                     LogContract.LogEntry.COLUMN_NAME_DEVICE};
                            // Construct the loader
                            Log.d(TAG, "Creating loader for device id: " +
                                       String.valueOf(args.getLong("device_id", 0)));

                            return new CursorLoader(LogActivity.this,
                                                    LogDatabaseProvider.URI_LOG,
                                                    projection,
                                                    LogContract.LogEntry.COLUMN_NAME_DEVICE +
                                                    " = " +
                                                    args.getLong("device_id", 0),
                                                    null,
                                                    LogContract.LogEntry.COLUMN_NAME_TIME);
                        default:
                            Log.e(TAG, "Unknown loader ID");
                            return null;
                    }
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
                {
                    //Use the new cursor.
                    logAdapter.swapCursor(cursor);
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader)
                {
                    logAdapter.swapCursor(null);

                }
            };
    private Device device;
    private DeviceDiscover discoverer;
    private JSONWebSocket ws;
    private CountDownTimer timeoutTimer;
    private ProgressDialog progressDialog;

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

        LogDatabaseHelper dbHelper = new LogDatabaseHelper(this);
        device = dbHelper.getDevice(getIntent().getLongExtra("device_id", 0));

        logAdapter = new SimpleCursorAdapter(this,
                                             R.layout.log_list_item,
                                             null,
                                             new String[]{LogContract.LogEntry.COLUMN_NAME_TIME,
                                                          LogContract.LogEntry.COLUMN_NAME_TYPE},
                                             new int[]{R.id.log_time, R.id.log_type},
                                             0);

        logAdapter.setViewBinder(new LogView());

        ListView listView = (ListView) findViewById(R.id.logView);
        if (listView == null)
        {
            Log.e(TAG, "No ListView.");
            throw new AndroidRuntimeException("ListViewNull",
                                              new Throwable("Could not find list view for logs."));
        }
        listView.setAdapter(logAdapter);

        //Count down to connection timeout.
        timeoutTimer = new CountDownTimer(10000, 1000)
        {

            public void onTick(long millisUntilFinished)
            {
                Log.d(TAG, "Update in progress, seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish()
            {
                Log.e(TAG, "Update timed out.");
                progressDialog.dismiss();
                Toast.makeText(LogActivity.this,
                               "Connection to " + device.getName() + " timed out.",
                               Toast.LENGTH_SHORT).show();
                discoverer.stop();
                if (ws != null)
                {
                    if (ws.isOpen())
                    {
                        ws.close();
                    }
                }
            }
        };

        //Progress dialog used when syncing.
        progressDialog = new ProgressDialog(LogActivity.this);


        //FAB to update log entries.
        FloatingActionButton fab =
                (FloatingActionButton) findViewById(R.id.fab);
        View.OnClickListener clickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Refresh pressed");
                if (LogActivity.this.discoverer == null)
                {
                    LogActivity.this.discoverer =
                            new DeviceDiscover(LogActivity.this, device.getName(),
                                               LogActivity.this);
                }
                LogActivity.this.timeoutTimer.start();
                LogActivity.this.discoverer.start();

                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage("Connecting to " + device.getName());
                progressDialog.show();
            }
        };

        if (fab == null)
        {
            Log.e(TAG, "No fab.");
            throw new AndroidRuntimeException("FabNull", new Throwable(
                    "Could not create button for adding device."));
        }
        fab.setOnClickListener(clickListener);

        //Get a loader.
        Loader<Cursor> loader = getSupportLoaderManager().getLoader(LOG_LOADER_ID);
        if (loader == null)
        {
            //Create a new loader.
            getSupportLoaderManager().initLoader(LOG_LOADER_ID, getIntent().getExtras(), logLoader);
        }
        else if (!loader.isReset())
        {
            //Restart an existing loader.
            getSupportLoaderManager()
                    .restartLoader(LOG_LOADER_ID, getIntent().getExtras(), logLoader);
        }
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
        if (ws != null)
        {
            ws.close();
        }
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        Log.d(TAG, "Resuming.");
        super.onResume();
    }

    public void onResolved(InetAddress address, int port)
    {
        Log.d(TAG, "Device resolved: " + address.toString() + ":" + port);
        Toast.makeText(this,
                       device.getName() + " found.", Toast.LENGTH_LONG).show();

        discoverer.stop();

        ws = new JSONWebSocket(address, port, this);
    }

    public void onResolveFailed()
    {
        Log.d(TAG, "Resolve failed.");

        //Stop the spinner and timeout timer.
        progressDialog.dismiss();
        timeoutTimer.cancel();

        Toast.makeText(this,
                       device.getName() + " not found.", Toast.LENGTH_SHORT).show();
    }

    public void onOpen()
    {
        Log.i(TAG, "Getting new logs.");
        ws.getLog();
    }

    public void onEntries(int entries)
    {
        Log.d(TAG, "Start of " + entries + " entries.");

        //Cancel timeout.
        timeoutTimer.cancel();
        if (entries == 0)
        {
            progressDialog.dismiss();
            Toast.makeText(this,
                           device.getName() + " has no new log entries.", Toast.LENGTH_SHORT)
                 .show();
        }
        else
        {
            progressDialog.setMessage("Downloading " + entries);
            progressDialog.show();
        }
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

    public void onEntriesEnd()
    {
        //Cancel timeout.
        timeoutTimer.cancel();
        //Dismiss the progress bar.
        progressDialog.dismiss();
    }
}
