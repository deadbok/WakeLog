package net.groenholdt.wakelog.protocol;

import android.util.Log;

import net.groenholdt.wakelog.model.LogEntry;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

/**
 * Created by oblivion on 10/04/16.
 * <p/>
 * Device communication using JSON over WebSockets.
 */
public class JSONWebSocket
{
    private static final String TAG = "JSONWebSocket";
    private static JSONWebSocketListener listener;
    private final WebSocketConnection webSocketConnection = new WebSocketConnection();
    private URI uri;

    public JSONWebSocket(InetAddress address, int port,
                         JSONWebSocketListener listener)
    {
        Log.d(TAG, "Creating WebSocket connection.");

        JSONWebSocket.listener = listener;

        try
        {
            uri = new URI("ws://" + address.getHostAddress() + ":" +
                          String.valueOf(port) + "/ws");
        }
        catch (URISyntaxException e)
        {
            return;
        }
        Log.i(TAG, "URI: " + uri.toString());

        try
        {
            webSocketConnection.connect(uri.toString(), new WebSocketHandler()
            {
                @Override
                public void onOpen()
                {
                    Log.d(TAG, "WebSocket opened.");
                    JSONWebSocket.listener.onOpen();
                }

                @Override
                public void onTextMessage(String payload)
                {
                    Log.d(TAG, "WebSocket message: " + payload);
                    try
                    {
                        JSONArray jsonLog = new JSONArray(payload);
                        for (int i = 0; i < jsonLog.length(); i++)
                        {
                            Log.d(TAG, "Adding entry: " + jsonLog.getLong(i));

                            LogEntry entry = new LogEntry();
                            entry.setTime((int) jsonLog.getLong(i));
                            JSONWebSocket.listener.onLogEntry(entry);
                        }
                    }
                    catch (JSONException e)
                    {
                        Log.e(TAG, e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason)
                {
                    Log.d(TAG, "WebSocket closed: " + reason);
                }
            });
        }
        catch (WebSocketException e)
        {

            Log.d(TAG, e.toString());
        }
    }

    public void getLog()
    {
        if (isOpen())
        {
            Log.i(TAG, "Downloading log from " + uri.toString());
            webSocketConnection.sendTextMessage("getlog");
        }
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isOpen()
    {
        return (webSocketConnection.isConnected());
    }

    public void close()
    {
        webSocketConnection.disconnect();
    }

    public interface JSONWebSocketListener
    {
        void onOpen();

        void onLogEntry(LogEntry entry);
    }
}
