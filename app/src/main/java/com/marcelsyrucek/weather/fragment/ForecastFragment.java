package com.marcelsyrucek.weather.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.WeatherApplication;
import com.marcelsyrucek.weather.service.NetworkService;
import com.marcelsyrucek.weather.utility.Logcat;
import com.squareup.otto.Bus;


/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment {

	public static final String TAG = ForecastFragment.class.getSimpleName();

	private ViewGroup mRoot;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private RecyclerView mRecyclerView;

	private int mTempPreference;

	private Bus mBus = WeatherApplication.bus;

	public static ForecastFragment newInstance() {
		ForecastFragment fragment = new ForecastFragment();
		return fragment;
	}

	public ForecastFragment() {
		// Required empty public constructor
	}

	@Override
	public void onStart() {
		super.onStart();
		Logcat.d(TAG, "onStart");
		mBus.register(this);
		startNetworkService();
	}

	private void startNetworkService() {
		Intent intent = new Intent(getActivity(), NetworkService.class);
		intent.putExtra(NetworkService.EXTRA_REQUEST, NetworkService.REQUEST_VALUE_FORECAST);
		getActivity().startService(intent);
	}

	@Override
	public void onStop() {
		super.onStop();
		Logcat.d(TAG, "onStop");
		mBus.unregister(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		Logcat.d(TAG, "onCreateView");

		mRoot = (ViewGroup) inflater.inflate(R.layout.fragment_forecast, container, false);

		mSwipeRefreshLayout = (SwipeRefreshLayout) mRoot.findViewById(R.id.fragment_forecast_swipe_refresh_layout);
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				Logcat.d(TAG, "Manual Refresh");
				startNetworkService();
			}
		});
		mRecyclerView = (RecyclerView) mRoot.findViewById(R.id.fragment_forecast_recycler_view);

		return mRoot;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Logcat.d(TAG, "onDestroyView");
	}
}
