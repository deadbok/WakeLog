package net.groenholdt.wakelog.model;

/**
 * Created by oblivion on 05/04/16.
 *
 * A single log entry.
 */
@SuppressWarnings("unused")
public class LogEntry
{
    private long id;
    private long time;
    private int type;
    private int deviceId;

    public long getId()
    {
        return (id);
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getTime()
    {
        return (time);
    }

    public void setTime(long time)
    {
        this.time = time;
    }

    public int getType()
    {
        return (type);
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public int getDeviceId()
    {
        return (deviceId);
    }

    public void setDeviceId(int deviceId)
    {
        this.deviceId = deviceId;
    }

    @Override
    public String toString()
    {
        return ("Id: " + String.valueOf(id) + ", time: " +
                        String.valueOf(time) + ", type: " +
                        String.valueOf(type) + ", device id: " +
                        String.valueOf(deviceId));
    }
}