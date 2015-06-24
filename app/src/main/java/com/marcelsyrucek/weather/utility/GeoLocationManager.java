package com.marcelsyrucek.weather.utility;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.WeatherConfig;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.listener.GeoLocationListener;

/**
 * This class provides available location manager (from standard Android API or Google Play Services) and common API.
 * This class can handle just one listener.
 */
public class GeoLocationManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient
		.OnConnectionFailedListener, LocationListener, android.location.LocationListener {

	public static final String TAG = GeoLocationManager.class.getSimpleName();

	private static GeoLocationManager sInstance;

	private Context mContext;

	private GeoLocationListener mGeoLocationListener;
	private Location mLocation;

	// for google play services
	private GoogleApiClient mGoogleApiClient;

	// for android location manager
	private LocationManager mAndroidManager;

	/**
	 * @param context
	 * @return returns the singleton instance of GeoLocationManager
	 */
	public static GeoLocationManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new GeoLocationManager(context);
		}

		return sInstance;
	}

	private GeoLocationManager(Context context) {
		mContext = context;
		if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult
				.SUCCESS) {
			Logcat.d(TAG, "initialize google play services");
			mGoogleApiClient = new GoogleApiClient.Builder(context).addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
			mGoogleApiClient.connect();
		} else {
			Logcat.d(TAG, "initialize android location manager");
			// TODO Marcel: should be there any option for updates google play services etc...
			mAndroidManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
			getAndroidLocation();
		}
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
		mGeoLocationListener = geoLocationListener;
		if (mLocation != null) {
			mGeoLocationListener.lastKnownLocation(mLocation);
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
			mAndroidManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 400, 1, this);
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
			locationRequest.setNumUpdates(1);
			locationRequest.setExpirationDuration(WeatherConfig.LOCATION_REQUEST_EXPIRATION_TIME_IN_MILLIS);
			LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		Logcat.d(TAG, "onConnected");
		getPlayLocation();
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
	public void onLocationChanged(Location location) {
		Logcat.d(TAG, "onLocationChanged");
		if (mAndroidManager != null) {
			mAndroidManager.removeUpdates(this);
		}
		mLocation = location;
		if (mGeoLocationListener != null) {
			mGeoLocationListener.lastKnownLocation(location);
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Logcat.d(TAG, "CHANGED");
	}

	@Override
	public void onProviderEnabled(String provider) {
		Logcat.d(TAG, "ENABLED");
	}

	@Override
	public void onProviderDisabled(String provider) {
		Logcat.d(TAG, "Disabled");
	}

	// will be useful in future

//	private boolean isGooglePlayServicesAvailable() {
//		GoogleApiAvailability api = GoogleApiAvailability.getInstance();
//		int status = api.isGooglePlayServicesAvailable(this);
//		if (ConnectionResult.SUCCESS == status) {
//			return true;
//		} else if (api.isUserResolvableError(status)) {
//			// show error dialog and handle it in onActivityResult
//			api.showErrorDialogFragment(this, status, REQUEST_CODE_RECOVER_GOOGLE_PLAY_SERVICES);
//			return false;
//		} else {
//			Logcat.e(TAG, "No Service");
//			Toast.makeText(this, R.string.google_play_services_not_supported_device, Toast.LENGTH_LONG).show();
//			finish();
//			return false;
//		}
//	}
}
