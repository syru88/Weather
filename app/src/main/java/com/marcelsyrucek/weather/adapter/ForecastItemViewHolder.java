package com.marcelsyrucek.weather.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.marcelsyrucek.weather.R;

/**
 * Created by marcel on 23.6.2015.
 */
public class ForecastItemViewHolder extends RecyclerView.ViewHolder {

	ImageView icon;
	TextView description;
	TextView temperature;

	public ForecastItemViewHolder(View itemView) {
		super(itemView);
		icon = (ImageView) itemView.findViewById(R.id.fragment_forecast_item_icon);
		description = (TextView) itemView.findViewById(R.id.fragment_forecast_item_description);
		temperature = (TextView) itemView.findViewById(R.id.fragment_forecast_item_temp);
	}
}
