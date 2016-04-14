package net.groenholdt.wakelog;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import net.groenholdt.wakelog.model.Device;
import net.groenholdt.wakelog.model.LogDatabaseHelper;
import net.groenholdt.wakelog.protocol.DeviceDiscover;
import net.groenholdt.wakelog.protocol.DeviceDiscoverListener;
import net.groenholdt.wakelog.protocol.JSONWebSocket;

import java.net.InetAddress;

public class LogActivity extends AppCompatActivity implements DeviceDiscoverListener
{
    private static final String TAG = "LogActivity";
    private LogDatabaseHelper database;
    private SimpleCursorAdapter logAdapter;
    private Device device;
    private DeviceDiscover discoverer;

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
                database.getLogCursor(device.getId()),
                new String[]{"time"},
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

            }
        });
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
        JSONWebSocket ws;

        Toast.makeText(this,
                device.getName() + " found.", Toast.LENGTH_LONG).show();

        ws = new JSONWebSocket();
        ws.getLog(addr, port);
    }

    public void onResolveFailed()
    {
        Toast.makeText(this,
                device.getName() + " not found.", Toast.LENGTH_SHORT);
    }
}
