package com.marcelsyrucek.weather.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.WeatherApplication;
import com.marcelsyrucek.weather.activity.MainActivity;
import com.marcelsyrucek.weather.adapter.ForecastAdapter;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.database.model.ForecastWeatherModelList;
import com.marcelsyrucek.weather.event.CityClickedEvent;
import com.marcelsyrucek.weather.event.CityLoadedEvent;
import com.marcelsyrucek.weather.event.ForecastLoadedEvent;
import com.marcelsyrucek.weather.service.NetworkService;
import com.marcelsyrucek.weather.utility.Logcat;
import com.marcelsyrucek.weather.utility.WeatherUtility;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;


/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment {

	public static final String TAG = ForecastFragment.class.getSimpleName();

	private ViewGroup mRoot;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private RecyclerView mRecyclerView;

	private int mTempPreference;
	private ForecastAdapter mAdapter;

	private CityModel mShownCity;

	private Bus mBus = WeatherApplication.bus;

	public static ForecastFragment newInstance() {
		ForecastFragment fragment = new ForecastFragment();
		return fragment;
	}

	public ForecastFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mShownCity = (CityModel) savedInstanceState.getSerializable(MainActivity.BUNDLE_LAST_SHOWN_CITY);
		}
		Logcat.d(TAG, "onCreate, mShownCity: " + mShownCity);
	}

	@Override
	public void onStart() {
		super.onStart();
		mBus.register(this);
	}

	private void startNetworkService() {
		Intent intent = new Intent(getActivity(), NetworkService.class);
		intent.putExtra(NetworkService.EXTRA_REQUEST, NetworkService.REQUEST_VALUE_FORECAST);
		intent.putExtra(NetworkService.EXTRA_CITY, mShownCity);
		getActivity().startService(intent);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Logcat.d(TAG, "onSaveInstanceState");
		outState.putSerializable(MainActivity.BUNDLE_LAST_SHOWN_CITY, mShownCity);

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStop() {
		super.onStop();
		mBus.unregister(this);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);

		if (isVisibleToUser) {
			startNetworkService();
		}
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
		mAdapter = new ForecastAdapter(getResources().getString(R.string.weather_temp_degree));
		LinearLayoutManager layoutManager = new LinearLayoutManager(container.getContext(), LinearLayoutManager.VERTICAL, false);

		mRecyclerView = (RecyclerView) mRoot.findViewById(R.id.fragment_forecast_recycler_view);
		mRecyclerView.setLayoutManager(layoutManager);
		mRecyclerView.setAdapter(mAdapter);

		return mRoot;
	}

	@Subscribe
	public void subscribeOnCityClickedEvent(CityClickedEvent event) {
		Logcat.e(TAG, "CityClickedEvent: " + event.getCityModel());
		mShownCity = event.getCityModel();
		startNetworkService();
	}

	@Subscribe
	public void subscribeOnCityLoadedEvent(CityLoadedEvent event) {
		Logcat.d(TAG, "CityLoadedEvent: " + event.getCityModel());
		mShownCity = event.getCityModel();
	}

	@Subscribe
	public void subscibeOnForecastLoadedEvent(ForecastLoadedEvent event) {
		Logcat.e(TAG, "ForecastLoadedEvent: " + event.getForecastWeatherModelList());

		if (mSwipeRefreshLayout != null) {
			mSwipeRefreshLayout.setRefreshing(false);
		}

		handleErrors(event.getForecastWeatherModelList());
		loadData(event.getForecastWeatherModelList());
	}

	private void loadUnitFromPreferences() {

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mTempPreference = Integer.parseInt(preferences.getString(getString(R.string.prefs_key_unit_of_temperature),
				getString(R.string.prefs_default_value_unit_of_temperature)));
	}

	private void loadData(ForecastWeatherModelList forecastWeatherModelList) {
		loadUnitFromPreferences();
		mAdapter.setDays(forecastWeatherModelList.getDays(), mTempPreference);
	}

	private void handleErrors(ForecastWeatherModelList forecastWeatherModelList) {
		if (forecastWeatherModelList.isError()) {
			Snackbar.make(mRoot, forecastWeatherModelList.getErrorText(), Snackbar.LENGTH_LONG).show();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Logcat.d(TAG, "onDestroyView");
	}
}
