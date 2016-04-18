package net.groenholdt.wakelog;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
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
import net.groenholdt.wakelog.model.LogContract;
import net.groenholdt.wakelog.model.LogDatabaseHelper;
import net.groenholdt.wakelog.model.LogDatabaseProvider;
import net.groenholdt.wakelog.model.LogEntry;
import net.groenholdt.wakelog.protocol.DeviceDiscover;
import net.groenholdt.wakelog.protocol.DeviceDiscoverListener;
import net.groenholdt.wakelog.protocol.JSONWebSocket;
import net.groenholdt.wakelog.protocol.LogEntryLisentener;

import java.net.InetAddress;

public class LogActivity extends AppCompatActivity implements DeviceDiscoverListener, LogEntryLisentener
{
    private static final String TAG = "LogActivity";
    private static final int LOG_LOADER_ID = 2;
    private LogDatabaseHelper database;
    private SimpleCursorAdapter logAdapter;
    private Device device;
    private DeviceDiscover discoverer;
    private JSONWebSocket ws;

    private LoaderManager.LoaderCallbacks<Cursor> logLoader =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                // Create and return the actual cursor loader for the contacts data
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    // Define the columns to retrieve
                    String[] projection =
                            {LogContract.LogEntry._ID, LogContract.LogEntry.COLUMN_NAME_TIME, LogContract.LogEntry.COLUMN_NAME_TYPE};
                    // Construct the loader
                    Log.d(TAG, "Creating loader for device id: " +
                            String.valueOf(args.getLong("device_id", 0)));
                    CursorLoader cursorLoader =
                            new CursorLoader(LogActivity.this,
                                    LogDatabaseProvider.URI_LOG,
                                    projection,
                                    LogContract.LogEntry.COLUMN_NAME_DEVICE + " = " + args.getLong("device_id", 0),
                                    null,
                                    null
                            );

                    return cursorLoader;
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                    logAdapter.swapCursor(cursor);
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
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
                android.R.layout.simple_list_item_1,
                null,
                new String[]{LogContract.LogEntry.COLUMN_NAME_TIME},
                new int[]{android.R.id.text1},
                0);

        ListView listView = (ListView) findViewById(R.id.logView);
        if (logAdapter != null)
        {
            listView.setAdapter(logAdapter);
        }

        discoverer = new DeviceDiscover(this, device.getName(), this);
        discoverer.start();

        FloatingActionButton fab =
                (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (ws == null) {
                    Toast.makeText(LogActivity.this, "Not connected.", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (ws.isOpen()) {
                    ws.getLog();
                } else {
                    Toast.makeText(LogActivity.this, "Not connected.", Toast.LENGTH_SHORT)
                            .show();
                }
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
                //TODO use loader
                database.removeDevice(device.getId());
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
        discoverer.stop();
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        discoverer.start();
    }

    public void onResolved(InetAddress addr, int port)
    {
        Toast.makeText(this,
                device.getName() + " found.", Toast.LENGTH_LONG).show();

        ws = new JSONWebSocket(this, addr, port, this);
        if (ws.isOpen()) {
            ws.getLog();
        }
    }

    public void onResolveFailed()
    {
        Toast.makeText(this,
                device.getName() + " not found.", Toast.LENGTH_SHORT);
    }

    public void onLogEntry(LogEntry entry) {
        Log.d(TAG, "Adding log entry to database: " + entry.toString());
        try {
            ContentValues logValues = new ContentValues();

            logValues
                    .put(LogContract.LogEntry.COLUMN_NAME_TIME, entry.getTime());
            logValues.put(LogContract.LogEntry.COLUMN_NAME_TYPE, entry.getType());
            logValues
                    .put(LogContract.LogEntry.COLUMN_NAME_DEVICE, getIntent().getLongExtra("device_id", 0));

            getContentResolver()
                    .insert(LogDatabaseProvider.URI_LOG, logValues);
        } catch (SQLException e) {
        }
    }
}
