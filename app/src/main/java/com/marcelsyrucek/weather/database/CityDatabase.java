package com.marcelsyrucek.weather.database;

import android.content.Context;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.utility.Logcat;

import java.util.ArrayList;

/**
 * This class behaves as database for saving cities. For internal implementation it uses Shared Preferences storage.
 */
public class CityDatabase {

	public static final String TAG = CityDatabase.class.getSimpleName();

	private static CityDatabase sInstance;

	private Context mContext;
	private GeneralDatabase<CityModel> mDatabase;

	public static CityDatabase getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new CityDatabase(context);
		}
		return sInstance;
	}

	private CityDatabase(Context context) {
		mContext = context.getApplicationContext();
		mDatabase = new GeneralDatabase<>(mContext, mContext.getString(R.string.prefs_cities_storage), CityModel.class);

		// prepare data for showing city with last current position
		CityModel cityModel = new CityModel();
		cityModel.setName(mContext.getString(R.string.menu_menu_current_position));
		mDatabase.addEntry(mContext.getString(R.string.prefs_storage_current_city), cityModel);

	}

	public ArrayList<CityModel> getCities() {
		return mDatabase.getEntries();
	}

	public boolean addCity(CityModel city) {
		return mDatabase.addEntry(city);
	}

	public boolean removeCity(CityModel city) {
		return mDatabase.removeEntry(city);
	}

	public CityModel getCityWithCurrentPosition() {
		CityModel cityModel = mDatabase.getEntryWithId(mContext.getString(R.string.prefs_storage_current_city));
		if (cityModel.getId() == null) {
			Logcat.e(TAG, "first start of application");
			return null;
		} else {
			return cityModel;
		}
	}

	public void editCurrentCity(CityModel cityModel) {
		cityModel.setId(mContext.getString(R.string.prefs_storage_current_city));
		mDatabase.editEntry(mContext.getString(R.string.prefs_storage_current_city), cityModel);
	}

	public boolean isCityInDatabase(CityModel cityModel) {
		if (cityModel == null) {
			return false;
		}
		return mDatabase.isEntryInDatabase(cityModel.getId());
	}

}
