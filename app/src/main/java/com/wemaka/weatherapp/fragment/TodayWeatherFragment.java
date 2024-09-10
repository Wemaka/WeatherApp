package com.wemaka.weatherapp.fragment;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.wemaka.weatherapp.MainViewModel;
import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.activity.LineChartActivity;
import com.wemaka.weatherapp.adapter.HourlyTempForecastAdapter;
import com.wemaka.weatherapp.adapter.decoration.ListPaddingDecoration;
import com.wemaka.weatherapp.databinding.FragmentTodayWeatherBinding;
import com.wemaka.weatherapp.math.UnitConverter;
import com.wemaka.weatherapp.store.proto.DayForecastProto;
import com.wemaka.weatherapp.store.proto.PrecipitationChanceProto;

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

		HourlyTempForecastAdapter hourlyTempForecastAdapter = new HourlyTempForecastAdapter();
		RecyclerView recyclerViewHourlyForecast = binding.rvHourlyForecast;

		recyclerViewHourlyForecast.setAdapter(hourlyTempForecastAdapter);
		recyclerViewHourlyForecast.addItemDecoration(new ListPaddingDecoration(recyclerViewHourlyForecast.getContext(), 25, ListPaddingDecoration.Orientation.HORIZONTAL));
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

		model.getDaysForecastResponseData().observe(getViewLifecycleOwner(), forecast -> {
			DayForecastProto df = forecast.dayForecast;

			binding.tvSunriseTime.setText(df.sunrise);
			binding.tvSunsetTime.setText(df.sunset);
			binding.tvWindSpeed.setText(df.windSpeed.currentWindSpeed);
			binding.tvRainPercent.setText(df.precipitationChance.percent);
			binding.tvPressureHpa.setText(df.pressure.currentPressure);
			binding.tvUv.setText(df.uvIndex.currentUvIndex);
			hourlyTempForecastAdapter.submitList(df.hourlyTempForecast);
			createWeekDayForecast(forecast.weekTempForecast);
			createPrecipitationForecast(df.precipitationChanceForecast);
			binding.tvWindDiff.setText(df.windSpeed.windSpeedDiff);
			binding.tvRainDiff.setText(df.precipitationChance.precipitationChanceDiff);
			binding.tvPressureDiff.setText(df.pressure.pressureDiff);
			binding.tvUvDiff.setText(df.uvIndex.uvIndexDiff);
			binding.imgWindSpeedIndicator.setImageResource(df.windSpeed.imgIdChangeWindSpeed);
			binding.imgRainChanceIndicator.setImageResource(df.precipitationChance.imgIdPrecipitationChance);
			binding.imgPressureIndicator.setImageResource(df.pressure.imgIdChangePressure);
			binding.imgUvIndexIndicator.setImageResource(df.uvIndex.imgIdChangeUvIndex);
		});
	}

	public String getTabTitle() {
		return "Today";
	}

	private void createWeekDayForecast(List<Float> tempForecast) {
		List<String> weekDay = List.of("", "Mon", "Tue", "Wen", "Thu", "Fri", "Sat", "Sun");
		List<Entry> points = new ArrayList<>();

		for (int i = 0; i <= tempForecast.size(); i += 6) {
			int dayIndex = i / 24 + 1;
			float hourFraction = (i % 24) / 24.0f;
			float x = dayIndex + hourFraction - 0.5f;
			float y;

			if (i == tempForecast.size()) {
				y = tempForecast.get(i - 1);
			} else {
				y = tempForecast.get(i);
			}

			points.add(new Entry(x, y));
		}

		LineChartActivity l = new LineChartActivity(binding.chDayForecast);

		l.changeAxisY(weekDay);
		l.setData(new LineDataSet(points, ""));

		l.setAxisYMax(l.getAxisYMax() + 2);
		l.setAxisYMin(l.getAxisYMin() - 2);
		l.getDataSet().setFillDrawable(ContextCompat.getDrawable(binding.getRoot().getContext(),
				R.drawable.gradient_dark_purple));
		l.getDataSet().setHighLightColor(getResources().getColor(R.color.darkPurple, null));
		l.getChart().setMarker(new LineChartActivity.CustomMarkerView(binding.getRoot().getContext(), R.layout.marker_layout));
	}

	private void createPrecipitationForecast(List<PrecipitationChanceProto> precipitationChances) {
		TableLayout tableLayout = binding.tlChanceOfRain;

		for (int i = 0; i < precipitationChances.size(); i++) {
			PrecipitationChanceProto forecastRain = precipitationChances.get(i);

			TableRow tableRow = new TableRow(getActivity());
			tableRow.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

			LayoutInflater inflater = LayoutInflater.from(getActivity());
			LinearProgressIndicator progressBarView = (LinearProgressIndicator) inflater.inflate(R.layout.progress_bar, tableRow, false);

			TextView timeView = new TextView(getActivity());
			TextView percentView = new TextView(getActivity());


			timeView.setText(forecastRain.time);
			percentView.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
			timeView.setTextColor(getResources().getColor(R.color.black, null));
			timeView.setGravity(Gravity.END);

			progressBarView.setProgress(forecastRain.currentPrecipitationChance);
			TableRow.LayoutParams params = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
			if (i == precipitationChances.size() - 1) {
				params.setMargins(UnitConverter.dpToPx(getActivity(), 33), 0, UnitConverter.dpToPx(getActivity(), 22), 0);
			} else {
				params.setMargins(UnitConverter.dpToPx(getActivity(), 33), 0, UnitConverter.dpToPx(getActivity(), 22), UnitConverter.dpToPx(getActivity(), 10));
			}
			progressBarView.setLayoutParams(params);

			percentView.setText(forecastRain.percent);
			percentView.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			percentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
			percentView.setTextColor(getResources().getColor(R.color.black, null));
			percentView.setGravity(Gravity.END);


			tableRow.addView(timeView, 0);
			tableRow.addView(progressBarView, 1);
			tableRow.addView(percentView, 2);

			tableLayout.addView(tableRow, -1);
		}
	}
}