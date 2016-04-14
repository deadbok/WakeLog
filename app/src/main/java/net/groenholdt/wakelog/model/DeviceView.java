package net.groenholdt.wakelog.model;

import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import net.groenholdt.wakelog.R;

/**
 * Created by oblivion on 08/04/16.
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
                    ((TextView) view).setText(String.valueOf(time));
                }
                else
                {
                    ((TextView) view).setText("Never");
                }
                return true;
        }
        return false;
    }
}