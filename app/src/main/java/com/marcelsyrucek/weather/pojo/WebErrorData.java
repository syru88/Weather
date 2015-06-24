package com.marcelsyrucek.weather.pojo;

import com.google.gson.annotations.Expose;

/**
 * Created by marcel on 18.6.2015.
 */
public class WebErrorData {
	@Expose
	private String cod;

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
