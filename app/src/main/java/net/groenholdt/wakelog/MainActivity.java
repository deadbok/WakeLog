package net.groenholdt.wakelog;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import net.groenholdt.wakelog.model.DeviceContract;
import net.groenholdt.wakelog.model.LogDatabaseProvider;

public class MainActivity extends AppCompatActivity
        implements AddDeviceDialogFragment.AddDeviceDialogListener
{
    private static final String TAG = "MainActivity";
    private static final int DEVICE_LOADER_ID = 1;

    private FloatingActionButton fab;
    private SimpleCursorAdapter deviceAdapter;

    private LoaderManager.LoaderCallbacks<Cursor> deviceLoader =
            new LoaderManager.LoaderCallbacks<Cursor>()
            {
                // Create and return the actual cursor loader for the contacts data
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args)
                {
                    // Define the columns to retrieve
                    String[] projection =
                            {DeviceContract.DeviceEntry._ID, DeviceContract.DeviceEntry.COLUMN_NAME_NAME, DeviceContract.DeviceEntry.COLUMN_NAME_SYNC_TIME};
                    // Construct the loader
                    CursorLoader cursorLoader =
                            new CursorLoader(MainActivity.this,
                                    LogDatabaseProvider.URI_DEVICE,
                                    projection,
                                    null,
                                    null,
                                    null
                            );

                    return cursorLoader;
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
                {
                    deviceAdapter.swapCursor(cursor);
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader)
                {
                    deviceAdapter.swapCursor(null);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        deviceAdapter = new SimpleCursorAdapter(this,
                R.layout.device_list_item,
                null,
                new String[]{DeviceContract.DeviceEntry.COLUMN_NAME_NAME, DeviceContract.DeviceEntry.COLUMN_NAME_SYNC_TIME},
                new int[]{R.id.device_name, R.id.device_sync_time},
                0);

        deviceAdapter.setViewBinder(new DeviceView());

        ListView listView = (ListView) findViewById(R.id.deviceView);
        listView.setAdapter(deviceAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
            {
                Intent intent =
                        new Intent(MainActivity.this, LogActivity.class);
                intent.putExtra("device_id", id);

                Log.d(TAG, "Showing device with id: " + String.valueOf(id));

                startActivity(intent);
            }
        });

        FloatingActionButton fab =
                (FloatingActionButton) findViewById(R.id.fab_add_device);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DialogFragment addDeviceDialog =
                        new AddDeviceDialogFragment();
                addDeviceDialog
                        .show(getSupportFragmentManager(), "add_device");
            }
        });

        getSupportLoaderManager()
                .initLoader(DEVICE_LOADER_ID, null, deviceLoader);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return (true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_about:
                DialogFragment aboutDialog =
                        new AboutDialogFragment();
                aboutDialog
                        .show(getSupportFragmentManager(), "about");

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (deviceAdapter != null)
        {
            deviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        if (deviceAdapter != null)
        {
            deviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog)
    {
        Dialog d = dialog.getDialog();
        EditText nameEdit = (EditText) d.findViewById(R.id.nameEditText);
        if (nameEdit != null)
        {
            String name = nameEdit.getText().toString();

            Log.d(TAG, "Adding device " + name);
            try
            {
                ContentValues deviceValues = new ContentValues();

                deviceValues
                        .put(DeviceContract.DeviceEntry.COLUMN_NAME_NAME, name);
                deviceValues.put(DeviceContract.DeviceEntry.COLUMN_NAME_IP, 0);
                deviceValues
                        .put(DeviceContract.DeviceEntry.COLUMN_NAME_SYNC_TIME, 0);

                getContentResolver()
                        .insert(LogDatabaseProvider.URI_DEVICE, deviceValues);
            }
            catch (SQLException e)
            {
            }

        }
        else
        {
            Log.e(TAG, "Could not get device name from UI.");
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog)
    {
    }
}
