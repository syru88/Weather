package com.marcelsyrucek.weather.event;

import com.marcelsyrucek.weather.database.model.CityModel;

/**
 * Created by marcel on 22.6.2015.
 */
public class CityLoadedEvent {

	private CityModel mCityModel;

	public CityLoadedEvent(CityModel cityModel) {
		mCityModel = cityModel;
	}

	public CityModel getCityModel() {
		return mCityModel;
	}
}
