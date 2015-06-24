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
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.database.model.CurrentWeatherModel;
import com.marcelsyrucek.weather.database.model.ForecastWeatherModelList;
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

	private static final int INACTIVITY_TIME_IN_MILISECONDS = 1000 * 60 * 2; // 2 minutes

	/** when true we know that application is still active so don't stop service */
	private boolean mServiceWasCalled;

	private CityModel mShownCity;
	private int mRequestValue;
	private CurrentWeatherModel mCurrentWeather;
	private ForecastWeatherModelList mForecast;

	private Bus mBus = WeatherApplication.bus;

	@Override
	public void onCreate() {
		super.onCreate();
		Logcat.e(TAG, "onCreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		CityModel requestedCity = (CityModel) intent.getSerializableExtra(EXTRA_CITY);
		mRequestValue = intent.getIntExtra(EXTRA_REQUEST, REQUEST_VALUE_CURRENT_WEATHER);

		Logcat.e(TAG, "onStartCommand: " + requestedCity + ", request: " + mRequestValue + ", shown: " + mShownCity);

		if (requestedCity == null && mShownCity == null) {
			Logcat.e(TAG, "Both cities null, so go away and don't contact server");
			return START_NOT_STICKY;
		}

		if (requestedCity != null) {
			Logcat.e(TAG, "Call from Activity!!!");
		}

		if (requestedCity == null) {
			Logcat.d(TAG, "Called from fragment for refresh or first connection");
			requestedCity = mShownCity;
		}

		// which request was requested
		switch (mRequestValue) {
			case REQUEST_VALUE_CURRENT_WEATHER:
				getCurrentWeather(requestedCity);
				break;
			case REQUEST_VALUE_FORECAST:
				getForecastWeather(requestedCity);
				break;
		}

		// TODO Marcel: change in future for synchronization
		return START_NOT_STICKY;
	}

	private void getCurrentWeather(CityModel requestedCity) {
		Logcat.d(TAG, "getCurrentWeather");

		// we already have data for this location
		if (mCurrentWeather != null && requestedCity.equals(mShownCity)) {
			Logcat.d(TAG, "Not load, send data");
			produceCurrentWeatherLoadedEvent();
			return;
		}

		GetWeatherRequest.getCurrentWeather(requestedCity, getApplicationContext(), new Response.Listener<CurrentWeatherPojo>() {
			@Override
			public void onResponse(CurrentWeatherPojo response) {
				// check server invalid data
				Logcat.e(TAG, "onResponse: " + response.getName());
				if (!"200".equals(response.getCod())) {
					Logcat.e(TAG, "Error in server data: " + response.getCod());
					if (mCurrentWeather == null) {
						mCurrentWeather = new CurrentWeatherModel();
					}
					mCurrentWeather.setIsError(true);
					mCurrentWeather.setErrorText(getString(R.string.network_error_server) + response.getCod());
				} else {
					mCurrentWeather = new CurrentWeatherModel(response);
					mShownCity = response.getCityModel();
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

	private void getForecastWeather(final CityModel requestedCity) {
		Logcat.d(TAG, "getForecastWeather");

		// we already have data for this location
		if (mForecast != null && requestedCity.equals(mShownCity)) {
			Logcat.d(TAG, "Not load, send data");
			produceForecastLoadedEvent();
			return;
		}

		GetForecastRequest.getForecastWeather(requestedCity, getApplicationContext(), new Response.Listener<ForecastPojo>() {
			@Override
			public void onResponse(ForecastPojo response) {
				Logcat.d(TAG, "onResponse: " + response.getCity());
				if (!"200".equals(response.getCod())) {
					Logcat.e(TAG, "Error in server data: " + response.getCod());
					if (mForecast == null) {
						mForecast = new ForecastWeatherModelList();
					}
					mForecast.setErrorText(getString(R.string.network_error_server) + response.getCod());
					mForecast.setIsError(true);
				} else {
					mForecast = new ForecastWeatherModelList(response, " " + getString(R.string.forecast_day_preposition)
							+ " ");
					mShownCity = response.getCityModel();
					produceCityLoadedEvent();
				}
				produceForecastLoadedEvent();

			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				error.printStackTrace();
				if (mForecast == null) {
					mForecast = new ForecastWeatherModelList();
				}
				mForecast.setIsError(true);
				mForecast.setErrorText(getMessageError(error));
				produceForecastLoadedEvent();
			}
		});
	}

	private void produceCityLoadedEvent() {
		Logcat.d(TAG, "produceCityLoadedEvent: " + mShownCity);
		mBus.post(new CityLoadedEvent(mShownCity));
	}

	private void produceCurrentWeatherLoadedEvent() {
		Logcat.d(TAG, "produceCurrentWeatherLoadedEvent: " + mCurrentWeather);
		mBus.post(new CurrentWeatherLoadedEvent(mCurrentWeather));
	}

	private void produceForecastLoadedEvent() {
		Logcat.d(TAG, "produceForecastLoadedEvent: " + mForecast);
		mBus.post(new ForecastLoadedEvent(mForecast));
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
