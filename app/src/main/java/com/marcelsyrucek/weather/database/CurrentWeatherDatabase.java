package com.marcelsyrucek.weather.database;

import android.content.Context;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.database.model.CurrentWeatherModel;
import com.marcelsyrucek.weather.utility.Logcat;

/**
 * Created by Marcel on 25.6.2015.
 */
public class CurrentWeatherDatabase {

	public static final String TAG = CurrentWeatherDatabase.class.getSimpleName();

	private static CurrentWeatherDatabase sInstance;

	private Context mContext;
	private GeneralDatabase<CurrentWeatherModel> mDatabase;

	public static CurrentWeatherDatabase getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new CurrentWeatherDatabase(context);
		}
		return sInstance;
	}

	private CurrentWeatherDatabase(Context context) {
		mContext = context;
		mDatabase = new GeneralDatabase<>(mContext, mContext.getString(R.string.prefs_current_weather_storage), CurrentWeatherModel.class);
	}

	public boolean saveCurrentWeather(CurrentWeatherModel currentWeatherModel, CityModel cityModel) {

		if (mContext.getString(R.string.prefs_storage_current_city_key).equals(cityModel.getId())) {
			return false;
		} else if (CityDatabase.getInstance(mContext).isCityInDatabase(cityModel)) {
			Logcat.d(TAG, "saveCurrentWeather: " + currentWeatherModel + ", city: " + cityModel);
			return mDatabase.editEntry(currentWeatherModel);
		} else {
			return false;
		}
	}

	public CurrentWeatherModel getCurrentWeather(CityModel cityModel) {
		Logcat.d(TAG, "getCurrentWeather: " + cityModel);
		return mDatabase.getEntryWithId(cityModel.getId());
	}

	public boolean deleteCurrentWeather(CityModel cityModel) {
		Logcat.d(TAG, "deleteCurrentWeather: " + cityModel);
		return mDatabase.removeEntryWithId(cityModel.getId());
	}
}
