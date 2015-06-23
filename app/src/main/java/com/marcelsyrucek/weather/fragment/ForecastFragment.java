package com.marcelsyrucek.weather.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marcelsyrucek.weather.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment {

	public static final String TAG = ForecastFragment.class.getSimpleName();


	public static ForecastFragment newInstance() {
		ForecastFragment fragment = new ForecastFragment();
		return fragment;
	}


	public ForecastFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_forecast, container, false);
	}


}
