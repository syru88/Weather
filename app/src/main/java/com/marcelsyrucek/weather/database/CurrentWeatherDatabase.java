package com.marcelsyrucek.weather.database;

import android.content.Context;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.database.model.CurrentWeatherModel;

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
}
