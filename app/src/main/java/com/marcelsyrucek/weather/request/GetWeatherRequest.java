package com.marcelsyrucek.weather.request;

import android.content.Context;
import android.location.Location;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.VolleyWrapper;
import com.marcelsyrucek.weather.WeatherConfig;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.pojo.WebErrorException;
import com.marcelsyrucek.weather.pojo.currentweather.CurrentWeather;
import com.marcelsyrucek.weather.utility.GsonRequest;
import com.marcelsyrucek.weather.utility.Logcat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by marcel on 18.6.2015.
 */
public class GetWeatherRequest {

	public static final String TAG = GetWeatherRequest.class.getSimpleName();

	private static final String VERSION = "2.5";

	public static CurrentWeather getCityWeather(CityModel city, Context context) throws Exception {
		StringBuilder url = new StringBuilder(300);
		url.append(WeatherConfig.API_ENDPOINT_PRODUCTION + VERSION);

		if (city == null) {
			throw new Exception("The city object is null which shouldn't happen!!!");
		}

		if (city.getId() != null && context.getString(R.string.menu_menu_current_position).equals(city.getId()) == false) {
			url.append("/weather?id=");
			url.append(city.getId());
		}  else if (city.getLatitude() != 0 || city.getLongitude() != 0) {
			url.append("/weather?lat=");
			url.append(city.getLatitude());
			url.append("&lon=");
			url.append(city.getLongitude());
		} else {
			url.append("/weather?q=");
			url.append(city.getName());
		}

		url.append(WeatherConfig.API_APPID);

		return getWeather(url.toString(), context);
	}

	private static CurrentWeather getWeather(String url, Context context) throws InterruptedException, ExecutionException, TimeoutException, WebErrorException {
		Logcat.d(TAG, "url: " + url);

		// TODO Marcel: whitespace out!!!
		RequestQueue requestQueue = VolleyWrapper.getInstance(context).getRequestQueue();
		RequestFuture<CurrentWeather> future = RequestFuture.newFuture();
		GsonRequest<CurrentWeather> request = new GsonRequest<CurrentWeather>(Request.Method.GET, url, CurrentWeather
				.class, future, future/*new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Logcat.e(TAG, "PRUSER: " + error.getMessage());
			}
		}*/);
		future.setRequest(request);
		requestQueue.add(request);

		CurrentWeather currentWeather = future.get(WeatherConfig.NETWORK_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

		if (currentWeather.getMessage() != null) {
			throw new WebErrorException(currentWeather.getMessage());
		}

		return currentWeather;
	}

}
