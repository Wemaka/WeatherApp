package com.wemaka.weatherapp.ui.adapter;

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
import com.wemaka.weatherapp.store.proto.HourlyTemperatureProto;

public class HourlyTempForecastAdapter extends ListAdapter<HourlyTemperatureProto,
		HourlyTempForecastAdapter.ViewHolder> {
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
		public final TextView degreeView;
		public final ImageView iconView;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);
			this.timeView = itemView.findViewById(R.id.tvTime);
			this.degreeView = itemView.findViewById(R.id.tvDegree);
			this.iconView = itemView.findViewById(R.id.imgWeatherIcon);
		}


		public void bindTo(HourlyTemperatureProto item) {
			timeView.setText(item.time);
			degreeView.setText(item.temperature + "°");
			iconView.setImageResource(item.imgIdWeatherCode);
		}
	}

	public static class Comparator extends DiffUtil.ItemCallback<HourlyTemperatureProto> {
		@Override
		public boolean areItemsTheSame(@NonNull HourlyTemperatureProto oldItem, @NonNull HourlyTemperatureProto newItem) {
			return oldItem == newItem;
		}

		@SuppressLint("DiffUtilEquals")
		@Override
		public boolean areContentsTheSame(@NonNull HourlyTemperatureProto oldItem, @NonNull HourlyTemperatureProto newItem) {
			return oldItem == newItem;
		}
	}
}
