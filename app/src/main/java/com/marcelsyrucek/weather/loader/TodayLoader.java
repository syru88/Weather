package com.marcelsyrucek.weather.loader;

import android.content.Context;
import android.location.Location;
import android.support.v4.content.AsyncTaskLoader;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.WeatherApplication;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.database.model.CurrentWeatherModel;
import com.marcelsyrucek.weather.event.LoadCityEvent;
import com.marcelsyrucek.weather.pojo.WebErrorException;
import com.marcelsyrucek.weather.pojo.currentweather.CurrentWeather;
import com.marcelsyrucek.weather.request.GetWeatherRequest;
import com.marcelsyrucek.weather.utility.Logcat;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * Created by marcel on 16.6.2015.
 */
public class TodayLoader extends AsyncTaskLoader<CurrentWeatherModel> {

	public static final String TAG = TodayLoader.class.getSimpleName();

	private CurrentWeatherModel mCurrentWeatherModel;
	private CityModel mCurrentCity;

	private Bus mBus = WeatherApplication.bus;

	public TodayLoader(Context context) {
		super(context);
		Logcat.e(TAG, "CONSTRUCTOR LOADER");
		mBus.register(this);

	}

	@Subscribe
	/**
	 * Subscribe this method for listening on {@link LoadCityEvent} which happens when we have old location from
	 * database but the new location is different. So user doesn't wait for position or network neither.
	 */
	public void subscribeOnRefreshEvent(LoadCityEvent event) {
		CityModel newCity = event.getCityModel();
		Logcat.e(TAG, "LOADER!!! " + newCity.toString());

		// check if it is different city than we already have
		if (newCity.equals(mCurrentCity) == false) {
			Logcat.e(TAG, "different cities");
			mCurrentCity = newCity;
			onContentChanged();
		}

	}

	@Override
	protected void onStartLoading() {
		Logcat.e(TAG, "onStartLoading");

		if (mCurrentWeatherModel != null) {
			deliverResult(mCurrentWeatherModel);
		} else if (mCurrentCity != null){
			forceLoad();
		}
	}

	@Override
	public void deliverResult(CurrentWeatherModel data) {
		Logcat.d(TAG, "deliverResult");
		if (isReset()) {
			releaseResources(data);
			return;
		}

		CurrentWeatherModel oldData = mCurrentWeatherModel;
		mCurrentWeatherModel = data;

		if (isStarted()) {
			super.deliverResult(data);
		}

		if (oldData != null && oldData != data) {
			releaseResources(oldData);
		}
	}

	@Override
	public CurrentWeatherModel loadInBackground() {
		Logcat.e(TAG, "loadInBackGround");

		CurrentWeather pojo = null;
		CurrentWeatherModel dao = null;

		try {
			pojo = GetWeatherRequest.getCityWeather(mCurrentCity, getContext());
			dao = new CurrentWeatherModel(pojo);
		} catch (WebErrorException e) {
			Logcat.e(TAG, "Server valid state but with error: " + e.getMessage());
			dao = new CurrentWeatherModel(mCurrentWeatherModel);
			dao.setIsError(true);
			dao.setErrorText(e.getMessage());
		} catch (Exception e) {
			Logcat.e(TAG, "Problem with server, network...");
			e.printStackTrace();
			dao = new CurrentWeatherModel(mCurrentWeatherModel);
			dao.setIsError(true);
			dao.setErrorText(getContext().getString(R.string.general_error_network_message));
		}

		Logcat.d(TAG, "loadInBackGround End");

		return dao;
	}

	@Override
	protected void onStopLoading() {
		Logcat.e(TAG, "onStopLoading");
		cancelLoad();
	}

	@Override
	protected void onReset() {
		Logcat.e(TAG, "onReset");
		onStopLoading();

		// At this point we can release the resources associated with 'mData'.
		if (mCurrentWeatherModel != null) {
			releaseResources(mCurrentWeatherModel);
			mCurrentWeatherModel = null;
		}

	}

	@Override
	public void onCanceled(CurrentWeatherModel data) {
		Logcat.e(TAG, "onCancel");
		super.onCanceled(data);

		releaseResources(data);
	}

	private void releaseResources(CurrentWeatherModel data) {
	}
}
