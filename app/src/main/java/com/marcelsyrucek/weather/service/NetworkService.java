package com.marcelsyrucek.weather.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.WeatherApplication;
import com.marcelsyrucek.weather.database.CurrentWeatherDatabase;
import com.marcelsyrucek.weather.database.ForecastDatabase;
import com.marcelsyrucek.weather.database.model.AgeAndExceptionData;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.database.model.CurrentWeatherModel;
import com.marcelsyrucek.weather.database.model.ForecastWeatherListModel;
import com.marcelsyrucek.weather.event.CityLoadedEvent;
import com.marcelsyrucek.weather.event.CurrentWeatherLoadedEvent;
import com.marcelsyrucek.weather.event.ForecastLoadedEvent;
import com.marcelsyrucek.weather.pojo.currentweather.CurrentWeatherPojo;
import com.marcelsyrucek.weather.pojo.forecast.ForecastPojo;
import com.marcelsyrucek.weather.request.GetForecastRequest;
import com.marcelsyrucek.weather.request.GetWeatherRequest;
import com.marcelsyrucek.weather.utility.Logcat;
import com.squareup.otto.Bus;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by marcel on 23.6.2015.
 */
public class NetworkService extends Service {

	public static final String TAG = NetworkService.class.getSimpleName();

	public static final String EXTRA_CITY = "EXTRA_CITY";
	public static final String EXTRA_REQUEST = "EXTRA_REQUEST";

	public static final int REQUEST_VALUE_CURRENT_WEATHER = 0;
	public static final int REQUEST_VALUE_FORECAST = 1;
	public static final int REQUEST_VALUE_SAVE_DATA = 2;

	private static final int INACTIVITY_TIME_IN_MILISECONDS = 1000 * 60 * 2; // 2 minutes

	/**
	 * when true we know that application is still active so don't stop service
	 */
	private boolean mServiceWasCalled;

	private int mRequestValue;
	private CityModel mLastLoadedCity;
	private CurrentWeatherModel mCurrentWeather;
	private ForecastWeatherListModel mForecast;

	private boolean mWasAskedForId;

	private Bus mBus = WeatherApplication.bus;

