package com.marcelsyrucek.weather.utility;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by marcel on 24.6.2015.
 */
public class WeatherUtilityTest {

	@Test
	public void testGetDate() throws Exception {
		System.out.println(WeatherUtility.getDate(1435053600));
	}
}