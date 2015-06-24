package com.marcelsyrucek.weather.request;

import android.content.Context;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.database.model.CityModel;

/**
 * Created by marcel on 23.6.2015.
 */
public class UtilityRequest {

	public static final String TAG = UtilityRequest.class.getSimpleName();

	public static String getSearchCondition(CityModel city, Context context) {
		StringBuilder searchCondition = new StringBuilder(300);

		if (city == null) {
			throw new IllegalArgumentException("The city object is null which shouldn't happen!!!");
		}

		if (city.getId() != null && !city.isCurrent()) {
			searchCondition.append("id=");
			searchCondition.append(city.getId());
		} else if (city.getLatitude() != 0 || city.getLongitude() != 0) {
			searchCondition.append("lat=");
			searchCondition.append(city.getLatitude());
			searchCondition.append("&lon=");
			searchCondition.append(city.getLongitude());
		} else {
			searchCondition.append("q=");
			searchCondition.append(city.getName());
		}

		return searchCondition.toString().trim();
	}
}
