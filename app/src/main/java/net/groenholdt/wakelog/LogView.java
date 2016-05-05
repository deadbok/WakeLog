package net.groenholdt.wakelog;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by oblivion on 19/04/16.
 * <p/>
 * Convert unix time to human readable time.
 */
class LogView implements SimpleCursorAdapter.ViewBinder
{
    private static final String TAG = "LogView";
    private final Context context;
    private int day;
    private int month;
    private int year;

    public LogView(Context context)
    {
        this.context = context;
    }

    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex)
    {
        int id = view.getId();

        Log.d(TAG, "Setting values for view with id: " + id);
        switch (view.getId())
        {
            case R.id.log_time:
                Log.d(TAG, "Setting time.");

                long time = cursor.getLong(columnIndex);
                if (time > 0)
                {
                    Log.d(TAG, "Time: " +
                               DateFormat.getDateTimeInstance().format(new java.util.Date(time)));
                    ((TextView) view).setText(
                            DateFormat.getDateTimeInstance().format(new java.util.Date(time)));
                }
                else
                {
                    ((TextView) view).setText(R.string.invalid);
                    //Cannot do anything with an invalid time, get out.
                    return true;
                }
                Calendar lastTime = GregorianCalendar.getInstance();
                lastTime.setTimeInMillis(time);

                if (year != lastTime.get(Calendar.YEAR))
                {
                    Log.d(TAG, "Highlighting next year.");
                    Log.d(TAG, "Year: " + year);
                    Log.d(TAG, "Current time: " + DateFormat.getDateTimeInstance().format(
                            lastTime.getTime()));
                    ((LinearLayout) view.getParent()).setBackgroundColor(
                            ContextCompat.getColor(view.getContext(), R.color.colorPrimary));
                    year = lastTime.get(Calendar.YEAR);
                    month = lastTime.get(Calendar.MONTH);
                    day = lastTime.get(Calendar.DAY_OF_MONTH);
                    Log.d(TAG, "Year: " + year);
                    return true;
                }

                if (month != lastTime.get(Calendar.MONTH))
                {
                    Log.d(TAG, "Highlighting next month.");
                    Log.d(TAG, "Month: " + month);
                    Log.d(TAG, "Current time: " + DateFormat.getDateTimeInstance().format(
                            lastTime.getTime()));
                    ((LinearLayout) view.getParent()).setBackgroundColor(
                            ContextCompat.getColor(view.getContext(), R.color.colorPrimary));
                    month = lastTime.get(Calendar.MONTH);
                    day = lastTime.get(Calendar.DAY_OF_MONTH);
                    Log.d(TAG, "Month: " + month);
                    return true;
                }

                if (day != lastTime.get(Calendar.DAY_OF_MONTH))
                {
                    Log.d(TAG, "Highlighting next day.");
                    Log.d(TAG, "Day: " + day);
                    Log.d(TAG, "Current time: " + DateFormat.getDateTimeInstance().format(
                            lastTime.getTime()));
                    ((LinearLayout) view.getParent()).setBackgroundColor(
                            ContextCompat.getColor(view.getContext(), R.color.colorPrimary));
                    day = lastTime.get(Calendar.DAY_OF_MONTH);
                    Log.d(TAG, "Day: " + day);
                    return true;
                }
                ((LinearLayout) view.getParent()).setBackgroundResource(0);
                return true;
            default:
                return false;
        }
    }
}
