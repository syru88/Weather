package com.marcelsyrucek.weather.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.database.model.CityModel;

import java.util.ArrayList;

/**
 * Created by marcel on 19.6.2015.
 */
public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemViewHolder> {

	public interface MenuClickListener {
		void onMenuClick(int position);
	}

	private MenuClickListener mListener;
	private ArrayList<CityModel> mCities;

	public MenuItemAdapter(MenuClickListener listener) {
		mListener = listener;
	}

	public void setCities(ArrayList<CityModel> cities) {
		mCities = cities;
	}

	@Override
	public MenuItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

		View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.navigation_menu_item, viewGroup,
				false);
		MenuItemViewHolder viewHolder = new MenuItemViewHolder(view);

		return viewHolder;
	}

	@Override
	public void onBindViewHolder(MenuItemViewHolder menuItemViewHolder, final int i) {
		CityModel model = mCities.get(i);

		menuItemViewHolder.title.setText(model.getName());
		if (i == 0) {
			menuItemViewHolder.icon.setImageResource(R.drawable.ic_gps_fixed_grey600_18dp);
		} else {
			menuItemViewHolder.icon.setImageResource(R.drawable.ic_today_grey600_18dp);
		}
		menuItemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onMenuClick(i);
			}
		});
	}

	@Override
	public int getItemCount() {
		if (mCities == null) {
			return 0;
		} else {
			return mCities.size();
		}
	}
}
