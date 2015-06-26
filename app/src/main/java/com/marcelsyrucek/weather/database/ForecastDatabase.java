package com.marcelsyrucek.weather.database;

import android.content.Context;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.database.model.ForecastWeatherListModel;
import com.marcelsyrucek.weather.utility.Logcat;

/**
 * Created by marcel on 26.6.2015.
 */
public class ForecastDatabase {

	public static final String TAG = ForecastDatabase.class.getSimpleName();

	private static ForecastDatabase sInstance;

	private Context mContext;
	private GeneralDatabase<ForecastWeatherListModel> mDatabase;

	public static ForecastDatabase getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ForecastDatabase(context);
		}
		return sInstance;
	}

	private ForecastDatabase(Context context) {
		mContext = context;
		mDatabase = new GeneralDatabase<>(mContext, mContext.getString(R.string.prefs_forecast_storage),
				ForecastWeatherListModel.class);
	}

	public boolean saveForecast(ForecastWeatherListModel forecast, CityModel cityModel) {
		Logcat.d(TAG, "saveForecast: " + forecast + ", city: " + cityModel);

		if (mContext.getString(R.string.prefs_storage_current_city_key).equals(cityModel.getId())) {
			return false;
		} else if (CityDatabase.getInstance(mContext).isCityInDatabase(cityModel)) {
			mDatabase.editEntry(forecast);
			return true;
		}

		return false;
	}

	public ForecastWeatherListModel getForecast(CityModel cityModel) {
		Logcat.d(TAG, "getForecast: " + cityModel);
		return mDatabase.getEntryWithId(cityModel.getId());
	}

	public boolean deleteForecast(CityModel cityModel) {
		Logcat.d(TAG, "deleteForecast: " + cityModel);
		return mDatabase.removeEntryWithId(cityModel.getId());
	}
}
