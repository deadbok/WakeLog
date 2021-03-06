package net.groenholdt.wakelog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;


public class AboutDialogFragment extends DialogFragment
{
    private static final String TAG = "AddDeviceDialogFragment";
    private static TextView versionLabel;

    @SuppressLint("InflateParams")
    @NonNull
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
                versionLabel = (TextView) ((AlertDialog) dialog).findViewById(R.id.versionLabel);
                String version = "" + net.groenholdt.wakelog.BuildConfig.VERSION_NAME;
                versionLabel.setText(version);
            }
        });

        return (dialog);
    }

    @SuppressWarnings("UnusedParameters")
    private void onDialogPositiveClick(DialogFragment dialog)
    {
        Log.d(TAG, "About dialog dismissed.");
    }
}

