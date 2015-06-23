package com.marcelsyrucek.weather.event;

import com.marcelsyrucek.weather.database.model.ForecastWeatherModelList;

/**
 * Created by marcel on 23.6.2015.
 */
public class ForecastLoadedEvent {

	private ForecastWeatherModelList mForecastWeatherModelList;

	public ForecastLoadedEvent(ForecastWeatherModelList forecastWeatherModelList) {
		mForecastWeatherModelList = forecastWeatherModelList;
	}

	public ForecastWeatherModelList getForecastWeatherModelList() {
		return mForecastWeatherModelList;
	}
}
