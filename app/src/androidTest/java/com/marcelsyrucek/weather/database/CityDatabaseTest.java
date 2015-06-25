package com.marcelsyrucek.weather.database;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.database.model.CityModel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * Created by marcel on 22.6.2015.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CityDatabaseTest {

	private CityDatabase mDatabase;

	@Before
	public void setUp() {
		mDatabase = CityDatabase.getInstance(InstrumentationRegistry.getTargetContext());

		CityModel cityModel = new CityModel();
		cityModel.setId("11");
		cityModel.setName("Brno");
		mDatabase.addCity(cityModel);

		cityModel = new CityModel();
		cityModel.setId(InstrumentationRegistry.getTargetContext().getString(R.string.prefs_storage_current_city));
		cityModel.setName("Current location");
		mDatabase.addCity(cityModel);
	}

	@Test
	public void a_getCityWithCurrentLocation() {
		CityModel model = mDatabase.getCityWithCurrentPosition();
		Assert.assertTrue(model.getName().equals("Current location"));
	}

	@Test
	public void b_firstStartOfApplication() {
		CityModel model = mDatabase.getCityWithCurrentPosition();
		Assert.assertTrue(model == null);

	}



}
