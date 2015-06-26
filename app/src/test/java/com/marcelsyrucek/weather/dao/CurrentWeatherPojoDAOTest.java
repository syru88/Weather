package com.marcelsyrucek.weather.dao;

import com.marcelsyrucek.weather.database.model.CurrentWeatherModel;
import com.marcelsyrucek.weather.utility.WeatherUtility;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by marcel on 21.6.2015.
 */
public class CurrentWeatherPojoDAOTest {

	private CurrentWeatherModel mDAO;

	@Before
	public void setUp() throws Exception {
		mDAO = new CurrentWeatherModel();
	}

	@Test
	public void testGetDescription() throws Exception {
		mDAO.setDescription("light rain");

		assertTrue(mDAO.getDescription().equals("Light Rain"));
	}

	@Test
	public void testTemperature() throws Exception {
		mDAO.setTemperature(0);

		assertEquals(-459.7, WeatherUtility.getTemperature(WeatherUtility.TEMP_FAHRENHEIT, 0), 0);
		assertEquals(-273.1, WeatherUtility.getTemperature(WeatherUtility.TEMP_CELSIUS, 0), 0);

	}

	@Test
	public void testWindSpeed() throws Exception {

		assertEquals(22.4, WeatherUtility.getWindSpeed(WeatherUtility.LENGTH_MILE, 10), 0);
		assertEquals(36, WeatherUtility.getWindSpeed(WeatherUtility.LENGTH_KILOMETER, 10), 0);
		assertEquals(10, WeatherUtility.getWindSpeed(WeatherUtility.LENGTH_METER, 10), 0);
	}

	@Test
	public void testWindDirection() throws Exception {
		mDAO.setWindDirection(45);
		assertTrue("NE".equals(mDAO.getWindDirection()));

		mDAO.setWindDirection(21);
		assertTrue("N".equals(mDAO.getWindDirection()));

		mDAO.setWindDirection(350);
		assertTrue("N".equals(mDAO.getWindDirection()));

		mDAO.setWindDirection(365);
		assertTrue("N".equals(mDAO.getWindDirection()));
	}
}