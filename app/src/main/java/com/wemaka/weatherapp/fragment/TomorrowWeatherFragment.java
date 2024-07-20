package com.wemaka.weatherapp.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.wemaka.weatherapp.DayForecast;
import com.wemaka.weatherapp.DaysForecastResponse;
import com.wemaka.weatherapp.MainViewModel;
import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.adapter.TestAdapter;
import com.wemaka.weatherapp.adapter.decoration.ListPaddingDecoration;
import com.wemaka.weatherapp.api.WeatherParse;
import com.wemaka.weatherapp.databinding.FragmentTodayWeatherBinding;
import com.wemaka.weatherapp.databinding.FragmentTomorrowWeatherBinding;

import java.util.List;

public class TomorrowWeatherFragment extends Fragment {
	private FragmentTomorrowWeatherBinding binding;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		binding = FragmentTomorrowWeatherBinding.inflate(getLayoutInflater());
		return binding.getRoot();
	}

	public static TomorrowWeatherFragment newInstance() {
		return new TomorrowWeatherFragment();
	}

	public String getTabTitle() {
		return "Tomorrow";
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		MainViewModel model = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

		model.getLiveData().observe(getViewLifecycleOwner(), item -> {
			DayForecast tf = item.getTomorrowForecast();
			binding.tvWindSpeed.setText(tf.getWindSpeed());
			binding.tvRainPercent.setText(tf.getPrecipitationChance());
			binding.tvPressureHpa.setText(tf.getPressure());
			binding.tvUv.setText(tf.getUvIndex());
			recyclerViewHourlyForecast(tf.getHourlyForecast());

		});
	}


	private void recyclerViewHourlyForecast(List<DaysForecastResponse> hourlyForecastList) {
//		ArrayList<HourlyForecast> hourlyForecasts = new ArrayList<>();
//		hourlyForecasts.add(new HourlyForecast("Now", R.drawable.ic_cloudy_icon, "10°"));
//		hourlyForecasts.add(new HourlyForecast("10AM", R.drawable.ic_overcast_icon, "8°"));
//		hourlyForecasts.add(new HourlyForecast("11AM", R.drawable.ic_overcast_icon, "5°"));
//		hourlyForecasts.add(new HourlyForecast("12PM", R.drawable.ic_cloudy_icon, "12°"));
//		hourlyForecasts.add(new HourlyForecast("1PM", R.drawable.ic_cloudy_icon, "9°"));
//		hourlyForecasts.add(new HourlyForecast("2PM", R.drawable.ic_overcast_icon, "12°"));

		RecyclerView recyclerViewHourlyForecast = binding.rvHourlyForecast;
//		RecyclerView.Adapter hourlyForecastAdapter = new HourlyForecastAdapter(this, hourlyForecasts);
//		recyclerViewHourlyForecast.setAdapter(hourlyForecastAdapter);
//		recyclerViewHourlyForecast.addItemDecoration(new ListPaddingDecoration(recyclerViewHourlyForecast.getContext(), 25, ListPaddingDecoration.Orientation.HORIZONTAL));

		TestAdapter testAdapter = new TestAdapter();
//		List<DayForecastResponse> lst = new ArrayList<>();
//		lst.add(new DayForecastResponse("", "", "", "12C", "", "01:00", "", "", "", "", "", null,
//				"", "", "", ""));
		recyclerViewHourlyForecast.setAdapter(testAdapter);
		recyclerViewHourlyForecast.addItemDecoration(new ListPaddingDecoration(recyclerViewHourlyForecast.getContext(), 25, ListPaddingDecoration.Orientation.HORIZONTAL));
		Log.i("TodayWeatherFragment", "mHourlyForecast: " + hourlyForecastList.toString());
		testAdapter.submitList(hourlyForecastList);
	}
}