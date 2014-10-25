package com.ecwidizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by arkady on 9/14/13.
 */
public class SettingsDialogFragment extends DialogFragment {
    View view;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        this.view = inflater.inflate(R.layout.dialog_settings, null);

        builder.setView(view)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    	EditText storeId = (EditText)view.findViewById(R.id.store_id);
                    	EditText storeKey = (EditText)view.findViewById(R.id.store_key);
						EcwidizerSettings.get().setStoreId(storeId.getText().toString());
						EcwidizerSettings.get().setToken(storeKey.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        SettingsDialogFragment.this.fillSettings(EcwidizerSettings.get().getStoreId(), EcwidizerSettings.get().getToken());

        return builder.create();
    }

    public void fillSettings(String storeIdValue, String storeKeyValue) {
        EditText storeId = (EditText)view.findViewById(R.id.store_id);
        EditText storeKey = (EditText)view.findViewById(R.id.store_key);
        storeId.setText(storeIdValue);
        storeKey.setText(storeKeyValue);
    }
}

