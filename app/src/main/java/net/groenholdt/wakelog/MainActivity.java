package net.groenholdt.wakelog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import net.groenholdt.wakelog.model.LogDatabaseHelper;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity implements AddDeviceDialogFragment.AddDeviceDialogListener
{
    private static final String TAG = "MainActivity";
    private WebSocketClient webSocketClient;
    private NsdHelper nsdHelper;
    private FloatingActionButton fab;
    private LogDatabaseHelper database;
    private SimpleCursorAdapter deviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        database = new LogDatabaseHelper(this);

        deviceAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                database.getDeviceCursor(),
                new String[]{"name"},
                new int[]{android.R.id.text1},
                0);

        ListView listView = (ListView) findViewById(R.id.deviceView);
        listView.setAdapter(deviceAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
            {
                Intent intent =
                        new Intent(MainActivity.this, LogActivity.class);
                intent.putExtra("device_id", id);

                Log.d(TAG, "Showing device with id: " + String.valueOf(id));

                startActivity(intent);
            }
        });

        FloatingActionButton fab =
                (FloatingActionButton) findViewById(R.id.fab_add_device);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DialogFragment addDeviceDialog =
                        new AddDeviceDialogFragment();
                addDeviceDialog
                        .show(getSupportFragmentManager(), "add_device");
                deviceAdapter.notifyDataSetChanged();
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
    public void onDialogPositiveClick(DialogFragment dialog)
    {
        Dialog d = dialog.getDialog();
        EditText nameEdit = (EditText) d.findViewById(R.id.nameEditText);
        if (nameEdit != null)
        {
            String name = nameEdit.getText().toString();

            Log.d(TAG, "Adding device " + name);
            database.addDevice(name, 0, 0);
        }
        else
        {
            Log.e(TAG, "Could not get device name from UI.");
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog)
    {
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
}
