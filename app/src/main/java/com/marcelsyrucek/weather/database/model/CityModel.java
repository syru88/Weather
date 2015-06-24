package com.marcelsyrucek.weather.database.model;

import com.marcelsyrucek.weather.database.JsonRecord;
import com.marcelsyrucek.weather.utility.GeoLocationManager;

import java.io.Serializable;

/**
 * This class is used as city entity for storing in database.
 */
// Using Serializable in this case is OK for performance.
public class CityModel implements Serializable, JsonRecord {

	public static final String TAG = CityModel.class.getSimpleName();

	private String mName;
	private String mId;
	private double mLatitude;
	private double mLongitude;

	private boolean mIsCurrent;

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

	public boolean isCurrent() {
		return mIsCurrent;
	}

	public void setIsCurrent(boolean isCurrent) {
		mIsCurrent = isCurrent;
	}

	@Override
	public String toString() {
		return "IsCur: " + mIsCurrent + "City: " + mName + ", id: " + mId + ", lat: " + mLatitude + ", long: " +
				mLongitude;
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
