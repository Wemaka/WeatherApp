package com.wemaka.weatherapp.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.wemaka.weatherapp.DaysForecastResponse;
import com.wemaka.weatherapp.MainViewModel;
import com.wemaka.weatherapp.DayForecast;
import com.wemaka.weatherapp.adapter.TestAdapter;
import com.wemaka.weatherapp.adapter.decoration.ListPaddingDecoration;
import com.wemaka.weatherapp.databinding.FragmentTodayWeatherBinding;

import java.util.List;

public class TodayWeatherFragment extends Fragment {
	private FragmentTodayWeatherBinding binding;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		binding = FragmentTodayWeatherBinding.inflate(getLayoutInflater());
		return binding.getRoot();
	}

	public static TodayWeatherFragment newInstance() {
		return new TodayWeatherFragment();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		MainViewModel model = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

		model.getLiveData().observe(getViewLifecycleOwner(), item -> {
			DayForecast tf = item.getTodayForecast();
			binding.tvWindSpeed.setText(tf.getWindSpeed());
			binding.tvRainPercent.setText(tf.getPrecipitationChance());
			binding.tvPressureHpa.setText(tf.getPressure());
			binding.tvUv.setText(tf.getUvIndex());
			recyclerViewHourlyForecast(tf.getHourlyForecast());

//			binding.tvWindSpeed.setText(item.getWindSpeed());
//			binding.tvRainPercent.setText(item.getPrecipitationChance());
//			binding.tvPressureHpa.setText(item.getPressure());
//			binding.tvUv.setText(item.getUvIndex());
//			recyclerViewHourlyForecast(item.getHourlyForecast());
		});
	}

	public String getTabTitle() {
		return "Today";
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

		recyclerViewHourlyForecast.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
			@Override
			public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
				int action = e.getAction();

				if (action == MotionEvent.ACTION_MOVE) {
					rv.getParent().requestDisallowInterceptTouchEvent(true);
				} else if (action == MotionEvent.ACTION_CANCEL) {
					rv.getParent().requestDisallowInterceptTouchEvent(false);
				}
				
				return false;
			}

			@Override
			public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
			}

			@Override
			public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
			}
		});

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