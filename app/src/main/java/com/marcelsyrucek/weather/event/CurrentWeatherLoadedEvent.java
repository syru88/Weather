package com.marcelsyrucek.weather.event;

import com.marcelsyrucek.weather.database.model.CurrentWeatherModel;

/**
 * Created by marcel on 23.6.2015.
 */
public class CurrentWeatherLoadedEvent {
	private CurrentWeatherModel mCurrentWeatherModel;

	public CurrentWeatherLoadedEvent(CurrentWeatherModel currentWeatherModel) {
		mCurrentWeatherModel = currentWeatherModel;
	}

	public CurrentWeatherModel getCurrentWeatherModel() {
		return mCurrentWeatherModel;
	}
}
