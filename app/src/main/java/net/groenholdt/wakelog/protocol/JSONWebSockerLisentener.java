package net.groenholdt.wakelog.protocol;

import net.groenholdt.wakelog.model.LogEntry;

/**
 * Created by oblivion on 17/04/16.
 */
public interface JSONWebSockerLisentener
{
    void onOpen();
    void onLogEntry(LogEntry entry);
}
