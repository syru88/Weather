package com.marcelsyrucek.weather.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.WeatherApplication;
import com.marcelsyrucek.weather.adapter.MenuAdapter;
import com.marcelsyrucek.weather.database.CityDatabase;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.dialog.AboutDialogFragment;
import com.marcelsyrucek.weather.event.CityClickedEvent;
import com.marcelsyrucek.weather.event.CityLoadedEvent;
import com.marcelsyrucek.weather.fragment.ForecastFragment;
import com.marcelsyrucek.weather.fragment.TodayFragment;
import com.marcelsyrucek.weather.listener.GeoLocationListener;
import com.marcelsyrucek.weather.service.NetworkService;
import com.marcelsyrucek.weather.utility.GeoLocationManager;
import com.marcelsyrucek.weather.utility.Logcat;
import com.marcelsyrucek.weather.utility.WeatherUtility;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

public class MainActivity extends AppCompatActivity implements GeoLocationListener, MenuAdapter.MenuClickListener {

	public static final String TAG = MainActivity.class.getSimpleName();
	public static final String BUNDLE_LAST_SHOWN_CITY = "BUNDLE_LAST_SHOWN_CITY";

	private static final int REQUEST_CODE_RECOVER_GOOGLE_PLAY_SERVICES = 1;

	private static final String BUNDLE_M_IS_POSITION_RECEIVED = "BUNDLE_M_IS_POSITION_RECEIVED";
	private static final String BUNDLE_LAST_KNOWN_LOCATION = "BUNDLE_LAST_KNOWN_LOCATION";

	private Toolbar mToolbar;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private RecyclerView mCityRecyclerView;

	private TabLayout mTabLayout;
	private ViewGroup mLoadingContainer;
	private ViewPager mViewPager;
	private PagerAdapter mPagerAdapter;

	private GeoLocationManager mGeoLocationManager;
	private Location mLastKnownLocation;
	private CityModel mRequestedCity, mCityWithCurrentPosition;
	private boolean mIsPositionReceived = true;

	private Bus mBus = WeatherApplication.bus;
	private MenuAdapter mMenuAdapter;
	private MenuItem mSearchMenuItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logcat.e(TAG, "onCreate");
		setContentView(R.layout.activity_main);

		if (savedInstanceState != null) {
			mIsPositionReceived = savedInstanceState.getBoolean(BUNDLE_M_IS_POSITION_RECEIVED, true);
			mRequestedCity = (CityModel) savedInstanceState.getSerializable(BUNDLE_LAST_SHOWN_CITY);
			mLastKnownLocation = savedInstanceState.getParcelable(BUNDLE_LAST_KNOWN_LOCATION);
		}

		// prepare location manager
		mGeoLocationManager = GeoLocationManager.getInstance(getApplicationContext());

