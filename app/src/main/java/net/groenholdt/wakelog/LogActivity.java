package net.groenholdt.wakelog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import net.groenholdt.wakelog.model.Device;
import net.groenholdt.wakelog.model.LogDatabaseHelper;

public class LogActivity extends AppCompatActivity implements AddDeviceDialogFragment.AddDeviceDialogListener
{
    private static final String TAG = "LogActivity";
    private LogDatabaseHelper database;
    private SimpleCursorAdapter logAdapter;
    private Device device;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        database = new LogDatabaseHelper(this);
        device = database.getDevice(getIntent().getLongExtra("device_id", 0));

        logAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                database.getLogCursor(device.getId()),
                new String[]{"time"},
                new int[]{android.R.id.text1},
                0);

        ListView listView = (ListView) findViewById(R.id.logView);
        if (logAdapter != null)
        {
            listView.setAdapter(logAdapter);
        }

        FloatingActionButton fab =
                (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

            }
        });
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
            database.addDevice(name, 0, 0);
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
