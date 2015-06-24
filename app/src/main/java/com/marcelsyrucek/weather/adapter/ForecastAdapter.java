package com.marcelsyrucek.weather.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marcelsyrucek.weather.R;
import com.marcelsyrucek.weather.database.model.ForecastWeatherModel;
import com.marcelsyrucek.weather.utility.Logcat;
import com.marcelsyrucek.weather.utility.WeatherUtility;

import java.util.ArrayList;

/**
 * Created by marcel on 23.6.2015.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastItemViewHolder> {

	public static final String TAG = ForecastAdapter.class.getSimpleName();

	private ArrayList<ForecastWeatherModel> mDays;
	private int mTempPreference;
	private String mDegreeUnit;

	public ForecastAdapter(String degreeUnit) {
		mDegreeUnit = degreeUnit;
	}

	public void setDays(ArrayList<ForecastWeatherModel> days, int tempPreference) {
		mDays = days;
		mTempPreference = tempPreference;
		notifyDataSetChanged();
	}

	@Override
	public ForecastItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_forecast_item, parent, false);
		ForecastItemViewHolder viewHolder = new ForecastItemViewHolder(view);

		return viewHolder;
	}

	@Override
	public void onBindViewHolder(ForecastItemViewHolder holder, int position) {
		ForecastWeatherModel model = mDays.get(position);

		holder.temperature.setText(WeatherUtility.getTemperature(mTempPreference, model.getTemperature()) + mDegreeUnit);
		holder.description.setText(model.getDescription());
	}

	@Override
	public int getItemCount() {
		if (mDays == null) {
			return 0;
		} else {
			return mDays.size();
		}
	}
}
