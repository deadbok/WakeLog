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
 * Modified 2016 by Martin Bo Krsitensen Grønholdt.
 *
 * Remove unneeded registration logic.
 */

package net.groenholdt.wakelog;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

public class NsdHelper
{

    public static final String SERVICE_TYPE = "_iot._tcp.";
    public static final String TAG = "NsdHelper";
    public String mServiceName = "wakelog";
    public boolean running = false;
    Context mContext;
    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdServiceInfo mService;

    public NsdHelper(Context context)
    {
        mContext = context;
        mNsdManager =
                (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void initializeNsd()
    {
        initializeResolveListener();
        initializeDiscoveryListener();

        //mNsdManager.init(mContext.getMainLooper(), this);

    }

    public void initializeDiscoveryListener()
    {
        mDiscoveryListener = new NsdManager.DiscoveryListener()
        {

            @Override
            public void onDiscoveryStarted(String regType)
            {
                Log.d(TAG, "Service discovery started");
                running = true;
            }

            @Override
            public void onServiceFound(NsdServiceInfo service)
            {
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE))
                {
                    Log.d(TAG, "Service Type: " + service.getServiceType());
                    Log.d(TAG, "Name: " + service.getServiceName());
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service)
            {
                Log.e(TAG, "service lost" + service);
                if (mService == service)
                {
                    mService = null;
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType)
            {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode)
            {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                //mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode)
            {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                //mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener()
    {
        mResolveListener = new NsdManager.ResolveListener()
        {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode)
            {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo)
            {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName))
                {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                mService = serviceInfo;
            }
        };
    }

    public void discoverServices()
    {
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void stopDiscovery()
    {
        if (running)
        {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }
    }

    public NsdServiceInfo getChosenServiceInfo()
    {
        return mService;
    }

    public void tearDown()
    {
    }
}
