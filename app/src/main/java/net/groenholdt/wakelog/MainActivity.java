package net.groenholdt.wakelog;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;

import net.groenholdt.wakelog.model.Device;
import net.groenholdt.wakelog.model.LogDatabaseHelper;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    private WebSocketClient webSocketClient;
    private NsdHelper nsdHelper;
    private FloatingActionButton fab;
    private LogDatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        database = new LogDatabaseHelper(this);

        populateTabHost();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DialogFragment addDeviceDialogFragment =
                        new AddDeviceDialogFragment();
                addDeviceDialogFragment
                        .show(getSupportFragmentManager(), "add_device");
            }
        });

        nsdHelper = new NsdHelper(this);
        nsdHelper.initializeNsd();
    }

    @Override
    protected void onPause()
    {
        if (nsdHelper != null)
        {
            nsdHelper.stopDiscovery();
        }
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (nsdHelper != null)
        {
            nsdHelper.discoverServices();
        }
    }

    @Override
    protected void onDestroy()
    {
        nsdHelper.tearDown();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_devices:
                Intent intent = new Intent(this, DevicesActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void connectWebSocket()
    {
        URI uri;
        try
        {
            uri = new URI("ws://wakelog.local:80");
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(uri)
        {
            @Override
            public void onOpen(ServerHandshake serverHandshake)
            {
                Log.d(TAG, "WebSocket opened");
            }

            @Override
            public void onMessage(String s)
            {
                //final String message = s;
                Log.d(TAG, "WebSocket message: " + s);

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        /*TextView textView = (TextView) findViewById(R.id.messages);
                        textView.setText(textView.getText() + "\n" + message);*/
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b)
            {
                Log.d(TAG, "WebSocket closed " + s);
            }

            @Override
            public void onError(Exception e)
            {
                Log.e("Websocket", "Error " + e.getMessage());
            }
        };
        webSocketClient.connect();
    }

    private void populateTabHost()
    {
        Log.d(TAG, "Populating tabs");
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        ArrayList<Device> devices = database.getDevices();

        for (int i = 0; i < devices.size(); i++)
        {
            tabHost.addTab(tabHost.newTabSpec("tab_test1")
                    .setIndicator(devices.get(i).getName()));
        }

    }
}
