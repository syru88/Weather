package com.marcelsyrucek.weather.pojo;

import com.google.gson.annotations.Expose;

/**
 * Created by marcel on 18.6.2015.
 */
public class WebErrorData {
	@Expose
	private String errorMessage;
	@Expose
	private String cod;

	/**
	 * @return The errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage The errorMessage
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
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
