package net.groenholdt.wakelog;

import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.DateFormat;

/**
 * Created by oblivion on 08/04/16.
 *
 * Replace sync time with never, if never synced.
 */
public class DeviceView implements SimpleCursorAdapter.ViewBinder
{
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex)
    {
        switch (view.getId())
        {
            case R.id.device_sync_time:
                long time = cursor.getLong(columnIndex);
                if (time > 0)
                {
                    ((TextView) view).setText(DateFormat.getDateTimeInstance().format(new java.util.Date(time)));
                }
                else
                {
                    ((TextView) view).setText(R.string.never);
                }
                return true;
        }
        return false;
    }
}