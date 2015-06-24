package com.marcelsyrucek.weather.request;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.VolleyWrapper;
import com.marcelsyrucek.weather.WeatherConfig;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.pojo.currentweather.CurrentWeatherPojo;
import com.marcelsyrucek.weather.utility.GsonRequest;
import com.marcelsyrucek.weather.utility.Logcat;

/**
 * Created by marcel on 18.6.2015.
 */
public class GetWeatherRequest {

	public static final String TAG = GetWeatherRequest.class.getSimpleName();

	private static final String VERSION = "2.5";

	public static void getCurrentWeather(CityModel city, Context context, Response.Listener<CurrentWeatherPojo>
			listener, Response.ErrorListener errorListener) {
		StringBuilder url = new StringBuilder(300);

		url.append(WeatherConfig.API_ENDPOINT_PRODUCTION + VERSION + "/weather?");
		url.append(UtilityRequest.getSearchCondition(city, context));
		url.append(WeatherConfig.API_APPID);

		getCurrentWeather(url.toString(), context, listener, errorListener);
	}

	private static void getCurrentWeather(String url, Context context, Response.Listener<CurrentWeatherPojo>
			listener, Response.ErrorListener errorListener) {
		url = url.replaceAll("\\s", "");
		Logcat.d(TAG, "url: " + url);

		RequestQueue requestQueue = VolleyWrapper.getInstance(context).getRequestQueue();
		GsonRequest<CurrentWeatherPojo> request = new GsonRequest<CurrentWeatherPojo>(Request.Method.GET, url, CurrentWeatherPojo
				.class, listener, errorListener);
		request.setRetryPolicy(new DefaultRetryPolicy(WeatherConfig.NETWORK_TIMEOUT_IN_MILLISECONDS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		requestQueue.add(request);
	}

}
