package net.groenholdt.wakelog.protocol;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by oblivion on 10/04/16.
 */
public class JSONWebSocket
{
    public static final String TAG = "JSONWebSocket";

    protected WebSocketClient webSocketClient;

    public void getLog(InetAddress addr, int port)
    {
        URI uri;
        try
        {
            uri = new URI("ws://" + addr.toString() + ":" +
                    String.valueOf(port));
        }
        catch (URISyntaxException e)
        {
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
                final String message = s;
                Log.d(TAG, "WebSocket message: " + s);
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
