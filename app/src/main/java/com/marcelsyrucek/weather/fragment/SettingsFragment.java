package com.marcelsyrucek.weather.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.utility.Logcat;

public class SettingsFragment extends PreferenceFragment {

	public static final String TAG = SettingsFragment.class.getSimpleName();

	public static SettingsFragment newInstance() {
		SettingsFragment fragment = new SettingsFragment();
		return fragment;
	}

	public SettingsFragment() {
		// Required empty public constructor
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logcat.d(TAG, "onCreate");
		addPreferencesFromResource(R.xml.preferences);
	}

}
