package com.marcelsyrucek.weather.event;

import com.marcelsyrucek.weather.database.model.CityModel;

/**
 * Created by marcel on 19.6.2015.
 */
public class LoadCityEvent {
	private CityModel mCityModel;

	public LoadCityEvent(CityModel cityModel) {
		mCityModel = cityModel;
	}

	public CityModel getCityModel() {
		return mCityModel;
	}
}
