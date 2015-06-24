package com.marcelsyrucek.weather.database.model;

import com.marcelsyrucek.weather.database.JsonRecord;
import com.marcelsyrucek.weather.pojo.forecast.ForecastPojo;
import com.marcelsyrucek.weather.pojo.forecast.List;
import com.marcelsyrucek.weather.pojo.forecast.Weather;
import com.marcelsyrucek.weather.utility.Logcat;
import com.marcelsyrucek.weather.utility.WeatherUtility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by marcel on 23.6.2015.
 */
public class ForecastWeatherModelList extends AgeAndExceptionData implements JsonRecord {

	public static final String TAG = ForecastWeatherModelList.class.getSimpleName();

	private String mId;
	private String mName;
	private ArrayList<ForecastWeatherModel> mDays = new ArrayList<>();

	public ForecastWeatherModelList() {
	}

	public ForecastWeatherModelList(ForecastPojo pojo, String preposition) {
		if (pojo.getCity() != null) {
			mId = pojo.getCity().getId() + "";
			mName = pojo.getCityWithCountry();
		}

		ForecastWeatherModel dayForecast;
		Weather weather;

		long timeInMillis;
		String description;
		String dayName;

		for (List day : pojo.getList()) {
			weather = day.getWeather().get(0);

			// get day of week and make description
			timeInMillis = day.getDt();
			dayName = WeatherUtility.getDate(timeInMillis);
			description = WeatherUtility.capitalizeFirstChar(weather.getDescription()) + preposition + dayName;
//			Logcat.d(TAG, "Description: " + description);

			dayForecast = new ForecastWeatherModel(weather.getIcon(), description, day.getTemp().getDay());
			mDays.add(dayForecast);
		}

	}

	public ArrayList<ForecastWeatherModel> getDays() {
		return mDays;
	}

	@Override
	public String getId() {
		return mId;
	}

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public Object getObject() {
		return this;
	}

	@Override
	public String toString() {
		return "Forecast for city " + mName + " in size " + mDays.size();
	}
}
