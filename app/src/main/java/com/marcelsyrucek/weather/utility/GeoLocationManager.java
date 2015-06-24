package com.marcelsyrucek.weather.utility;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.WeatherConfig;
import com.marcelsyrucek.weather.activity.MainActivity;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.listener.GeoLocationListener;

/**
 * This class provides available location manager (from standard Android API or Google Play Services) and common API.
 * This class can handle just one listener and should be refactored in future.
 */
public class GeoLocationManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient
		.OnConnectionFailedListener, LocationListener, android.location.LocationListener {

	public static final String TAG = GeoLocationManager.class.getSimpleName();

	public static final int LOCATION_REQUEST_EXPIRATION_TIME_IN_MILLIS = 60 * 1000;
	public static final int LOCATION_REQUEST_UPDATE_INTERVAL_IN_MILLIS = 10 * 1000;
	public static final int LOCATION_REQUEST_FASTEST_UPDATE_INTERVAL_IN_MILLIS = LOCATION_REQUEST_UPDATE_INTERVAL_IN_MILLIS / 2;

	private MainActivity mMainActivity;

	private GeoLocationListener mGeoLocationListener;
	private Location mLocation;

	// for google play services
	private GoogleApiClient mGoogleApiClient;

	// for android location manager
	private LocationManager mAndroidManager;

	public GeoLocationManager(MainActivity mainActivity) {

		if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mainActivity.getApplicationContext()) == ConnectionResult.SUCCESS) {
			Logcat.d(TAG, "Google Play Success");
			mGoogleApiClient = new GoogleApiClient.Builder(mMainActivity.getApplicationContext()).addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
			mGoogleApiClient.connect();
		} else {
			Logcat.d(TAG, "Initialize android location manager");
			mAndroidManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);
			boolean isNetworkEnabled = mAndroidManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			boolean isGpsEnabled = mAndroidManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			if (!isNetworkEnabled && !isGpsEnabled) {
				Logcat.e(TAG, "go to settings");
				showAlertLocationDialog();
			} else {
				getAndroidLocation();
			}
		}
	}

	private void showAlertLocationDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
		builder.setTitle(mMainActivity.getString(R.string.location_dialog_title));
		builder.setMessage(mMainActivity.getString(R.string.location_dialog_message));
		builder.setPositiveButton(mMainActivity.getText(R.string.general_ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				mMainActivity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		});
		builder.show();
	}

	/**
	 * @return the last known location, or null
	 */
	public Location getLastKnownLocation() {
		return mLocation;
	}

	/**
	 * Register GeoLocationListener. This method should be called when method {@link #getLastKnownLocation()} returns
	 * null.
	 *
	 * @param geoLocationListener a {@link GeoLocationListener} object to registerListener
	 */
	public void registerListener(GeoLocationListener geoLocationListener) {
		if (mGeoLocationListener == geoLocationListener) {
			// TODO Marcel: this kind of working with listeners isn't the best one. Rewrite!!!
			return;
		}
		Logcat.d(TAG, "registerListener");

		if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
			mGoogleApiClient.connect();
		}

		if (mAndroidManager != null) {
			getAndroidLocation();
		}

		mGeoLocationListener = geoLocationListener;
		// try to get new position
		mLocation = null;
	}

	public void reloadCurrentPosition() {
		Logcat.d(TAG, "reloadCurrentPosition");
		mLocation = null;

		if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
			mGoogleApiClient.connect();
		}

		if (mAndroidManager != null) {
			getAndroidLocation();
		}
	}

	public void unregisterListener() {
		Logcat.d(TAG, "unregisterListener");
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
			LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
			mGoogleApiClient.disconnect();
		}
		if (mAndroidManager != null) {
			mAndroidManager.removeUpdates(this);
		}
		mGeoLocationListener = null;
	}

	/**
	 * If last location from Android Location API is unknown start receiving updates about location.
	 */
	private void getAndroidLocation() {
		Logcat.d(TAG, "getAndroidLocation");
		Location lastLocation = mAndroidManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastLocation != null) {
			mLocation = lastLocation;
			if (mGeoLocationListener != null) {
				mGeoLocationListener.lastKnownLocation(mLocation);
			}
		} else {
			Logcat.d(TAG, "request");
			mAndroidManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, Looper.getMainLooper());
		}

	}

	/**
	 * If last location from Google Play client is unknown start receiving updates about location.
	 */
	private void getPlayLocation() {
		Logcat.d(TAG, "getPlayLocation");
		Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		if (lastLocation != null) {
			Logcat.d(TAG, "We have last known location");
			mLocation = lastLocation;
			if (mGeoLocationListener != null) {
				mGeoLocationListener.lastKnownLocation(mLocation);
			}
		} else {
			Logcat.d(TAG, "No last location, run request.");
			LocationRequest locationRequest = new LocationRequest();
			locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
			locationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_UPDATE_INTERVAL_IN_MILLIS);
			locationRequest.setInterval(LOCATION_REQUEST_UPDATE_INTERVAL_IN_MILLIS);
			locationRequest.setNumUpdates(1);
			locationRequest.setExpirationDuration(LOCATION_REQUEST_EXPIRATION_TIME_IN_MILLIS);
			LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		Logcat.d(TAG, "onConnected");
		getPlayLocation();
	}

	@Override
	public void onLocationChanged(Location location) {
		Logcat.e(TAG, "Both: onLocationChanged");
		if (mAndroidManager != null) {
			mAndroidManager.removeUpdates(this);
		}
		mLocation = location;
		if (mGeoLocationListener != null) {
			mGeoLocationListener.lastKnownLocation(location);
		}
	}

	/**
	 * Return true if the location is in city area.
	 *
	 * @param currentLatitude
	 * @param currentLongitude
	 * @param city
	 * @param results
	 * @return
	 */
	public static boolean isLocationInArea(double currentLatitude, double currentLongitude, CityModel city, float[]
			results) {

		results[0] = 0;

		if (city == null) {
			return false;
		}

		Location.distanceBetween(currentLatitude, currentLongitude, city.getLatitude(), city.getLongitude(), results);
		Logcat.d(TAG, "Distance between current location and " + city.getName() + " is " + results[0] + " meters");

		if (results[0] < WeatherConfig.DISTANCE_LOCATION_ACCURACY_IN_METERS) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
		Logcat.d(TAG, "onConnectionSuspended");
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Logcat.d(TAG, "onConnectionFailed");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Logcat.d(TAG, "Android: Status changed");
	}

	@Override
	public void onProviderEnabled(String provider) {
		Logcat.d(TAG, "Android: Provider enabled");
	}

	@Override
	public void onProviderDisabled(String provider) {
		Logcat.d(TAG, "Android: Provider disabled");
	}

}
