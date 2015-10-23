package com.example.android.popularmovies;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.plus.PlusOneButton;

public class AboutFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Resources res = getResources();
        String title = res.getString(R.string.about) + " " + res.getString(getActivity().getApplicationInfo().labelRes);
        String creatorName = res.getString(R.string.creator);
        String versionInfo = res.getString(R.string.version_label) + ": "
                + res.getString(R.string.version);

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(creatorName + "\n" + versionInfo)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
    }
}
