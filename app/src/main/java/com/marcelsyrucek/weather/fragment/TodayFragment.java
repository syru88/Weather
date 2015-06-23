package com.marcelsyrucek.weather.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.WeatherApplication;
import com.marcelsyrucek.weather.database.model.CurrentWeatherModel;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.event.LoadCityEvent;
import com.marcelsyrucek.weather.loader.TodayLoader;
import com.marcelsyrucek.weather.utility.Logcat;
import com.marcelsyrucek.weather.utility.WeatherUtility;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;


/**
 * A simple {@link Fragment} subclass.
 */
public class TodayFragment extends Fragment implements LoaderManager.LoaderCallbacks<CurrentWeatherModel> {

	public static final String TAG = TodayFragment.class.getSimpleName();

	public static final String BUNDLE_CITY_MODEL = "BUNDLE_CITY_MODEL";

	private ViewGroup mRoot;
	private TextView mCity, mDescription, mTemperature;
	private ImageView mPoster, mIcon;
	private TextView mHumidity, mPrecipitation, mPressure, mWind, mDirection;
	private SwipeRefreshLayout mSwipeRefreshLayout;

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
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logcat.d(TAG, "onCreate");

		if (savedInstanceState != null) {
			mShownCity = (CityModel) savedInstanceState.getSerializable(BUNDLE_CITY_MODEL);
			Logcat.e(TAG, "mShownCity: " + mShownCity);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Logcat.d(TAG, "onStart");
		mBus.register(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		Logcat.d(TAG, "onStop");
		mBus.unregister(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		Logcat.d(TAG, "onResume");
	}

	@Override
	public void onPause() {
		super.onPause();
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(BUNDLE_CITY_MODEL, mShownCity);

		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		Logcat.d(TAG, "onCreateView");

		mRoot = (ViewGroup) inflater.inflate(R.layout.fragment_today, container, false);

		mSwipeRefreshLayout = (SwipeRefreshLayout) mRoot.findViewById(R.id.swipe_refreshLayout);
		mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				Logcat.d(TAG, "Manual refresh");
				getLoaderManager().restartLoader(0, null, TodayFragment.this);
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
	/**
	 * Subscribe this method for listening on {@link LoadCityEvent} which happens when we have old location from
	 * database but the new location is different. So user doesn't wait for position or network neither.
	 */
	public void subscribeOnRefreshEvent(LoadCityEvent event) {
		if (mSwipeRefreshLayout != null) {
			mSwipeRefreshLayout.setRefreshing(true);
		}
		CityModel newCity = event.getCityModel();
		Logcat.e(TAG, "subscribeOnRefreshEvent: " + newCity.getName() + ", long: " + newCity.getLongitude() + ", lat: " + newCity.getLatitude() + ", id: " + newCity.getId());
//		if (newCity != null && ) {
//
//		}

	}

	private String getWindSpeedUnitFromPref() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mLengthPreference = Integer.parseInt(preferences.getString(getString(R.string.prefs_key_unit_of_length)
				, "0"));
		mTempPreference = Integer.parseInt(preferences.getString(getString(R.string.prefs_key_unit_of_temperature),
				"0"));

		return WeatherUtility.getWindSpeedUnit(mLengthPreference, getActivity());
	}

	@Override
	public Loader<CurrentWeatherModel> onCreateLoader(int id, Bundle args) {
		Logcat.e(TAG, "onCreateLoader");

		return new TodayLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<CurrentWeatherModel> loader, CurrentWeatherModel data) {
		Logcat.e(TAG, "onLoadFinished");
		if (mSwipeRefreshLayout != null) {
			mSwipeRefreshLayout.setRefreshing(false);
		}

//		WeatherApplication.bus.post(new CityLoadedEvent());
		loadData(data);
		if (data.isError()) {
			handleErrors(data);
		}

	}

	@Override
	public void onLoaderReset(Loader<CurrentWeatherModel> loader) {
		Logcat.e(TAG, "onLoaderReset");
	}

	private void loadData(CurrentWeatherModel currentWeatherModel) {
		mWindSpeedUnit = getWindSpeedUnitFromPref();

		mCity.setText(currentWeatherModel.getCity());
		mDescription.setText(currentWeatherModel.getDescription());
		mTemperature.setText(currentWeatherModel.getTemperature(mTempPreference) + getString(R.string.weather_temp_degree));

		// TODO Marcel: handle images
//
		mHumidity.setText(currentWeatherModel.getHumidity() + getString(R.string.weather_humidity_unit));
		mPrecipitation.setText(currentWeatherModel.getPrecipitation() + " " + getString(R.string
				.weather_precipitation_unit));
		mPressure.setText(currentWeatherModel.getPressure() + " " + getString(R.string.weather_preasure_unit));
		mWind.setText(currentWeatherModel.getWindSpeed(mLengthPreference) + " " + mWindSpeedUnit);
		mDirection.setText(currentWeatherModel.getWindDirection());

	}

	private void handleErrors(CurrentWeatherModel currentWeather) {
		Snackbar.make(mRoot, currentWeather.getErrorText(), Snackbar.LENGTH_LONG).show();
	}


}