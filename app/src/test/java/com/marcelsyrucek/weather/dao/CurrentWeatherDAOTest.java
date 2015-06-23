package com.marcelsyrucek.weather.dao;

import com.marcelsyrucek.weather.database.model.CurrentWeatherModel;
import com.marcelsyrucek.weather.utility.WeatherUtility;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by marcel on 18.6.2015.
 */
public class CurrentWeatherDAOTest {

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

		assertEquals(-459.7, mDAO.getTemperature(WeatherUtility.TEMP_FAHRENHEIT), 0);
		assertEquals(-273.1, mDAO.getTemperature(WeatherUtility.TEMP_CELSIUS), 0);

	}

	@Test
	public void testWindSpeed() throws Exception {
		mDAO.setWindSpeed(10);

		assertEquals(22.4, mDAO.getWindSpeed(WeatherUtility.LENGTH_MILE), 0);
		assertEquals(36, mDAO.getWindSpeed(WeatherUtility.LENGTH_KILOMETER), 0);
		assertEquals(10, mDAO.getWindSpeed(WeatherUtility.LENGTH_METER), 0);
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