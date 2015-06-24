package com.marcelsyrucek.weather.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.utility.Logcat;

import java.util.ArrayList;

/**
 * Created by marcel on 19.6.2015.
 */
public class MenuAdapter extends RecyclerView.Adapter<MenuItemViewHolder> {

	public interface MenuClickListener {
		void onCityMenuClick(CityModel cityModel);
	}

	public static final String TAG = MenuAdapter.class.getSimpleName();

	public static final int NO_POSITION = -1;

	private ArrayList<CityModel> mCities;
	private MenuClickListener mMenuClickListener;
	private int mLastSelectedPosition = NO_POSITION;

	public MenuAdapter(ArrayList<CityModel> cities, MenuClickListener menuClickListener) {
		mCities = cities;
		mMenuClickListener = menuClickListener;
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

	public void setCities(ArrayList<CityModel> cities) {
		mLastSelectedPosition = NO_POSITION;
		mCities = cities;
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
				Logcat.e(TAG, "last: " + mLastSelectedPosition + ", cur: " + i);
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
