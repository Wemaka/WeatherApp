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

import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.data.Temperature;

public class HourlyTempForecastAdapter extends ListAdapter<Temperature, HourlyTempForecastAdapter.ViewHolder> {
	public HourlyTempForecastAdapter() {
		super(new Comparator());
	}

	@NonNull
	@Override
	public HourlyTempForecastAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_hourly_forecast, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull HourlyTempForecastAdapter.ViewHolder holder, int position) {
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

		public void bindTo(Temperature item) {
			timeView.setText(item.getTime());
			degreeView.setText(item.getTemperature());
		}
	}

	public static class Comparator extends DiffUtil.ItemCallback<Temperature> {
		@Override
		public boolean areItemsTheSame(@NonNull Temperature oldItem, @NonNull Temperature newItem) {
			return oldItem == newItem;
		}

		@SuppressLint("DiffUtilEquals")
		@Override
		public boolean areContentsTheSame(@NonNull Temperature oldItem, @NonNull Temperature newItem) {
			return oldItem == newItem;
		}
	}
}
