/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Modified 2016 by Martin Bo Kristensen Grønholdt.
 *
 * Remove unneeded registration logic and adapted a litle..
 */

package net.groenholdt.wakelog.protocol;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.net.InetAddress;

public class DeviceDiscover implements NsdManager.ResolveListener
{
    public static final String TAG = "DeviceDiscover";
    public static final String SERVICE_TYPE = "_iot._tcp.";
    public String SERVICE_NAME = "localhost";
    protected NsdManager.DiscoveryListener discoveryListener;
    protected NsdServiceInfo service;
    protected DeviceDiscoverListener listener;
    protected InetAddress address = null;
    protected boolean discovering = false;
    protected int port = 0;
    private Context context;
    private NsdManager nsdManager;

    public DeviceDiscover(Context context, String hostname, DeviceDiscoverListener listener)
    {
        Log.d(TAG, "Creating discoverer: " + hostname);
        this.context = context;
        nsdManager =
                (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        SERVICE_NAME = hostname;
        this.listener = listener;

        initialiseDiscoveryListener();
    }

    protected void initialiseDiscoveryListener()
    {
        discoveryListener = new NsdManager.DiscoveryListener()
        {

            @Override
            public void onDiscoveryStarted(String regType)
            {
                Log.d(TAG, "Service discovery started");
                discovering = true;
            }

            @Override
            public void onServiceFound(NsdServiceInfo service)
            {
                Log.i(TAG, "Service discovery success.");
                Log.d(TAG, "Service Type: " + service.getServiceType());
                Log.d(TAG, "Name: " + service.getServiceName());

                if (service.getServiceType().equals(SERVICE_TYPE))
                {
                    if (service.getServiceName().equals(SERVICE_NAME)) {
                        DeviceDiscover.this.service = service;
                        DeviceDiscover.this.stop();
                        Log.i(TAG, "Found reqeusted service.");
                        nsdManager.resolveService(service, DeviceDiscover.this);
                    }
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service)
            {
                Log.e(TAG, "Service lost.");
                if (DeviceDiscover.this.service == service)
                {
                    DeviceDiscover.this.service = null;
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType)
            {
                Log.i(TAG, "Discovery stopped: " + serviceType);
                discovering = false;
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode)
            {
                Log.e(TAG, "Discovery failed: " + errorCode);
                listener.onResolveFailed();
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode)
            {
                Log.e(TAG, "Discovery failed: " + errorCode);
            }
        };
    }

    @Override
    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode)
    {
        Log.e(TAG, "Resolve failed: " + errorCode);
        listener.onResolveFailed();
    }

    @Override
    public void onServiceResolved(NsdServiceInfo serviceInfo)
    {
        Log.i(TAG, "Resolve Succeeded. " + serviceInfo);
        Log.d(TAG, "Name: " + serviceInfo.getServiceName());
        Log.d(TAG, "Type: " + serviceInfo.getServiceType());
        Log.d(TAG, "Host: " + serviceInfo.getHost().toString());

        service = serviceInfo;

        address = serviceInfo.getHost();
        port = serviceInfo.getPort();

        listener.onResolved(address, port);
    }

    public NsdServiceInfo getServiceInfo()
    {
        return service;
    }

    public void start()
    {
        nsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    public void stop()
    {
        if (discovering) {
            nsdManager.stopServiceDiscovery(discoveryListener);
        }
    }

    public InetAddress getIP()
    {
        return (address);
    }

    public int getPort()
    {
        return (port);
    }
}
