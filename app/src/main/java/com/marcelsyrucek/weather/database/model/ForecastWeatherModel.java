package com.marcelsyrucek.weather.database.model;

import com.marcelsyrucek.weather.WeatherConfig;
import com.marcelsyrucek.weather.database.JsonRecord;
import com.marcelsyrucek.weather.utility.Logcat;

/**
 * Created by marcel on 23.6.2015.
 */
public class ForecastWeatherModel {

	private String mIconUrl, mDescription;
	private double mTemperature;

	public ForecastWeatherModel(String iconUrl, String description, double temperature) {
		mIconUrl = WeatherConfig.API_IMAGES + iconUrl + WeatherConfig.API_IMAGES_TYPE;
		mDescription = description;
		mTemperature = temperature;
	}

	public String getIconUrl() {
		return mIconUrl;
	}

	public String getDescription() {
		return mDescription;
	}

	public double getTemperature() {
		return mTemperature;
	}
}
