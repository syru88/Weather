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
import com.marcelsyrucek.weather.adapter.MenuItemAdapter;
import com.marcelsyrucek.weather.database.CityDatabase;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.dialog.AboutDialogFragment;
import com.marcelsyrucek.weather.event.LoadCityEvent;
import com.marcelsyrucek.weather.fragment.ForecastFragment;
import com.marcelsyrucek.weather.fragment.TodayFragment;
import com.marcelsyrucek.weather.listener.GeoLocationListener;
import com.marcelsyrucek.weather.utility.GeoLocationManager;
import com.marcelsyrucek.weather.utility.Logcat;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;

public class MainActivity extends AppCompatActivity implements GeoLocationListener, MenuItemAdapter.MenuClickListener {

	public static final String TAG = MainActivity.class.getSimpleName();

	private static final int REQUEST_CODE_RECOVER_GOOGLE_PLAY_SERVICES = 1;

	private static final String BUNDLE_M_IS_POSITION_RECEIVED = "BUNDLE_M_IS_POSITION_RECEIVED";

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
	private CityModel mRequestedCity;
	private boolean mIsPositionReceived = true;

	private Bus mBus = WeatherApplication.bus;
	private MenuItemAdapter mMenuItemAdapter;
	private MenuItem mSearchMenuItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logcat.d(TAG, "onCreate");
		setContentView(R.layout.activity_main);

		if (savedInstanceState != null) {
			mIsPositionReceived = savedInstanceState.getBoolean(BUNDLE_M_IS_POSITION_RECEIVED, true);
		}

		// prepare location manager
		mGeoLocationManager = GeoLocationManager.getInstance(getApplicationContext());

		// setup UI
		setupToolbar();
		setupDrawer();
		setupMainMenu();
		setupViewPager();
		loadCities();

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(BUNDLE_M_IS_POSITION_RECEIVED, mIsPositionReceived);

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
		Logcat.e(TAG, "onResume");

		if (mIsPositionReceived == false) {
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
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_others, menu);

		mSearchMenuItem = menu.findItem(R.id.menu_search);
		SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
		searchView.setQueryHint(getString(R.string.menu_action_search_city_hint));
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				Logcat.d(TAG, "Search submit: " + query);
				mBus.post(new LoadCityEvent(new CityModel(query)));
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
	public boolean onOptionsItemSelected(MenuItem item) {
		Logcat.d(TAG, "onOptionsItemSelected: " + item.getTitle());
		switch (item.getItemId()) {
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
	}

	private void setupMainMenu() {
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.menu_open, R.string
				.menu_close);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();

		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		mCityRecyclerView = (RecyclerView) findViewById(R.id.activity_main_navigation_view);
		mMenuItemAdapter = new MenuItemAdapter(this);
		mCityRecyclerView.setAdapter(mMenuItemAdapter);
		mCityRecyclerView.setLayoutManager(layoutManager);
	}

	private void setupViewPager() {
		Logcat.d(TAG, "setupViewPager");
		mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.activity_main_main_container);
		mViewPager.setAdapter(mPagerAdapter);

		mTabLayout = (TabLayout) findViewById(R.id.activity_main_tab_layout);
		mTabLayout.setupWithViewPager(mViewPager);
	}

	private void loadCities() {
		Logcat.d(TAG, "loadCities");

		// prepare UI
		mLoadingContainer = (ViewGroup) findViewById(R.id.activity_main_loading_container);

		// get current position and if we can't get it, register listener and meanwhile show last current city and
		// refresh its data
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
			}
		} else {
			// we have current location so load data about it
			CityModel unknownCity = new CityModel();
			unknownCity.setLatitude(mLastKnownLocation.getLatitude());
			unknownCity.setLongitude(mLastKnownLocation.getLongitude());
			mRequestedCity = unknownCity;
		}
	}

	@Produce
	public LoadCityEvent produceLoadCityEvent() {
		return new LoadCityEvent(mRequestedCity);
	}

	@Override
	public void lastKnownLocation(Location location) {
		Logcat.e(TAG, "Just received location, longitude: " + location.getLongitude() + ", latitude: " + location
				.getLatitude());
		mGeoLocationManager.unregisterListener();
		mIsPositionReceived = true;

		if (mRequestedCity == null) {
			Logcat.e(TAG, "First start of application and we just received our current position");
			mLoadingContainer.setVisibility(View.GONE);
		}

		mLastKnownLocation = location;

		// if current location isn't in area which has already been requested, refresh data
		if (GeoLocationManager.isLocationInArea(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(),
				mRequestedCity, new float[3]) == false) {
			Logcat.d(TAG, "The new location is different from old one, so refresh");

			CityModel unknownCity = new CityModel();
			unknownCity.setLatitude(mLastKnownLocation.getLatitude());
			unknownCity.setLongitude(mLastKnownLocation.getLongitude());
			mRequestedCity = unknownCity;

//			mBus.post(new LoadCityEvent(mRequestedCity));
		}

	}

	@Override
	public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
		super.onPostCreate(savedInstanceState, persistentState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onMenuClick(int position) {
		Logcat.e(TAG, "POSITION: " + position);
		mDrawerLayout.closeDrawer(Gravity.LEFT);
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
