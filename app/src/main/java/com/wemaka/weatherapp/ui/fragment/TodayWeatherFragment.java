package com.wemaka.weatherapp.ui.fragment;

import android.icu.util.Calendar;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.databinding.FragmentTodayWeatherBinding;
import com.wemaka.weatherapp.store.proto.DayForecastProto;
import com.wemaka.weatherapp.store.proto.HourlyTemperatureProto;
import com.wemaka.weatherapp.store.proto.PrecipitationChanceProto;
import com.wemaka.weatherapp.ui.MainActivity;
import com.wemaka.weatherapp.ui.adapter.HourlyTempForecastAdapter;
import com.wemaka.weatherapp.ui.adapter.decoration.ListPaddingDecoration;
import com.wemaka.weatherapp.ui.view.LineChartView;
import com.wemaka.weatherapp.ui.viewmodel.MainViewModel;
import com.wemaka.weatherapp.util.math.UnitConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TodayWeatherFragment extends Fragment {
	public static final String TAG = "TodayWeatherFragment";
	private FragmentTodayWeatherBinding binding;
	private MainViewModel model;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		binding = FragmentTodayWeatherBinding.inflate(getLayoutInflater());
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		model = ((MainActivity) requireActivity()).getModel();

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

		model.getDaysForecast().observe(getViewLifecycleOwner(), resource -> {
			if (resource.isSuccess() && resource.getData() != null) {
				DayForecastProto df = resource.getData().dayForecast;

				binding.tvSunriseTime.setText(df.sunrise);
				binding.tvSunsetTime.setText(df.sunset);
				binding.tvWindSpeed.setText(getString(R.string.speed_insert_kmh, df.windSpeed.currentWindSpeed));
				binding.tvRainPercent.setText(df.precipitationChance.percent + "%");
				binding.tvPressureHpa.setText(getString(R.string.air_pressure_insert_hpa, df.pressure.currentPressure));
				binding.tvUv.setText(df.uvIndex.currentUvIndex + "");

				List<HourlyTemperatureProto> formatTemperatureList = new ArrayList<>(df.hourlyTempForecast);
				formatTemperatureList.set(0,
						formatTemperatureList.get(0).newBuilder().time(getString(R.string.text_now)).build());
				hourlyTempForecastAdapter.submitList(formatTemperatureList);

				createWeekDayForecast(resource.getData().weekTempForecast);

				List<PrecipitationChanceProto> formatPrecipitationList = new ArrayList<>(df.precipitationChanceForecast);
				formatPrecipitationList.set(0,
						formatPrecipitationList.get(0).newBuilder().time(getString(R.string.text_now)).build());
				createPrecipitationForecast(formatPrecipitationList);

				binding.tvWindDiff.setText(getString(R.string.speed_insert_kmh, df.windSpeed.windSpeedDiff));
				binding.tvRainDiff.setText(df.precipitationChance.precipitationChanceDiff + "%");
				binding.tvPressureDiff.setText(getString(R.string.air_pressure_insert_hpa, df.pressure.pressureDiff));
				binding.tvUvDiff.setText(df.uvIndex.uvIndexDiff + "");
				binding.imgWindSpeedIndicator.setImageResource(df.windSpeed.imgIdChangeWindSpeed);
				binding.imgRainChanceIndicator.setImageResource(df.precipitationChance.imgIdPrecipitationChance);
				binding.imgPressureIndicator.setImageResource(df.pressure.imgIdChangePressure);
				binding.imgUvIndexIndicator.setImageResource(df.uvIndex.imgIdChangeUvIndex);
			}
		});
	}

	public static TodayWeatherFragment newInstance() {
		return new TodayWeatherFragment();
	}

	private void createWeekDayForecast(List<Float> tempForecast) {
		String[] daysOfWeek = getResources().getStringArray(R.array.days_of_week);
		List<String> days = new ArrayList<>(Arrays.asList(daysOfWeek));
		int firstDayOfWeek = Calendar.getInstance(Locale.getDefault()).getFirstDayOfWeek();

		if (firstDayOfWeek == Calendar.SUNDAY) {
			days.add(0, days.remove(days.size() - 1));
		}
		days.add(0, "");

		List<Entry> points = new ArrayList<>(tempForecast.size() / 6 + 1);

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

		LineChartView lineChart = new LineChartView(binding.chDayForecast);

		lineChart.changeAxisY(days);
		lineChart.setData(new LineDataSet(points, ""));

		lineChart.setAxisYMax(lineChart.getAxisYMax() + 2);
		lineChart.setAxisYMin(lineChart.getAxisYMin() - 2);
		lineChart.getDataSet().setFillDrawable(ContextCompat.getDrawable(binding.getRoot().getContext(),
				R.drawable.gradient_dark_purple));
		lineChart.getDataSet().setHighLightColor(getResources().getColor(R.color.darkPurple, null));
		lineChart.getChart().setMarker(new LineChartView.CustomMarkerView(binding.getRoot().getContext(), R.layout.marker_layout));
	}

	private void createPrecipitationForecast(List<PrecipitationChanceProto> precipitationChances) {
		TableLayout tableLayout = binding.tlChanceOfRain;
		tableLayout.removeAllViews();

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
				params.setMargins(UnitConverter.dpToPx(requireActivity(), 33), 0,
						UnitConverter.dpToPx(requireActivity(), 22), 0);
			} else {
				params.setMargins(UnitConverter.dpToPx(requireActivity(), 33), 0,
						UnitConverter.dpToPx(requireActivity(), 22), UnitConverter.dpToPx(requireActivity(), 10));
			}
			progressBarView.setLayoutParams(params);

			percentView.setText(forecastRain.percent + "%");
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