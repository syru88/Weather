package com.marcelsyrucek.weather.database.model;

import com.marcelsyrucek.weather.database.JsonRecord;
import com.marcelsyrucek.weather.pojo.forecast.ForecastPojo;

import java.util.ArrayList;

/**
 * Created by marcel on 23.6.2015.
 */
public class ForecastWeatherModelList extends AgeAndExceptionData implements JsonRecord {

	private String mId;
	private String mName;
	private ArrayList<ForecastWeatherModel> mDays = new ArrayList<>();

	public ForecastWeatherModelList() {
	}

	public ForecastWeatherModelList(ForecastPojo pojo) {

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
