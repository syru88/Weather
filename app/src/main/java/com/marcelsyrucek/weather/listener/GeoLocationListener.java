package com.marcelsyrucek.weather.listener;

import android.app.Activity;
import android.location.Location;

/**
 * Used for receiving notification from {@link com.marcelsyrucek.weather.utility.GeoLocationManager GeoLocationManager}
 * when location is
 * available.
 */
public interface GeoLocationListener {

	void onLocationChanged(Location location);

	void onRequestLocationFailed(String errorMesage);

	Activity getActivity();

}
