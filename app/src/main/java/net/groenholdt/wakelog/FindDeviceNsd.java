/**
 *
 * Created by oblivion on 25/03/16.
 */
package net.groenholdt.wakelog;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

public class FindDeviceNsd implements NsdManager.DiscoveryListener, NsdManager.ResolveListener
{

    private static final String TAG = "FindDeviceNsd";
    private NsdManager   manager;
    private String       serviceType;

    public FindDeviceNsd(NsdManager theManager, String theServiceType)
    {
        manager     = theManager;
        serviceType = theServiceType;
    }

    @Override
    public void onDiscoveryStarted(String theServiceType)
    {
        Log.d(TAG, "onDiscoveryStarted");
    }

    @Override
    public void onStartDiscoveryFailed(String theServiceType, int theErrorCode)
    {
        Log.d(TAG, "onStartDiscoveryFailed(" + theServiceType + ", " + theErrorCode);
    }

    @Override
    public void onDiscoveryStopped(String serviceType)
    {
        Log.d(TAG, "onDiscoveryStopped");
    }

    @Override
    public void onStopDiscoveryFailed(String theServiceType, int theErrorCode)
    {
        Log.d(TAG, "onStartDiscoveryFailed(" + theServiceType + ", " + theErrorCode);
    }

    @Override
    public void onServiceFound(NsdServiceInfo theServiceInfo)
    {
        Log.d(TAG, "onServiceFound(" + theServiceInfo + ")");
        Log.d(TAG, "name == " + theServiceInfo.getServiceName());
        Log.d(TAG, "type == " + theServiceInfo.getServiceType());
        serviceFound(theServiceInfo);
    }

    // Resolve Listener

    @Override
    public void onServiceLost(NsdServiceInfo theServiceInfo)
    {
        Log.d(TAG, "onServiceLost(" + theServiceInfo + ")");
    }

    @Override
    public void onServiceResolved(NsdServiceInfo theServiceInfo)
    {
        Log.d(TAG, "onServiceResolved(" + theServiceInfo + ")");
        Log.d(TAG, "name == " + theServiceInfo.getServiceName());
        Log.d(TAG, "type == " + theServiceInfo.getServiceType());
        Log.d(TAG, "host == " + theServiceInfo.getHost());
        Log.d(TAG, "port == " + theServiceInfo.getPort());
    }

    //

    @Override
    public void onResolveFailed(NsdServiceInfo theServiceInfo, int theErrorCode)
    {
        Log.d(TAG, "onResolveFailed(" + theServiceInfo + ", " + theErrorCode);
    }

    void run()
    {
        manager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, this);
    }

    private void serviceFound(NsdServiceInfo theServiceInfo)
    {
        manager.resolveService(theServiceInfo, this);
    }
}

