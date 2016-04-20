package net.groenholdt.wakelog.model;

import java.util.ArrayList;

/**
 * Created by oblivion on 05/04/16.
 * <p/>
 * Representation of a WakeLog device.
 */
public class Device
{
    private long id;
    private String name;
    private int ipAddr;
    private long syncTime;
    private ArrayList<LogEntry> log = new ArrayList<>();

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return (name);
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getIpAddr()
    {
        return (ipAddr);
    }

    public void setIpAddr(int ipAddr)
    {
        this.ipAddr = ipAddr;
    }

    public long getSyncTime()
    {
        return (syncTime);
    }

    public void setSyncTime(long syncTime)
    {
        this.syncTime = syncTime;
    }

    public ArrayList<LogEntry> getLog()
    {
        return (log);
    }

    public void setLog(ArrayList<LogEntry> log)
    {
        this.log = log;
    }

    @Override
    public String toString()
    {
        return ("Id: " + String.valueOf(id) + ", name: " +
                        String.valueOf(name) + ", IP address: " +
                        String.valueOf(ipAddr) + ", last sync time: " +
                        String.valueOf(syncTime));
    }
}
