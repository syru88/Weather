package com.marcelsyrucek.weather.database.model;

import android.content.Context;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.database.JsonRecord;
import com.marcelsyrucek.weather.utility.GeoLocationManager;

import java.io.Serializable;

/**
 * This class is used as city entity for storing in database.
 */
// Using Serializable in this case is OK for performance.
public class CityModel implements Serializable, JsonRecord {

	public static final String TAG = CityModel.class.getSimpleName();

	public static final int CURRENT_CITY = 0;
	public static final int SEARCH_CITY = 1;
	public static final int ID_CITY = 2;


	private String mName;
	private String mId;
	private double mLatitude;
	private double mLongitude;

	private boolean mIsSelected;

	public CityModel() {
	}

	public CityModel(String name) {
		mName = name;
	}

	public String getName() {
		return mName;
	}

	@Override
	public Object getObject() {
		return this;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}


	public double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(double latitude) {
		mLatitude = latitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}

	public boolean isSelected() {
		return mIsSelected;
	}

	public void setIsSelected(boolean isSelected) {
		mIsSelected = isSelected;
	}

	@Override
	public String toString() {
		return "City: " + mName + ", id: " + mId + ", lat: " + mLatitude + ", long: " +
				mLongitude;
	}

	public int getCityType(Context context) {
		if (context.getString(R.string.prefs_storage_current_city_key).equals(mId)) {
			return CURRENT_CITY;
		} else if (context.getString(R.string.prefs_search_city_key).equals(mId)) {
			return SEARCH_CITY;
		} else {
			return ID_CITY;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CityModel) {
			return GeoLocationManager.isLocationInArea(mLatitude, mLongitude, (CityModel) o, new float[3]);
		} else {
			return false;
		}
	}

}
