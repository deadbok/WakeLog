package net.groenholdt.wakelog;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    NsdHelper mNsdHelper;
    private WebSocketClient webSocketClient;
    private NsdHelper nsdHelper;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Click action
                //Intent intent = new Intent(MainActivity.this, NewMessageActivity.class);
                //startActivity(intent);
            }
        });

        nsdHelper = new NsdHelper(this);
        nsdHelper.initializeNsd();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        if (id == R.id.action_update)
        {
            Log.d(TAG, "Update action.");
            nsdHelper.discoverServices();
            connectWebSocket();
        }
        if (id == R.id.action_mdns)
        {
            Log.d(TAG, "MDNS action.");
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    private void connectWebSocket()
    {
        URI uri;
        try
        {
            uri = new URI("ws://wakelog.local:80");
        } catch (URISyntaxException e)
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