	@Override
	public void onCreate() {
		super.onCreate();
		Logcat.d(TAG, "onCreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		CityModel requestedCity = (CityModel) intent.getSerializableExtra(EXTRA_CITY);
		mRequestValue = intent.getIntExtra(EXTRA_REQUEST, REQUEST_VALUE_CURRENT_WEATHER);

		Logcat.d(TAG, "onStartCommand: " + requestedCity + ", request: " + mRequestValue + ", shown: " +
				mLastLoadedCity);

		if (requestedCity == null) {
			Logcat.d(TAG, "We can't ask anything without city!");
			return START_NOT_STICKY;
		}

		// sometimes the API returns different cities for same location, so load first current weather and use its id
		// for request about forecast
		if (getString(R.string.prefs_storage_current_city_key).equals(requestedCity.getId())) {
			if (mWasAskedForId) {
				Logcat.d(TAG, "Already asked for id but waiting for response.");
			} else {
				Logcat.d(TAG, "Ask id for city and use it for forecast");
				mWasAskedForId = true;
				getCurrentWeather(requestedCity);
			}
		} else {
			Logcat.d(TAG, "We have id");
			// which request was requested
			switch (mRequestValue) {
				case REQUEST_VALUE_CURRENT_WEATHER:
					getCurrentWeather(requestedCity);
					break;
				case REQUEST_VALUE_FORECAST:
					getForecastWeather(requestedCity);
					break;
				case REQUEST_VALUE_SAVE_DATA:
					saveDataToDatabase();
					break;
			}
		}

		// TODO Marcel: change in future for synchronization
		return START_NOT_STICKY;
	}

	private boolean isInMemory(AgeAndExceptionData current, CityModel cityModel) {
		if (current == null || current.getId() == null) {
			return false;
		} else if (!current.getId().equals(cityModel.getId())) {
			Logcat.d(TAG, "Data for different city");
			return false;
		} else if (current.shoulReload()) {
			Logcat.d(TAG, "Data are old");
			return false;
		} else {
			return true;
		}
	}

	private boolean isCurrentWeatherInDatabase(CityModel cityModel) {
		CurrentWeatherModel currentWeatherModel = CurrentWeatherDatabase.getInstance(getApplicationContext())
				.getCurrentWeather(cityModel);
		if (currentWeatherModel == null) {
			return false;
		} else if (currentWeatherModel.shoulReload()) {
			Logcat.d(TAG, "Data are old");
			return false;
		} else {
			mCurrentWeather = currentWeatherModel;
			return true;
		}
	}

	private boolean isForecastInDatabase(CityModel cityModel) {
		ForecastWeatherListModel forecast = ForecastDatabase.getInstance(getApplicationContext()).getForecast
				(cityModel);
		if (forecast == null) {
			return false;
		} else if (forecast.shoulReload()) {
			Logcat.d((TAG), "Data are old");
			return false;
		} else {
			mForecast = forecast;
			return true;
		}
	}

	private void getCurrentWeather(final CityModel requestedCity) {
		Logcat.d(TAG, "getCurrentWeather");

		// we already have data for this location
		if (isInMemory(mCurrentWeather, requestedCity)) {
			Logcat.i(TAG, "Information from memory.");
			produceCurrentWeatherLoadedEvent();
		} else if (isCurrentWeatherInDatabase(requestedCity)) {
			Logcat.i(TAG, "Information from database");
			produceCurrentWeatherLoadedEvent();
		} else {

			GetWeatherRequest.getCurrentWeather(requestedCity, getApplicationContext(), new Response.Listener<CurrentWeatherPojo>() {
				@Override
				public void onResponse(CurrentWeatherPojo response) {
					// check server invalid data
					Logcat.i(TAG, "onResponse: " + response.getName());
					if (!"200".equals(response.getCod())) {
						Logcat.e(TAG, "Error in server data: " + response.getCod());
						if (mCurrentWeather == null) {
							mCurrentWeather = new CurrentWeatherModel();
						}
						mCurrentWeather.setIsError(true);
						mCurrentWeather.setErrorText(getString(R.string.network_error_server) + response.getCod());
					} else {
						mCurrentWeather = new CurrentWeatherModel(response);
						CurrentWeatherDatabase.getInstance(getApplicationContext()).saveCurrentWeather
								(mCurrentWeather, requestedCity);
						mLastLoadedCity = response.getCityModel();
						if (mWasAskedForId) {
							// we have city id so we can load forecast
							Logcat.d(TAG, "We have city id so we can load forecast: " + mLastLoadedCity);
							mForecast = null;
							getForecastWeather(mLastLoadedCity);
							mWasAskedForId = false;
						}
						produceCityLoadedEvent();
					}
					produceCurrentWeatherLoadedEvent();

				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					Logcat.e(TAG, "onError: " + error.getMessage());
					error.printStackTrace();
					if (mCurrentWeather == null) {
						mCurrentWeather = new CurrentWeatherModel();
					}
					mCurrentWeather.setIsError(true);
					mCurrentWeather.setErrorText(getMessageError(error));
					produceCurrentWeatherLoadedEvent();

				}
			});
		}
	}

	private void getForecastWeather(final CityModel requestedCity) {
		Logcat.d(TAG, "getForecastWeather");

		// we already have data for this location
		if (isInMemory(mForecast, requestedCity)) {
			Logcat.d(TAG, "Information from memory.");
			produceForecastLoadedEvent();
		} else if (isForecastInDatabase(requestedCity)) {
			Logcat.d(TAG, "Data from database");
			produceForecastLoadedEvent();
		} else {

			GetForecastRequest.getForecastWeather(requestedCity, getApplicationContext(), new Response.Listener<ForecastPojo>() {
				@Override
				public void onResponse(ForecastPojo response) {
					Logcat.d(TAG, "onResponse: " + response.getCity());
					if (!"200".equals(response.getCod())) {
						Logcat.e(TAG, "Error in server data: " + response.getCod());
						if (mForecast == null) {
							mForecast = new ForecastWeatherListModel();
						}
						mForecast.setErrorText(getString(R.string.network_error_server) + response.getCod());
						mForecast.setIsError(true);
					} else {
						mForecast = new ForecastWeatherListModel(response, " " + getString(R.string.forecast_day_preposition)
								+ " ");
						ForecastDatabase.getInstance(getApplicationContext()).saveForecast(mForecast, requestedCity);
						mLastLoadedCity = response.getCityModel();
						produceCityLoadedEvent();
					}
					produceForecastLoadedEvent();

				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					error.printStackTrace();
					if (mForecast == null) {
						mForecast = new ForecastWeatherListModel();
					}
					mForecast.setIsError(true);
					mForecast.setErrorText(getMessageError(error));
					produceForecastLoadedEvent();
				}
			});
		}
	}

	private void produceCityLoadedEvent() {
//		Logcat.d(TAG, "produceCityLoadedEvent: " + mLastLoadedCity);
		mBus.post(new CityLoadedEvent(mLastLoadedCity));
	}

	private void produceCurrentWeatherLoadedEvent() {
//		Logcat.d(TAG, "produceCurrentWeatherLoadedEvent: " + mCurrentWeather);
		mBus.post(new CurrentWeatherLoadedEvent(mCurrentWeather));
	}

	private void produceForecastLoadedEvent() {
//		Logcat.d(TAG, "produceForecastLoadedEvent: " + mForecast);
		mBus.post(new ForecastLoadedEvent(mForecast));
	}

	private void saveDataToDatabase() {
		Logcat.d(TAG, "saveDataToDatabase");
		CurrentWeatherDatabase.getInstance(getApplicationContext()).saveCurrentWeather(mCurrentWeather, mLastLoadedCity);
		ForecastDatabase.getInstance(getApplicationContext()).saveForecast(mForecast, mLastLoadedCity);
	}

	private String getMessageError(VolleyError error) {
		if (error instanceof TimeoutError) {
			return getString(R.string.network_error_timeout);
		} else if (error instanceof NoConnectionError && error.getCause() instanceof UnknownHostException) {
			return getString(R.string.network_error_no_connection);
		} else if (error instanceof NoConnectionError && error.getCause() instanceof IOException) {
			return getString(R.string.network_error_invalid_request);
		} else if (error instanceof ParseError) {
			return getString(R.string.network_error_parse);
		} else {
			return getString(R.string.network_error_general);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Logcat.e(TAG, "onDestroy");
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
