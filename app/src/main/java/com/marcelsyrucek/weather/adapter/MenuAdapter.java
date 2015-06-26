package com.marcelsyrucek.weather.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.utility.Logcat;

import java.util.ArrayList;

/**
 * Created by marcel on 21.6.2015.
 */
public class MenuAdapter extends RecyclerView.Adapter<MenuItemViewHolder> {

	public interface MenuClickListener {
		void onCityMenuClick(CityModel cityModel);
	}

	public static final String TAG = MenuAdapter.class.getSimpleName();

	public static final int NO_POSITION = -1;

	private Context mContext;
	private ArrayList<CityModel> mCities;
	private MenuClickListener mMenuClickListener;
	private int mLastSelectedPosition = NO_POSITION;

	public MenuAdapter(ArrayList<CityModel> cities, MenuClickListener menuClickListener, Context context) {
		mMenuClickListener = menuClickListener;
		mContext = context;
		setCities(cities);
	}

	public int getLastSelectedPosition() {
		return mLastSelectedPosition;
	}

	public void setLastSelectedPosition(int position) {
		if (position == NO_POSITION) {
			return;
		}

		mLastSelectedPosition = position;
		mCities.get(position).setIsSelected(true);
	}

	public void unSelectPosition() {
		if (mLastSelectedPosition != NO_POSITION) {
			mCities.get(mLastSelectedPosition).setIsSelected(false);
			notifyItemChanged(mLastSelectedPosition);
			mLastSelectedPosition = NO_POSITION;
		}
	}

	public void setCities(ArrayList<CityModel> cities) {
		mLastSelectedPosition = NO_POSITION;

		CityModel currentPosition = cities.get(0);
		for (int i = 0, size = cities.size(); i < size; i++) {
			if (mContext.getString(R.string.prefs_storage_current_city_key).equals(cities.get(i).getId())) {
				Logcat.e(TAG, "Current is on: " + i);
				currentPosition = cities.get(i);
				cities.remove(i);
				break;
			}
		}

		mCities = cities;
		mCities.add(0, currentPosition);
		notifyDataSetChanged();
	}

	@Override
	public MenuItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

		View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.navigation_menu_item, viewGroup,
				false);
		MenuItemViewHolder viewHolder = new MenuItemViewHolder(view);

		return viewHolder;
	}

	@Override
	public void onBindViewHolder(final MenuItemViewHolder menuItemViewHolder, final int i) {
		final CityModel model = mCities.get(i);

		menuItemViewHolder.itemView.setSelected(model.isSelected());

		menuItemViewHolder.title.setText(model.getName());
		if (i == 0) {
			menuItemViewHolder.icon.setImageResource(R.drawable.ic_gps_fixed_grey600_18dp);
		} else {
			menuItemViewHolder.icon.setImageResource(R.drawable.ic_today_grey600_18dp);
		}
		menuItemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Logcat.d(TAG, "last: " + mLastSelectedPosition + ", cur: " + i);
				if (mLastSelectedPosition != NO_POSITION) {
					mCities.get(mLastSelectedPosition).setIsSelected(false);
					notifyItemChanged(mLastSelectedPosition);
				}

				menuItemViewHolder.itemView.setSelected(true);
				mCities.get(i).setIsSelected(true);
				mLastSelectedPosition = i;
				mMenuClickListener.onCityMenuClick(model);
			}
		});
	}

	@Override
	public int getItemCount() {
		return mCities.size();
	}
}
