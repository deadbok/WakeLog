package net.groenholdt.wakelog.protocol;

import net.groenholdt.wakelog.model.LogEntry;

/**
 * Created by oblivion on 17/04/16.
 */
public interface LogEntryLisentener {
    void onLogEntry(LogEntry entry);
}
