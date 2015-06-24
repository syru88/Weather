package com.marcelsyrucek.weather.event;

import com.marcelsyrucek.weather.database.model.CityModel;

/**
 * Created by marcel on 24.6.2015.
 */
public class CityClickedEvent {

	private CityModel mCityModel;

	public CityClickedEvent(CityModel cityModel) {
		mCityModel = cityModel;
	}

	public CityModel getCityModel() {
		return mCityModel;
	}
}
