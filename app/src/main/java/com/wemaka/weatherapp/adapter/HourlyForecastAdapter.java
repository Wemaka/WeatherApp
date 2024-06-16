package com.wemaka.weatherapp.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.domain.HourlyForecast;

import java.util.List;

public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder> {
	public static RecyclerView.ItemDecoration itemDecoration;
	private final LayoutInflater inflater;
	private final List<HourlyForecast> hourlyForecasts;

	public HourlyForecastAdapter(@NonNull Context context, @NonNull List<HourlyForecast> objects) {
		this.inflater = LayoutInflater.from(context);
		this.hourlyForecasts = objects;
	}

	@NonNull
	@Override
	public HourlyForecastAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = inflater.inflate(R.layout.viewholder_hourly_forecast, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(HourlyForecastAdapter.ViewHolder holder, int position) {
		HourlyForecast hourlyForecast = hourlyForecasts.get(position);
		holder.timeView.setText(hourlyForecast.getTime());
		holder.iconView.setImageResource(hourlyForecast.getIconResource());
		holder.degreeView.setText(hourlyForecast.getDegree());
	}

	@Override
	public int getItemCount() {
		return hourlyForecasts.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public final TextView timeView;
		public final ImageView iconView;
		public final TextView degreeView;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);
			timeView = itemView.findViewById(R.id.textView_time);
			iconView = itemView.findViewById(R.id.imageView_weather_icon);
			degreeView = itemView.findViewById(R.id.textView_degree);
		}
	}
}
