package com.marcelsyrucek.weather.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import android.widget.TextView;
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
	private static final String BUNDLE_MENU_POSITION = "BUNDLE_MENU_POSITION";
	private static final String BUNDLE_TAB_POSITION = "BUNDLE_TAB_POSITION";

	// menu and toolbars
	private Toolbar mToolbar;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private RecyclerView mCityRecyclerView;
	private ViewGroup mMenuContainer;
	private TabLayout mTabLayout;

	// current position stuff
	private ViewGroup mLoadingContainer;
	private TextView mLoadingTextView;
	private View mProgressBar;

	// viewpagers
	private ViewPager mViewPager;
	private PagerAdapter mPagerAdapter;

	private GeoLocationManager mGeoLocationManager;
	private boolean mIsRequestForCurrentPosition;
	private boolean mIsPositionReceived;

	private Bus mBus = WeatherApplication.bus;
	private MenuAdapter mMenuAdapter;
	private MenuItem mSearchMenuItem;

	// fields to be saved
	private Location mLastKnownLocation;
	private CityModel mRequestedCity;
	private int mMenuItemPosition = MenuAdapter.NO_POSITION;
	private int mTabPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logcat.e(TAG, "onCreate");
		setContentView(R.layout.activity_main);

		if (savedInstanceState != null) {
			mIsPositionReceived = savedInstanceState.getBoolean(BUNDLE_M_IS_POSITION_RECEIVED, false);
			mRequestedCity = (CityModel) savedInstanceState.getSerializable(BUNDLE_LAST_SHOWN_CITY);
			mLastKnownLocation = savedInstanceState.getParcelable(BUNDLE_LAST_KNOWN_LOCATION);
			mMenuItemPosition = savedInstanceState.getInt(BUNDLE_MENU_POSITION, MenuAdapter.NO_POSITION);
			mTabPosition = savedInstanceState.getInt(BUNDLE_TAB_POSITION, 0);
		}

		// setup UI
		setupToolbar();
		setupDrawer();
		setupMainMenu();
		setupViewPager();
		mGeoLocationManager = GeoLocationManager.getInstance(getApplicationContext());
		if (mLastKnownLocation == null) {
			loadCurrentPosition();
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(BUNDLE_M_IS_POSITION_RECEIVED, mIsPositionReceived);
		outState.putParcelable(BUNDLE_LAST_KNOWN_LOCATION, mLastKnownLocation);
		outState.putSerializable(BUNDLE_LAST_SHOWN_CITY, mRequestedCity);
		outState.putInt(BUNDLE_MENU_POSITION, mMenuAdapter.getLastSelectedPosition());
		outState.putInt(BUNDLE_TAB_POSITION, mViewPager.getCurrentItem());

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

		if (!mIsPositionReceived) {
			mGeoLocationManager.registerListener(this);
		}
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
	}

	private void setupDrawer() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.menu_open, R.string
				.menu_close);
		if (mRequestedCity != null) {
			getSupportActionBar().setTitle(mRequestedCity.getName());
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerLayout.setDrawerShadow(R.drawable.general_vertical_shadow, GravityCompat.START);
		mDrawerToggle.syncState();
	}

	private void setupMainMenu() {

		// navigation drawer width according to styleguides
		mMenuContainer = (ViewGroup) findViewById(R.id.activity_main_menu_container);
		ViewGroup.LayoutParams lp = mMenuContainer.getLayoutParams();
		lp.width = Math.min(WeatherUtility.getDisplayWidth(this) - getResources().getDimensionPixelSize(R.dimen.navigation_drawer_rigth_margin), lp.width);
		mMenuContainer.setLayoutParams(lp);

		mMenuAdapter = new MenuAdapter(CityDatabase.getInstance(this).getCities(), this);
		mMenuAdapter.setLastSelectedPosition(mMenuItemPosition);

		mCityRecyclerView = (RecyclerView) findViewById(R.id.activity_main_navigation_view);
		mCityRecyclerView.setAdapter(mMenuAdapter);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		mCityRecyclerView.setLayoutManager(layoutManager);
	}

	private void setupViewPager() {
		Logcat.d(TAG, "setupViewPager");
		mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.activity_main_main_container);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setCurrentItem(mTabPosition);

		mTabLayout = (TabLayout) findViewById(R.id.activity_main_tab_layout);
		mTabLayout.setupWithViewPager(mViewPager);

		mLoadingContainer = (ViewGroup) findViewById(R.id.activity_main_loading_container);
		mLoadingTextView = (TextView) findViewById(R.id.activity_main_loading_text);
		mProgressBar = findViewById(R.id.activity_main_progress_bar);
	}

	private void loadCurrentPosition() {
		Logcat.d(TAG, "loadCurrentPosition");

		mIsRequestForCurrentPosition = true;
		mLastKnownLocation = mGeoLocationManager.getLastKnownLocation();

		if (mLastKnownLocation == null) {
			mGeoLocationManager.registerListener(this);
			mIsPositionReceived = false;
			Logcat.d(TAG, "Current location is unknown");

			Logcat.e(TAG, "First start of application and we don't have position, so wait for it.");
			mLoadingContainer.setVisibility(View.VISIBLE);
			ProgressBar progressBar = (ProgressBar) findViewById(R.id.activity_main_progress_bar);
			progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.primary),
					PorterDuff.Mode
							.SRC_IN);
		} else {
			// we have current location so fetch data about it
			prepareCityWithCurrentPosition();
			sendCityClickEvent();
		}
	}

	@Subscribe
	public void subscribeOnCityLoadedEvent(CityLoadedEvent cityLoadedEvent) {
		CityModel receivedCity = cityLoadedEvent.getCityModel();
		Logcat.e(TAG, "CityLoadedEvent: " + receivedCity);

		if (mIsRequestForCurrentPosition) {
			mIsRequestForCurrentPosition = false;
			Logcat.d(TAG, "We just received current position, so save it to db as current position");
			CityDatabase.getInstance(this).editCurrentCity(receivedCity);
			mMenuAdapter.setCities(CityDatabase.getInstance(this).getCities());
		}

		mRequestedCity = receivedCity;
		getSupportActionBar().setTitle(mRequestedCity.getName());

	}

	private void sendCityClickEvent() {
		mBus.post(new CityClickedEvent(mRequestedCity));
	}

	@Produce
	public CityClickedEvent produceLastCityClickedEvent() {
		return new CityClickedEvent(mRequestedCity);
	}

	private void prepareCityWithCurrentPosition() {
		mRequestedCity = new CityModel();
		mRequestedCity.setLatitude(mLastKnownLocation.getLatitude());
		mRequestedCity.setLongitude(mLastKnownLocation.getLongitude());
		mRequestedCity.setName(getString(R.string.menu_menu_current_position));

		CityDatabase.getInstance(getApplicationContext()).editCurrentCity(mRequestedCity);
	}

	@Override
	public void onLocationChanged(Location location) {
		Logcat.e(TAG, "Just received location, latitude: " + location
				.getLatitude() + ", latitude: " + location.getLongitude());

		mGeoLocationManager.unregisterListener();
		mLoadingContainer.setVisibility(View.GONE);
		mLastKnownLocation = location;
		mIsPositionReceived = true;

		prepareCityWithCurrentPosition();
		sendCityClickEvent();
	}

	@Override
	public void onRequestLocationFailed(String errorMesage) {
		Logcat.d(TAG, "onRequestLocationFailed");

		if (mLoadingContainer.getVisibility() == View.VISIBLE) {
			// application started first time so we don't have any data to show
			mProgressBar.setVisibility(View.GONE);
			mLoadingTextView.setText(errorMesage);
		} else {
			Snackbar.make(mViewPager, errorMesage, Snackbar.LENGTH_LONG).show();
		}
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public void onCityMenuClick(CityModel cityModel) {
		mDrawerLayout.closeDrawer(Gravity.LEFT);

		if (getString(R.string.prefs_cities_storage_current_city).equals(cityModel.getId())) {
			// get current position and fetch new data from network
			Logcat.e(TAG, "Obtain new current position");
			mGeoLocationManager.reloadCurrentPosition();
			mIsRequestForCurrentPosition = true;
			loadCurrentPosition();
		} else {
			// fetch data from "db" or network
			mRequestedCity = cityModel;
			getSupportActionBar().setTitle(mRequestedCity.getName());
			sendCityClickEvent();
		}
	}

	private void addCityToDabase() {
		Logcat.d(TAG, "addCityToDatabase");
		boolean result = CityDatabase.getInstance(this).addCity(mRequestedCity);
		if (result) {
			Snackbar.make(mViewPager, getString(R.string.notification_city_added, mRequestedCity.getName()), Snackbar.LENGTH_LONG).show();
		}
		mMenuAdapter.setCities(CityDatabase.getInstance(this).getCities());
	}

	private void removeCityFromDatabase() {
		Logcat.d(TAG, "removeCityFromDatabase");
		boolean result = CityDatabase.getInstance(this).removeCity(mRequestedCity);
		if (result) {
			Snackbar.make(mViewPager, getString(R.string.notification_city_removed, mRequestedCity.getName()), Snackbar.LENGTH_LONG).show();
		}
		mMenuAdapter.setCities(CityDatabase.getInstance(this).getCities());
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
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
