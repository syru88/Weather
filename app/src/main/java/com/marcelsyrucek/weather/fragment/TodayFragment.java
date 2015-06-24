package com.marcelsyrucek.weather.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.WeatherApplication;
import com.marcelsyrucek.weather.activity.MainActivity;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.database.model.CurrentWeatherModel;
import com.marcelsyrucek.weather.event.CityClickedEvent;
import com.marcelsyrucek.weather.event.CityLoadedEvent;
import com.marcelsyrucek.weather.event.CurrentWeatherLoadedEvent;
import com.marcelsyrucek.weather.service.NetworkService;
import com.marcelsyrucek.weather.utility.Logcat;
import com.marcelsyrucek.weather.utility.WeatherUtility;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;


/**
 * A simple {@link Fragment} subclass.
 */
public class TodayFragment extends Fragment /*implements LoaderManager.LoaderCallbacks<CurrentWeatherModel>*/ {

	public static final String TAG = TodayFragment.class.getSimpleName();

	private ViewGroup mRoot;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private TextView mCity, mDescription, mTemperature;
	private ImageView mPoster, mIcon;
	private TextView mHumidity, mPrecipitation, mPressure, mWind, mDirection;

	private int mLengthPreference;
	private int mTempPreference;
	private String mWindSpeedUnit;

	private CityModel mShownCity;

	private Bus mBus = WeatherApplication.bus;

	public static TodayFragment newInstance() {
		TodayFragment fragment = new TodayFragment();
		return fragment;
	}

	public TodayFragment() {
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

	private void startNetworkService(boolean showProgress) {
		Intent intent = new Intent(getActivity(), NetworkService.class);
		intent.putExtra(NetworkService.EXTRA_REQUEST, NetworkService.REQUEST_VALUE_CURRENT_WEATHER);
		intent.putExtra(NetworkService.EXTRA_CITY, mShownCity);

		if (mSwipeRefreshLayout != null && showProgress) {
			mSwipeRefreshLayout.setRefreshing(true);
		}

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

//	@Override
//	public void setUserVisibleHint(boolean isVisibleToUser) {
//		Logcat.d(TAG, "setUserVisibleHint");
//		super.setUserVisibleHint(isVisibleToUser);
//
//		if (isVisibleToUser) {
//			startNetworkService(true);
//		}
//	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		Logcat.d(TAG, "onCreateView");

		mRoot = (ViewGroup) inflater.inflate(R.layout.fragment_today, container, false);

		mSwipeRefreshLayout = (SwipeRefreshLayout) mRoot.findViewById(R.id.fragment_today_swipe_refresh_layout);
		mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				Logcat.d(TAG, "Manual refresh");
				startNetworkService(false);
			}
		});

		mCity = (TextView) mRoot.findViewById(R.id.fragment_today_city);
		mDescription = (TextView) mRoot.findViewById(R.id.fragment_today_description);
		mTemperature = (TextView) mRoot.findViewById(R.id.fragment_today_temperature);

		// TODO Marcel: mPoster will be used in the future for showing images from network
		mPoster = (ImageView) mRoot.findViewById(R.id.fragment_today_poster);
		mIcon = (ImageView) mRoot.findViewById(R.id.fragment_today_icon);

		mHumidity = (TextView) mRoot.findViewById(R.id.fragment_today_humidity);
		mPrecipitation = (TextView) mRoot.findViewById(R.id.fragment_today_precipitation);
		mPressure = (TextView) mRoot.findViewById(R.id.fragment_today_pressure);
		mWind = (TextView) mRoot.findViewById(R.id.fragment_today_wind);
		mDirection = (TextView) mRoot.findViewById(R.id.fragment_today_direction);

		return mRoot;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Logcat.d(TAG, "onDestroyView");
	}

	@Subscribe
	public void subscribeOnCityClickedEvent(CityClickedEvent event) {
		Logcat.e(TAG, "CityClickedEvent: " + event.getCityModel());
		mShownCity = event.getCityModel();
		startNetworkService(true);
	}

	@Subscribe
	public void subscribeOnCityLoadedEvent(CityLoadedEvent event) {
		Logcat.d(TAG, "CityLoadedEvent: " + event.getCityModel());
		mShownCity = event.getCityModel();
	}

	/**
	 * Subscribe this method for listening on {@link CurrentWeatherLoadedEvent} which happens when we have old location from
	 * database but the new location is different. So user doesn't wait for position or network neither.
	 */
	@Subscribe
	public void subscribeOnCurrentWeatherLoadedEvent(CurrentWeatherLoadedEvent event) {
		Logcat.e(TAG, "CurrentWeatherLoadedEvent: " + event.getCurrentWeatherModel());

		if (mSwipeRefreshLayout != null) {
			mSwipeRefreshLayout.setRefreshing(false);
		}

		handleErrors(event.getCurrentWeatherModel());
		loadData(event.getCurrentWeatherModel());

	}

	private void loadUnitFromPreferences() {

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mLengthPreference = Integer.parseInt(preferences.getString(getString(R.string.prefs_key_unit_of_length), getString(R
				.string
				.prefs_default_value_unit_of_length)));
		mTempPreference = Integer.parseInt(preferences.getString(getString(R.string.prefs_key_unit_of_temperature),
				getString(R.string.prefs_default_value_unit_of_temperature)));
	}

	private void loadData(CurrentWeatherModel currentWeatherModel) {
		loadUnitFromPreferences();

		mWindSpeedUnit = WeatherUtility.getWindSpeedUnit(mLengthPreference, getActivity());

		mCity.setText(currentWeatherModel.getCity());
		mDescription.setText(currentWeatherModel.getDescription());
		mTemperature.setText(WeatherUtility.getTemperature(mTempPreference, currentWeatherModel.getTemperature()) + getString(R.string.weather_temp_degree));

		// TODO Marcel: handle images
//
		mHumidity.setText(currentWeatherModel.getHumidity() + getString(R.string.weather_humidity_unit));
		mPrecipitation.setText(currentWeatherModel.getPrecipitation() + " " + getString(R.string
				.weather_precipitation_unit));
		mPressure.setText(currentWeatherModel.getPressure() + " " + getString(R.string.weather_preasure_unit));
		mWind.setText(WeatherUtility.getWindSpeed(mLengthPreference, currentWeatherModel.getWindSpeed()) + " " + mWindSpeedUnit);
		mDirection.setText(currentWeatherModel.getWindDirection());

	}

	private void handleErrors(CurrentWeatherModel currentWeather) {
		if (currentWeather.isError()) {
			Snackbar.make(mRoot, currentWeather.getErrorText(), Snackbar.LENGTH_LONG).show();
		}
	}


}
