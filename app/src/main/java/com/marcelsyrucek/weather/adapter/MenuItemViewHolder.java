package com.marcelsyrucek.weather.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.marcelsyrucek.weather.R;

/**
 * Created by marcel on 19.6.2015.
 */
public class MenuItemViewHolder extends RecyclerView.ViewHolder {

	ImageView icon;
	TextView title;

	public MenuItemViewHolder(View itemView) {
		super(itemView);
		icon = (ImageView) itemView.findViewById(R.id.navigation_menu_item_icon);
		title = (TextView) itemView.findViewById(R.id.navigation_menu_item_title);
	}

}
