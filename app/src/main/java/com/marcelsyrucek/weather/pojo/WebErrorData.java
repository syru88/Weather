package com.marcelsyrucek.weather.pojo;

import com.google.gson.annotations.Expose;

/**
 * Created by marcel on 18.6.2015.
 */
public class WebErrorData {
	@Expose
	private String message;
	@Expose
	private String cod;

	/**
	 * @return The message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message The message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return The cod
	 */
	public String getCod() {
		return cod;
	}

	/**
	 * @param cod The cod
	 */
	public void setCod(String cod) {
		this.cod = cod;
	}
}
