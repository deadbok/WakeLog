package net.groenholdt.wakelog.protocol;

import java.net.InetAddress;

/**
 * Created by oblivion on 10/04/16.
 */
public interface DeviceDiscoverListener
{
    /**
     * Called when the address of a device is found.
     *
     * @param addr The IP address of the device.
     * @param port The port of the device.
     */
    void onResolved(InetAddress addr, int port);

    /**
     * Called when a device cannot be resolved.
     */
    void onResolveFailed();
}