		// setup UI
		setupToolbar();
		setupDrawer();
		setupMainMenu();
		setupViewPager();
		if (mLastKnownLocation == null) {
			loadCities();
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(BUNDLE_M_IS_POSITION_RECEIVED, mIsPositionReceived);
		outState.putParcelable(BUNDLE_LAST_KNOWN_LOCATION, mLastKnownLocation);
		outState.putSerializable(BUNDLE_LAST_SHOWN_CITY, mRequestedCity);

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mBus.register(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mBus.unregister(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mGeoLocationManager.registerListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		mGeoLocationManager.unregisterListener();
	}

	@Override
	public void onBackPressed() {
		if (mSearchMenuItem != null && mSearchMenuItem.isActionViewExpanded()) {
			mSearchMenuItem.collapseActionView();
		} else if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
			mDrawerLayout.closeDrawer(Gravity.LEFT);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_others, menu);

		mSearchMenuItem = menu.findItem(R.id.menu_search);
		SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
		searchView.setQueryHint(getString(R.string.menu_action_search_city_hint));
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				mRequestedCity = new CityModel(query);
				sendCityClickEvent();
				mSearchMenuItem.collapseActionView();
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		MenuItem menuItem = menu.findItem(R.id.menu_edit_city);

		// Add or Remove
		if (CityDatabase.getInstance(this).isCityInDatabase(mRequestedCity)) {
			menuItem.setTitle(R.string.menu_action_remove_this_city);
		} else {
			menuItem.setTitle(R.string.menu_action_add_this_city);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mDrawerLayout.closeDrawer(Gravity.LEFT);
		switch (item.getItemId()) {
			case R.id.menu_edit_city:
				if (CityDatabase.getInstance(this).isCityInDatabase(mRequestedCity)) {
					removeCityFromDatabase();
				} else {
					addCityToDabase();
				}
				return true;
			case R.id.menu_others_settings:
				startSettingsActivity();
				return true;
			case R.id.menu_others_about:
				showAboutDialog();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void setupToolbar() {
		mToolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
		setSupportActionBar(mToolbar);
		if (mRequestedCity != null) {
			mToolbar.setTitle(mRequestedCity.getName());
		}
	}

	private void setupDrawer() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
	}

	private void setupMainMenu() {
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.menu_open, R.string
				.menu_close);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerLayout.setDrawerShadow(R.drawable.general_vertical_shadow, GravityCompat.START);
		mDrawerToggle.syncState();

		mCityRecyclerView = (RecyclerView) findViewById(R.id.activity_main_navigation_view);
		ViewGroup.LayoutParams lp = mCityRecyclerView.getLayoutParams();
		lp.width = Math.min(WeatherUtility.getDisplayWidth(this) - getResources().getDimensionPixelSize(R.dimen
				.navigation_drawer_rigth_margin), lp.width);
		mCityRecyclerView.setLayoutParams(lp);

		mMenuAdapter = new MenuAdapter(CityDatabase.getInstance(this).getCities(), this);
		mCityRecyclerView.setAdapter(mMenuAdapter);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		mCityRecyclerView.setLayoutManager(layoutManager);
	}

	private void setupViewPager() {
		Logcat.d(TAG, "setupViewPager");
		mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.activity_main_main_container);
		mViewPager.setAdapter(mPagerAdapter);

		mTabLayout = (TabLayout) findViewById(R.id.activity_main_tab_layout);
		mTabLayout.setupWithViewPager(mViewPager);

		mLoadingContainer = (ViewGroup) findViewById(R.id.activity_main_loading_container);
	}

	private void loadCities() {
		Logcat.d(TAG, "loadCities");

		mLastKnownLocation = mGeoLocationManager.getLastKnownLocation();

		if (mLastKnownLocation == null) {
			mGeoLocationManager.registerListener(this);
			mIsPositionReceived = false;
			Logcat.d(TAG, "Current location is unknown");

			mRequestedCity = CityDatabase.getInstance(getApplicationContext()).getCityWithCurrentPosition();
			if (mRequestedCity == null) {
				// first start of application and waiting for position, should happen just one time :-)
				Logcat.e(TAG, "First start of application and we don't have position, so wait for it.");
				mLoadingContainer.setVisibility(View.VISIBLE);
				ProgressBar progressBar = (ProgressBar) findViewById(R.id.activity_main_progress_bar);
				progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.primary),
						PorterDuff.Mode
								.SRC_IN);
			} else {
				sendCityClickEvent();
			}
		} else {
			// we have current location so load data about it
			prepareCityWithCurrentPosition();
			sendCityClickEvent();
		}
	}

	@Subscribe
	public void subscribeOnCityLoadedEvent(CityLoadedEvent cityLoadedEvent) {
		CityModel receivedCity = cityLoadedEvent.getCityModel();
		Logcat.e(TAG, "CityLoadedEvent: " + receivedCity);

		if (GeoLocationManager.isLocationInArea(receivedCity.getLatitude(), receivedCity.getLongitude(), mCityWithCurrentPosition, new
				float[3])) {
			Logcat.d(TAG, "We just received current position, so change name");
			CityDatabase.getInstance(this).editCurrentCity(receivedCity);
			mMenuAdapter.setCities(CityDatabase.getInstance(this).getCities());
		}

		mRequestedCity = receivedCity;
		mToolbar.setTitle(mRequestedCity.getName());

	}

	private void sendCityClickEvent() {
		mBus.post(new CityClickedEvent(mRequestedCity));
	}

	@Produce
	public CityClickedEvent produceLastCityClickedEvent() {
		return new CityClickedEvent(mRequestedCity);
	}

	private void prepareCityWithCurrentPosition() {
		mCityWithCurrentPosition = new CityModel();
		mCityWithCurrentPosition.setLatitude(mLastKnownLocation.getLatitude());
		mCityWithCurrentPosition.setLongitude(mLastKnownLocation.getLongitude());
		mCityWithCurrentPosition.setName(getString(R.string.menu_menu_current_position));
		mCityWithCurrentPosition.setId(getString(R.string.prefs_cities_storage_current_city));

		mRequestedCity = mCityWithCurrentPosition;

		CityDatabase.getInstance(getApplicationContext()).editCurrentCity(mRequestedCity);
	}

	@Override
	public void lastKnownLocation(Location location) {
		Logcat.e(TAG, "Just received location, latitude: " + location
				.getLatitude() + ", latitude: " + location.getLongitude());
		mGeoLocationManager.unregisterListener();
		mIsPositionReceived = true;

		mLoadingContainer.setVisibility(View.GONE);

		mLastKnownLocation = location;

		// if current location isn't in area which has already been requested, refresh data
		if (!GeoLocationManager.isLocationInArea(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(),
				mRequestedCity, new float[3])) {
			Logcat.e(TAG, "The new location is different from old one, so refresh");

			prepareCityWithCurrentPosition();
			sendCityClickEvent();
		}

	}

	@Override
	public void onCityMenuClick(CityModel cityModel) {
		if (getString(R.string.prefs_cities_storage_current_city).equals(cityModel.getId())) {
			Logcat.e(TAG, "Obtain new current position");
			mGeoLocationManager.reloadCurrentPosition();
			loadCities();
		}
		mRequestedCity = cityModel;
		mDrawerLayout.closeDrawer(Gravity.LEFT);
		sendCityClickEvent();
	}

	private void addCityToDabase() {
		Logcat.d(TAG, "addCityToDatabase");
		CityDatabase.getInstance(this).addCity(mRequestedCity);
		mMenuAdapter.setCities(CityDatabase.getInstance(this).getCities());
	}

	private void removeCityFromDatabase() {
		Logcat.d(TAG, "removeCityFromDatabase");
		CityDatabase.getInstance(this).removeCity(mRequestedCity);
		mMenuAdapter.setCities(CityDatabase.getInstance(this).getCities());
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
		super.onPostCreate(savedInstanceState, persistentState);
		mDrawerToggle.syncState();
	}

	public class PagerAdapter extends FragmentPagerAdapter {

		public PagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (position == 0) {
				return TodayFragment.newInstance();
			} else {
				return ForecastFragment.newInstance();
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			if (position == 0) {
				return getString(R.string.menu_today);
			} else {
				return getString(R.string.menu_forecast);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Logcat.d(TAG, "onActivityResult: " + requestCode);
		switch (requestCode) {
			case REQUEST_CODE_RECOVER_GOOGLE_PLAY_SERVICES:
				if (resultCode == RESULT_CANCELED) {
					Toast.makeText(this, R.string.google_play_services_not_installed, Toast.LENGTH_LONG).show();
					finish();
				}
				break;
		}
	}

	private void startSettingsActivity() {
		Logcat.d(TAG, "startSettingsActivity");

		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);

	}

	private void showAboutDialog() {
		AboutDialogFragment fragment = AboutDialogFragment.newInstance();
		fragment.show(getFragmentManager(), fragment.getTag());
	}

}
