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

import java.util.Timer;
import java.util.TimerTask;

/**
 * This class provides available location manager (from standard Android API or Google Play Services) and common API.
 * This class can handle just one listener and should be definitely refactored.
 */
public class GeoLocationManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient
		.OnConnectionFailedListener, LocationListener, android.location.LocationListener {

	public static final String TAG = GeoLocationManager.class.getSimpleName();

	private static final int INTERVAL_IN_MILLIS = 10 * 1000;

	private static GeoLocationManager sInstance;

	private Context mContext;

	private GeoLocationListener mGeoLocationListener;
	private Location mLocation;
	private boolean mIsError;
	private String mErrorText;

	// for google play services
	private GoogleApiClient mGoogleApiClient;

	// for android location manager
	private LocationManager mAndroidManager;
	private Timer mTimer;
	private TimerTask mTimerTask;

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
			Logcat.d(TAG, "Initialize google play services");
			mGoogleApiClient = new GoogleApiClient.Builder(context).addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
			mGoogleApiClient.connect();
		} else {
			Logcat.d(TAG, "Initialize android location manager");
			// TODO Marcel: should be there any option for updates google play services etc...
			mAndroidManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
			getAndroidLocation();
		}
		initializeTimerAndTask();
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
			mGeoLocationListener.onLocationChanged(mLocation);
		}
		if (mIsError) {
			mGeoLocationListener.onRequestLocationFailed(mErrorText);
		}
	}

	public void reloadCurrentPosition() {
		Logcat.d(TAG, "reloadCurrentPosition");
		mLocation = null;
		mIsError = false;
		mTimer.cancel();
		initializeTimerAndTask();

		connectRequests();
	}

	private void connectRequests() {
		if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
			mGoogleApiClient.connect();
		}

		if (mAndroidManager != null) {
			getAndroidLocation();
		}
	}

	public void unregisterListener() {
		Logcat.d(TAG, "unregisterListener");
		disconnectRequests();
		mGeoLocationListener = null;
	}

	private void disconnectRequests() {
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
			LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
			mGoogleApiClient.disconnect();
		}
		if (mAndroidManager != null) {
			mAndroidManager.removeUpdates(this);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		Logcat.d(TAG, "onLocationChanged");
		mIsError = false;
		if (mTimer != null) {
			mTimer.cancel();
		}

		// don't need to unregister mAndroidManager or mGoogleApiClient because they are set just for one update

		mLocation = location;
		if (mGeoLocationListener != null) {
			mGeoLocationListener.onLocationChanged(location);
		}
	}

	/**
	 * If last location from Android Location API is unknown start receiving updates about location.
	 */
	private void getAndroidLocation() {
		Logcat.d(TAG, "getAndroidLocation");
		Location lastLocation = mAndroidManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastLocation != null) {
			Logcat.d(TAG, "We have last known location");
			mTimer.cancel();
			mLocation = lastLocation;
			if (mGeoLocationListener != null) {
				mGeoLocationListener.onLocationChanged(mLocation);
			}
		} else {
			Logcat.d(TAG, "No last location, run request.");
			mAndroidManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, Looper.getMainLooper());
		}

	}

	@Override
	public void onConnected(Bundle bundle) {
		Logcat.d(TAG, "onConnected");
		getPlayLocation();
	}

	/**
	 * If last location from Google Play client is unknown start receiving updates about location.
	 */
	private void getPlayLocation() {
		Logcat.d(TAG, "getPlayLocation");
		Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		if (lastLocation != null) {
			Logcat.d(TAG, "We have last known location");
			mTimer.cancel();
			mLocation = lastLocation;
			if (mGeoLocationListener != null) {
				mGeoLocationListener.onLocationChanged(mLocation);
			}
		} else {
			Logcat.d(TAG, "No last location, run request.");
			LocationRequest locationRequest = new LocationRequest();
			locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
			locationRequest.setInterval(INTERVAL_IN_MILLIS);
			locationRequest.setNumUpdates(1);
			LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
		}
	}

	private void initializeTimerAndTask() {
		mTimerTask = new TimerTask() {
			@Override
			public void run() {
				Logcat.e(TAG, "RUN");
				mIsError = true;
				mErrorText = mContext.getString(R.string.location_error_general);
				if (mGeoLocationListener != null) {
					mGeoLocationListener.getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Logcat.e(TAG, "Time is over!");
							mGeoLocationListener.onRequestLocationFailed(mErrorText);
						}
					});
				}
				disconnectRequests();
			}
		};
		mTimer = new Timer();
		mTimer.schedule(mTimerTask, WeatherConfig.CURRENT_LOCATION_TIMEOUT_IN_MILLIS);
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

}
