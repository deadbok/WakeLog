package net.groenholdt.wakelog;

import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

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
                int time = cursor.getInt(columnIndex);
                if (time > 0)
                {
                    ((TextView) view).setText(new java.util.Date(time).toString());
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