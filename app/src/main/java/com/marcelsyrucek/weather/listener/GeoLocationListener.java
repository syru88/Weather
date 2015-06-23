package com.marcelsyrucek.weather.listener;

import android.location.Location;

/**
 * Used for receiving notification from {@link com.marcelsyrucek.weather.utility.GeoLocationManager GeoLocationManager}
 * when location is
 * available.
 */
public interface GeoLocationListener {

	void lastKnownLocation(Location location);

}
