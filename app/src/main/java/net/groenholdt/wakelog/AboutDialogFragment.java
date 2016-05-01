package net.groenholdt.wakelog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;


public class AboutDialogFragment extends DialogFragment
{
    private static final String TAG = "AddDeviceDialogFragment";
    private static Button positiveButton;
    private static TextView versionLabel;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setTitle(R.string.title_dialog_about);
        builder.setView(inflater.inflate(R.layout.dialog_about, null));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                onDialogPositiveClick(AboutDialogFragment.this);
            }
        });

        Dialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialog)
            {
                versionLabel = (TextView) ((AlertDialog) dialog).findViewById(R.id.version_label);
                String version = "" + net.groenholdt.wakelog.BuildConfig.VERSION_NAME;
                versionLabel.setText(version);
            }
        });

        return (dialog);
    }

    void onDialogPositiveClick(DialogFragment dialog)
    {
        Log.d(TAG, "About dialog dismissed.");
    }
}

