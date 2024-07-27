package com.wemaka.weatherapp.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wemaka.weatherapp.DayForecast;
import com.wemaka.weatherapp.R;

public class HourlyForecastAdapter extends ListAdapter<DayForecast, HourlyForecastAdapter.ViewHolder> {
//	private final LayoutInflater inflater;

	public HourlyForecastAdapter() {
		super(new Comparator());
//		this.inflater = LayoutInflater.from(context);
	}

	@NonNull
	@Override
	public HourlyForecastAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//		View view = inflater.inflate(R.layout.viewholder_hourly_forecast, parent, false);
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_hourly_forecast, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull HourlyForecastAdapter.ViewHolder holder, int position) {
		holder.bindTo(getItem(position));
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

		public void bindTo(DayForecast item) {
			timeView.setText(item.getDate());
			degreeView.setText(item.getTemperature());
		}
	}

	public static class Comparator extends DiffUtil.ItemCallback<DayForecast> {
		@Override
		public boolean areItemsTheSame(@NonNull DayForecast oldItem, @NonNull DayForecast newItem) {
			return oldItem == newItem;
		}

		@SuppressLint("DiffUtilEquals")
		@Override
		public boolean areContentsTheSame(@NonNull DayForecast oldItem, @NonNull DayForecast newItem) {
			return oldItem == newItem;
		}
	}
}
