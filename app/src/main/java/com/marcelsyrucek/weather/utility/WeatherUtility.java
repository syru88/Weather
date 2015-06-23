package com.marcelsyrucek.weather.utility;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;

import com.marcelsyrucek.weather.R;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by marcel on 18.6.2015.
 */
public class WeatherUtility {

	public static final int TEMP_CELSIUS = 0;
	public static final int TEMP_FAHRENHEIT = 1;

	private static final double KELVIN_CONSTANT = 273.15;
	private static final double FAHRENHEIT_CONSTANT = 459.67;

	//	values from preferences
	public static final int LENGTH_KILOMETER = 0;
	public static final int LENGTH_METER = 1;
	public static final int LENGTH_MILE = 2;

	private static final double METER_SEC_TO_KILO_HOUR = 3.6;
	private static final double METER_SEC_TO_MILE_HOUR = 2.2369362920544;

	private static int sWidth;

	private static String directions[] = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

	// move to general utility class
	public static int getDisplayWidth(AppCompatActivity activity) {
		if (sWidth != 0) {
			return sWidth;
		}

		Display display = activity.getWindowManager().getDefaultDisplay();
		Point point = new Point();
		display.getSize(point);
		sWidth = point.x;

		return sWidth;
	}

	public static double getTemperature(int unit, double value) {
		switch (unit) {
			case TEMP_CELSIUS:
				return round(value - KELVIN_CONSTANT);
			case TEMP_FAHRENHEIT:
				return round(value * 9 / 5 - FAHRENHEIT_CONSTANT);
			default:
				return 0;
		}
	}

	public static double getWindSpeed(int unit, double value) {
		switch (unit) {
			case LENGTH_KILOMETER:
				return round(value * METER_SEC_TO_KILO_HOUR);
			case LENGTH_MILE:
				return round(value * METER_SEC_TO_MILE_HOUR);
			default:
				return value;
		}
	}

	public static String getWindSpeedUnit(int unit, Context context) {
		switch (unit) {
			case LENGTH_KILOMETER:
				return context.getString(R.string.weather_wind_speed_km_h);
			case LENGTH_MILE:
				return context.getString(R.string.weather_wind_speed_mph);
			default:
				return context.getString(R.string.weather_wind_speed_m_s);
		}
	}

	public static String getWindDirection(double degrees) {
		return directions[(int) Math.round(((degrees % 360) / 45)) % 8];
	}

	public static String capitalizeFirstLetters(String sentence) {
		if (sentence == null) {
			return null;
		}

		StringBuffer buffer = new StringBuffer(sentence.length());
		String[] words = sentence.split(" ");

		for (String word : words) {
			char[] chars = word.trim().toCharArray();
			chars[0] = Character.toUpperCase(chars[0]);
			buffer.append(new String(chars)).append(" ");
		}

		return buffer.toString().trim();
	}

	// this function round negative number e.g. -273.15 to -273.1
	private static double round(double value) {
		return (double) Math.round(value * 10) / 10;
	}
}
