package net.groenholdt.wakelog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;

import net.groenholdt.wakelog.model.LogDatabaseHelper;


public class AddDeviceDialogFragment extends DialogFragment
{
    private static final String TAG = "AddDeviceDialogFragment";
    private LogDatabaseHelper database;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        database = new LogDatabaseHelper(getActivity());

        builder.setView(inflater.inflate(R.layout.dialog_add_device, null))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // Create new device in database.
                        EditText name = (EditText) getActivity()
                                .findViewById(R.id.nameEditText);
                        Log.d(TAG, "Adding device " + name.toString());
                        //database.addDevice(name.getText().toString(), 0, 0);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
