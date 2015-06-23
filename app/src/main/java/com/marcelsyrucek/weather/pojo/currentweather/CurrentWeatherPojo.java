
package com.marcelsyrucek.weather.pojo.currentweather;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.pojo.WebErrorData;

public class CurrentWeatherPojo extends WebErrorData {

	@Expose
	private int id;
	@Expose
	private long dt;
	@Expose
	private String name;
	@Expose
	private Coord coord;
	@Expose
	private Sys sys;
	@Expose
	private Main main;
	@Expose
	private Wind wind;
	@Expose
	private List<Weather> weather = new ArrayList<Weather>();
	@Expose
	private Clouds clouds;
	@Expose
	private Rain rain;

	public CityModel getCityModel() {
		CityModel cityModel = new CityModel();

		cityModel.setName(getNameWithCountry());
		cityModel.setId(id+"");
		if (coord != null) {
			cityModel.setLatitude(coord.getLat());
			cityModel.setLongitude(coord.getLon());
		}

		return cityModel;
	}

	public String getNameWithCountry() {
		if (sys != null && sys.getCountry() != null) {
			return name + ", " + sys.getCountry();
		}
		return name;
	}

	/**
	 * @return The id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id The id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return The dt
	 */
	public long getDt() {
		return dt;
	}

	/**
	 * @param dt The dt
	 */
	public void setDt(int dt) {
		this.dt = dt;
	}

	/**
	 * @return The name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The coord
	 */
	public Coord getCoord() {
		return coord;
	}

	/**
	 * @param coord The coord
	 */
	public void setCoord(Coord coord) {
		this.coord = coord;
	}

	public Sys getSys() {
		return sys;
	}

	public void setSys(Sys sys) {
		this.sys = sys;
	}

	/**
	 * @return The main
	 */
	public Main getMain() {
		return main;
	}

	/**
	 * @param main The main
	 */
	public void setMain(Main main) {
		this.main = main;
	}

	/**
	 * @return The wind
	 */
	public Wind getWind() {
		return wind;
	}

	/**
	 * @param wind The wind
	 */
	public void setWind(Wind wind) {
		this.wind = wind;
	}

	/**
	 * @return The weather
	 */
	public List<Weather> getWeather() {
		return weather;
	}

	/**
	 * @param weather The weather
	 */
	public void setWeather(List<Weather> weather) {
		this.weather = weather;
	}

	/**
	 * @return The clouds
	 */
	public Clouds getClouds() {
		return clouds;
	}

	/**
	 * @param clouds The clouds
	 */
	public void setClouds(Clouds clouds) {
		this.clouds = clouds;
	}

	/**
	 * @return The rain
	 */
	public Rain getRain() {
		return rain;
	}

	/**
	 * @param rain The rain
	 */
	public void setRain(Rain rain) {
		this.rain = rain;
	}

}
