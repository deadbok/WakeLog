package net.groenholdt.wakelog;

import android.content.Intent;
import android.net.nsd.NsdManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

import net.groenholdt.wakelog.NsdHelper;

public class MainActivity extends AppCompatActivity
{
    NsdHelper mNsdHelper;

    private static final String TAG = "MainActivity";
    private WebSocketClient mWebSocketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();
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
            connectWebSocket();
        }

        return super.onOptionsItemSelected(item);
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

        mWebSocketClient = new WebSocketClient(uri)
        {
            @Override
            public void onOpen(ServerHandshake serverHandshake)
            {
                Log.d(TAG, "WebSocket opened");
            }

            @Override
            public void onMessage(String s)
            {
                final String message = s;
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
        mWebSocketClient.connect();
    }
}
