package com.marcelsyrucek.weather.pojo.currentweather;

import java.util.HashMap;
import java.util.Map;

public class Sys {

	private double message;
	private String country;
	private long sunrise;
	private long sunset;
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 * @return The message
	 */
	public double getMessage() {
		return message;
	}

	/**
	 * @param message The message
	 */
	public void setMessage(double message) {
		this.message = message;
	}

	/**
	 * @return The country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @param country The country
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @return The sunrise
	 */
	public long getSunrise() {
		return sunrise;
	}

	/**
	 * @param sunrise The sunrise
	 */
	public void setSunrise(long sunrise) {
		this.sunrise = sunrise;
	}

	/**
	 * @return The sunset
	 */
	public long getSunset() {
		return sunset;
	}

	/**
	 * @param sunset The sunset
	 */
	public void setSunset(long sunset) {
		this.sunset = sunset;
	}

	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}