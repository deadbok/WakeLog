package net.groenholdt.wakelog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AddDeviceDialogFragment extends DialogFragment implements TextWatcher
{
    private static final String TAG = "AddDeviceDialogFragment";
    private static Button positiveButton;
    private static EditText nameEdit;
    AddDeviceDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setTitle(R.string.title_dialog_add_device);
        builder.setView(inflater.inflate(R.layout.dialog_add_device, null));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                listener.onDialogPositiveClick(AddDeviceDialogFragment.this);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                listener.onDialogNegativeClick(AddDeviceDialogFragment.this);
            }
        });

        Dialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                nameEdit = (EditText) ((AlertDialog) dialog).findViewById(R.id.nameEditText);
                nameEdit.addTextChangedListener(AddDeviceDialogFragment.this);

                positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setEnabled(false);
            }
        });


        return (dialog);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try {
            listener = (AddDeviceDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    private boolean isValidHost(String host) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9]+";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(host);
        return matcher.matches();
    }

    @Override
    public void afterTextChanged(Editable s)
    {
        if (isValidHost(nameEdit.getText().toString()))
        {
            positiveButton.setEnabled(true);
            nameEdit.setError(null, null);
        } else
        {
            positiveButton.setEnabled(false);
            nameEdit.setError(getString(R.string.invalid_device_name));
        }
    }

    @Override
    final public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* Don't care */ }

    @Override
    final public void onTextChanged(CharSequence s, int start, int before, int count) { /* Don't care */ }

    public interface AddDeviceDialogListener
    {
        void onDialogPositiveClick(DialogFragment dialog);

        void onDialogNegativeClick(DialogFragment dialog);
    }
}
