package net.groenholdt.wakelog;

import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.DateFormat;

/**
 * Created by oblivion on 19/04/16.
 * <p/>
 * Convert unix time to human readable time.
 */
public class LogView implements SimpleCursorAdapter.ViewBinder {
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        switch (view.getId()) {
            case R.id.log_time:
                long time = cursor.getLong(columnIndex);
                if (time > 0) {
                    ((TextView) view).setText(DateFormat.getDateTimeInstance().format(new java.util.Date(time)));
                } else {
                    ((TextView) view).setText(R.string.invalid);
                }
                return true;
        }
        return false;
    }
}
