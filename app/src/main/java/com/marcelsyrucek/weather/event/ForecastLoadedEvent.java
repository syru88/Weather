package com.marcelsyrucek.weather.event;

import com.marcelsyrucek.weather.database.model.ForecastWeatherListModel;

/**
 * Created by marcel on 23.6.2015.
 */
public class ForecastLoadedEvent {

	private ForecastWeatherListModel mForecastWeatherListModel;

	public ForecastLoadedEvent(ForecastWeatherListModel forecastWeatherListModel) {
		mForecastWeatherListModel = forecastWeatherListModel;
	}

	public ForecastWeatherListModel getForecastWeatherListModel() {
		return mForecastWeatherListModel;
	}
}
