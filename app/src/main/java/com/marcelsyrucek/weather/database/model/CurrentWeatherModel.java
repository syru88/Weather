package com.marcelsyrucek.weather.database.model;

import com.marcelsyrucek.weather.WeatherConfig;
import com.marcelsyrucek.weather.database.JsonRecord;
import com.marcelsyrucek.weather.pojo.currentweather.CurrentWeatherPojo;
import com.marcelsyrucek.weather.utility.WeatherUtility;

/**
 * Created by marcel on 15.6.2015.
 */
public class CurrentWeatherModel extends AgeAndExceptionData implements JsonRecord {

	private String mId;

	private String mCity, mDescription, mIconUrl;
	private double mTemperature, mWindSpeed, mWindDirection;
	private double mHumidity, mPrecipitation, mPressure;

	public CurrentWeatherModel() {
	}

	public CurrentWeatherModel(CurrentWeatherModel newWeather) {
		mId = newWeather.mId;
		mCity = newWeather.mCity;
		mDescription = newWeather.mDescription;
		mIconUrl = newWeather.mIconUrl;

		mTemperature = newWeather.mTemperature;
		mWindSpeed = newWeather.mWindSpeed;
		mWindDirection = newWeather.mWindDirection;

		mHumidity = newWeather.mHumidity;
		mPrecipitation = newWeather.mPrecipitation;
		mPressure = newWeather.mPressure;
	}

	public CurrentWeatherModel(CurrentWeatherPojo pojo) {
		mId = String.valueOf(pojo.getId());
		mCity = pojo.getNameWithCountry();
		// according to API documentation many parameters are optional, so we need to check it for null references :-(
		if (pojo.getWeather() != null && pojo.getWeather().size() > 0 && pojo.getWeather().get(0) != null) {
			mDescription = pojo.getWeather().get(0).getDescription();
			mIconUrl = WeatherConfig.API_IMAGES + pojo.getWeather().get(0).getIcon() + WeatherConfig.API_IMAGES_TYPE;
		}
		mTemperature = pojo.getMain().getTemp();

		mHumidity = pojo.getMain().getHumidity();
		if (pojo.getRain() != null) {
			mPrecipitation = pojo.getRain().get3h();
		}
		mPressure = (int) pojo.getMain().getPressure();
		if (pojo.getWind() != null) {
			mWindSpeed = pojo.getWind().getSpeed();
			mWindDirection = pojo.getWind().getDeg();
		}

	}

	@Override
	public String toString() {
		return "CurrentWeatherModel: " + mId + ", city: " + mCity + ", error: " + getErrorText();
	}

	public String getCity() {
		return mCity;
	}

	public void setCity(String city) {
		mCity = city;
	}

	public String getDescription() {
		return WeatherUtility.capitalizeFirstLetters(mDescription);
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public String getIconUrl() {
		return mIconUrl;
	}

	public void setIconUrl(String iconUrl) {
		mIconUrl = iconUrl;
	}

	public double getTemperature() {
		return mTemperature;
	}

	public void setTemperature(double temperature) {
		mTemperature = temperature;
	}

	public double getHumidity() {
		return mHumidity;
	}

	public void setHumidity(int humidity) {
		mHumidity = humidity;
	}

	public double getPrecipitation() {
		return mPrecipitation;
	}

	public void setPrecipitation(int precipitation) {
		mPrecipitation = precipitation;
	}

	public double getPressure() {
		return mPressure;
	}

	public void setPressure(int pressure) {
		mPressure = pressure;
	}

	public double getWindSpeed() {
		return mWindSpeed;
	}

	public void setWindSpeed(double windSpeed) {
		mWindSpeed = windSpeed;
	}

	public String getWindDirection() {
		return WeatherUtility.getWindDirection(mWindDirection);
	}

	public void setWindDirection(int windDirection) {
		mWindDirection = windDirection;
	}

	@Override
	public String getId() {
		return mId;
	}

	@Override
	public String getName() {
		return mCity;
	}

	@Override
	public Object getObject() {
		return this;
	}

	public void setId(String id) {
		mId = id;
	}
}
