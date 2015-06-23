package com.marcelsyrucek.weather.request;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.marcelsyrucek.weather.VolleyWrapper;
import com.marcelsyrucek.weather.WeatherConfig;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.pojo.forecast.ForecastPojo;
import com.marcelsyrucek.weather.utility.GsonRequest;
import com.marcelsyrucek.weather.utility.Logcat;

/**
 * Created by marcel on 23.6.2015.
 */
public class GetForecastRequest {

	private static final String TAG = GetForecastRequest.class.getSimpleName();

	private static final String VERSION = "2.5";

	public static void getForecastWeather(CityModel city, Context context, Response.Listener<ForecastPojo> listener,
	                                      Response.ErrorListener errorListener) {
		StringBuilder url = new StringBuilder(300);

		url.append(WeatherConfig.API_ENDPOINT_PRODUCTION + VERSION + "/forecast/daily?");
		url.append(UtilityRequest.getSearchCondition(city, context));
		url.append(WeatherConfig.API_APPID);


	}

	private static void getForecastWeather(String url, Context context, Response.Listener<ForecastPojo> listener,
	                                       Response.ErrorListener errorListener) {
		url = url.replaceAll("\\s", "");
		Logcat.d(TAG, "url: " + url);

		RequestQueue requestQueue = VolleyWrapper.getInstance(context).getRequestQueue();
		GsonRequest<ForecastPojo> request = new GsonRequest<ForecastPojo>(Request.Method.GET, url, ForecastPojo
				.class, listener, errorListener);

		requestQueue.add(request);
	}

}
