package com.marcelsyrucek.weather;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

import com.facebook.stetho.Stetho;
import com.marcelsyrucek.weather.database.CityDatabase;
import com.marcelsyrucek.weather.utility.GeoLocationManager;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by marcel on 17.6.2015.
 */
public class WeatherApplication extends Application {
	private static WeatherApplication sInstance;
	public static Bus bus = new Bus(ThreadEnforcer.ANY);

	public static Context getContext() {
		return sInstance;
	}

	public WeatherApplication() {
		sInstance = this;

	}

	@Override
	public void onCreate() {
		super.onCreate();
		VolleyWrapper.getInstance(this);
		Stetho.initialize( Stetho.newInitializerBuilder(this)
				.enableDumpapp(
						Stetho.defaultDumperPluginsProvider(this))
				.enableWebKitInspector(
						Stetho.defaultInspectorModulesProvider(this))
				.build());

		// initialize preferences, database, location manager etc.
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		CityDatabase.getInstance(this);
	}

}
