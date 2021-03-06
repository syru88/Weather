package com.marcelsyrucek.weather;

/**
 * Created by marcel on 21.6.2015.
 */
public class WeatherConfig {
	public static final boolean LOGS = BuildConfig.LOGS;
	public static final boolean DEV_API = BuildConfig.DEV_API;

	public static final int NETWORK_TIMEOUT_IN_MILLISECONDS = 10 * 1000;

	public static final String OPEN_WEATHER_MAP_APP_ID = "6f5b024d603a3c8dc3fcdc1ec7674b25";
	public static final String API_APPID = "&APPID=" + OPEN_WEATHER_MAP_APP_ID;
	public static final String API_ENDPOINT_PRODUCTION = "http://api.openweathermap.org/data/";
	public static final String API_IMAGES = "http://openweathermap.org/img/w/";
	public static final String API_IMAGES_TYPE = ".png";

	public static final int DISTANCE_LOCATION_ACCURACY_IN_METERS = 10 * 1000; // 10 km
	public static final int CURRENT_LOCATION_TIMEOUT_IN_MILLIS = 20 * 1000;
	public static final long DATA_VALID_FOR_IN_MILLIS = 2 * 60 * 1000; // 2 minute


}
