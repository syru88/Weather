package com.marcelsyrucek.weather.dialog;


import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;

import com.marcelsyrucek.weather.R;

public class AboutDialogFragment extends DialogFragment {

	public static final String TAG = AboutDialogFragment.class.getSimpleName();

	public static AboutDialogFragment newInstance() {
		AboutDialogFragment fragment = new AboutDialogFragment();
		return fragment;
	}

	public AboutDialogFragment() {
		// Required empty public constructor
	}

	@Override
	public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(R.string.menu_action_about).setMessage(R.string.dialog_about_message).setPositiveButton(R.string.general_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});

		return builder.create();
	}
}
