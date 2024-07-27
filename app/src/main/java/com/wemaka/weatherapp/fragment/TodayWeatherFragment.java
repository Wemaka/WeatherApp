package com.wemaka.weatherapp.fragment;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.wemaka.weatherapp.MainViewModel;
import com.wemaka.weatherapp.DayForecast;
import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.activity.LineChartActivity;
import com.wemaka.weatherapp.adapter.HourlyForecastAdapter;
import com.wemaka.weatherapp.adapter.decoration.ListPaddingDecoration;
import com.wemaka.weatherapp.databinding.FragmentTodayWeatherBinding;

import java.util.ArrayList;
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
			createDayForecast(item.getWeekTempForecast());
		});
	}

	public String getTabTitle() {
		return "Today";
	}

	private void recyclerViewHourlyForecast(List<DayForecast> hourlyForecastList) {
//		ArrayList<HourlyForecast> hourlyForecasts = new ArrayList<>();
//		hourlyForecasts.add(new HourlyForecast("Now", R.drawable.ic_cloudy_icon, "10°"));
//		hourlyForecasts.add(new HourlyForecast("10AM", R.drawable.ic_overcast_icon, "8°"));

		RecyclerView recyclerViewHourlyForecast = binding.rvHourlyForecast;
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

		HourlyForecastAdapter hourlyForecastAdapter = new HourlyForecastAdapter();
		recyclerViewHourlyForecast.setAdapter(hourlyForecastAdapter);
		recyclerViewHourlyForecast.addItemDecoration(new ListPaddingDecoration(recyclerViewHourlyForecast.getContext(), 25, ListPaddingDecoration.Orientation.HORIZONTAL));
		hourlyForecastAdapter.submitList(hourlyForecastList);
	}

	private void createDayForecast(List<Float> tempForecast) {
		List<String> weekDay = List.of("", "Mon", "Tue", "Wen", "Thu", "Fri", "Sat", "Sun");
		List<Entry> points = new ArrayList<>();
		LineChartActivity l = new LineChartActivity(binding.chDayForecast);

		for (int i = 0; i <= tempForecast.size(); i += 6) {
			int dayIndex = i / 24 + 1; // Индекс дня недели
			float hourFraction = (i % 24) / 24.0f; // Доля суток
			float x = dayIndex + hourFraction - 0.5f; // Координата x
			float y;

			if (i == tempForecast.size()) {
				y = tempForecast.get(i - 1);
			} else {
				y = tempForecast.get(i);
			}

			points.add(new Entry(x, y));
		}

		l.changeAxisY(weekDay);
		l.setData(new LineDataSet(points, ""));

		l.setAxisYMax(l.getAxisYMax() + 2);
		l.setAxisYMin(l.getAxisYMin() - 2);
		l.getDataSet().setFillDrawable(ContextCompat.getDrawable(binding.getRoot().getContext(),
				R.drawable.gradient_dark_purple));
		l.getDataSet().setHighLightColor(getResources().getColor(R.color.darkPurple, null));
		l.getChart().setMarker(new LineChartActivity.CustomMarkerView(binding.getRoot().getContext(), R.layout.marker_layout));
	}
}