package com.wemaka.weatherapp.activity;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineDataSet;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.openmeteo.sdk.WeatherApiResponse;
import com.wemaka.weatherapp.DayForecastResponse;
import com.wemaka.weatherapp.LocationService;
import com.wemaka.weatherapp.MainViewModel;
import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.adapter.TestAdapter;
import com.wemaka.weatherapp.adapter.ViewPagerAdapter;
import com.wemaka.weatherapp.adapter.decoration.ListPaddingDecoration;
import com.wemaka.weatherapp.api.RequestCallback;
import com.wemaka.weatherapp.api.WeatherParse;
import com.wemaka.weatherapp.databinding.ActivityMainBinding;
import com.wemaka.weatherapp.domain.HourlyForecast;
import com.wemaka.weatherapp.domain.HourlyForecastRain;
import com.wemaka.weatherapp.fragment.TodayWeatherFragment;
import com.wemaka.weatherapp.fragment.TomorrowWeatherFragment;
import com.wemaka.weatherapp.math.UnitConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
	public static final String TAG = "MainActivity";
	private ActivityMainBinding mainBinding;
	public LocationService mLocationService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(mainBinding.getRoot());

//		createDayForecast();
//		scrollViewHourlyForecastRain();

		String[] tabTitleButton = new String[]{"Today", "Tomorrow", "10 days"};
		List<Fragment> fragmentList = new ArrayList<>();
		fragmentList.add(TodayWeatherFragment.newInstance());
		fragmentList.add(TomorrowWeatherFragment.newInstance());
		ViewPager2 pager = mainBinding.vpContainer;
		FragmentStateAdapter pageAdapter = new ViewPagerAdapter(this, fragmentList);
		pager.setAdapter(pageAdapter);
		TabLayout tabLayout = mainBinding.tbNavBtn;
		new TabLayoutMediator(tabLayout, pager, (tab, i) -> {
//			tab.setText(fragmentList.get(i).getTabTitle());
//			tab.setText(tabTitleButton[i]);
			LayoutInflater inflater = LayoutInflater.from(this);
			View customView = inflater.inflate(R.layout.custom_tab, tabLayout, false);
			TextView tabTitle = customView.findViewById(R.id.tvTabTitle);
			tabTitle.setText("Tab " + (i + 1));
			tab.setCustomView(customView);
		}).attach();
		tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getCustomView().findViewById(R.id.lnrlContainerTitle).setBackground(getResources().getDrawable(R.drawable.block_background_select_tab, null));
		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				tab.getCustomView().findViewById(R.id.lnrlContainerTitle).setBackground(getResources().getDrawable(R.drawable.block_background_select_tab, null));
				animateTab(tab, true);
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
				tab.getCustomView().findViewById(R.id.lnrlContainerTitle).setBackground(getResources().getDrawable(R.drawable.block_background_tab, null));
				animateTab(tab, false);
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});


		MainViewModel model = new ViewModelProvider(this).get(MainViewModel.class);
		checkLocationPermission();
//		checkLocationProvider();
		mLocationService = new LocationService(this, model);
//		mLocationService.getLocation();

		mainBinding.main.setOnRefreshListener(() -> {
					Log.i(TAG, "onRefresh called from SwipeRefreshLayout");

					myUpdateOperation();
				}
		);

		model.getLiveData().observe(this, item -> {
			mainBinding.tvCityCountry.setText(item.getLocationName());
			mainBinding.tvMainDegree.setText(item.getCurrentTemp());
			mainBinding.tvFeelsLike.setText("Feels like " + item.getApparentTemp());
			mainBinding.imgMainWeatherIcon.setImageResource(R.drawable.ic_cloud_and_sun);
			mainBinding.tvWeatherMainText.setText(item.getWeatherCode());
			mainBinding.tvDegreesTime.setText("Last update\n" + item.getDate());
			mainBinding.tvWindSpeed.setText(item.getWindSpeed());
			mainBinding.tvRainPercent.setText(item.getPrecipitationChance());
			mainBinding.tvPressureHpa.setText(item.getPressure());
			mainBinding.tvUv.setText(item.getUvIndex());

			recyclerViewHourlyForecast(item.getHourlyForecast());
		});

		ViewCompat.setOnApplyWindowInsetsListener(mainBinding.main, (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
			return insets;
		});
	}

	private void animateTab(TabLayout.Tab tab, boolean isSelected) {
		View customView = tab.getCustomView();
		if (customView != null) {
			int colorFrom = isSelected ?
					getResources().getColor(android.R.color.white) :
					getResources().getColor(R.color.backgroundButtonPurpleSelect);
			int colorTo = isSelected ?
					getResources().getColor(R.color.backgroundButtonPurpleSelect) :
					getResources().getColor(android.R.color.white);

			ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
			colorAnimation.setDuration(300); // Длительность анимации
			colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(@NonNull ValueAnimator animator) {
//					customView.setBackgroundColor((int) animator.getAnimatedValue());
					customView.setBackground((Drawable) animator.getAnimatedValue());
				}
			});
			colorAnimation.start();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
//		checkLocationProvider();
	}

	private void checkLocationPermission() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//			ActivityCompat.requestPermissions(this,
//					new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
//							Manifest.permission.ACCESS_FINE_LOCATION}, 200);

			ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(
					new ActivityResultContracts.RequestMultiplePermissions(), result -> {
						Boolean fineLocationGranted = result.getOrDefault(
								Manifest.permission.ACCESS_FINE_LOCATION, false);
						Boolean coarseLocationGranted = result.getOrDefault(
								Manifest.permission.ACCESS_COARSE_LOCATION, false);

						if ((fineLocationGranted != null && fineLocationGranted) || (coarseLocationGranted != null && coarseLocationGranted)) {
//							mLocationService.getLocation();
							checkLocationProvider();
						} else {
							Log.i(TAG, "No location access granted");
						}
					}
			);

			locationPermissionRequest.launch(new String[]{
					Manifest.permission.ACCESS_FINE_LOCATION,
					Manifest.permission.ACCESS_COARSE_LOCATION
			});
		} else {
			checkLocationProvider();
		}
	}

	private void checkLocationProvider() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			mLocationService.getLocation();
		} else {
			Snackbar.make(findViewById(R.id.sMain), "Enables the location service " +
									"to retrieve weather data for the current location.",
							Snackbar.LENGTH_LONG)
					.setAction("Settings", click -> {
						startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					})
					.setMaxInlineActionWidth(1)
					.show();
		}
	}

	private void recyclerViewHourlyForecast(List<DayForecastResponse> hourlyForecastList) {
//		ArrayList<HourlyForecast> hourlyForecasts = new ArrayList<>();
//		hourlyForecasts.add(new HourlyForecast("Now", R.drawable.ic_cloudy_icon, "10°"));
//		hourlyForecasts.add(new HourlyForecast("10AM", R.drawable.ic_overcast_icon, "8°"));
//		hourlyForecasts.add(new HourlyForecast("11AM", R.drawable.ic_overcast_icon, "5°"));
//		hourlyForecasts.add(new HourlyForecast("12PM", R.drawable.ic_cloudy_icon, "12°"));
//		hourlyForecasts.add(new HourlyForecast("1PM", R.drawable.ic_cloudy_icon, "9°"));
//		hourlyForecasts.add(new HourlyForecast("2PM", R.drawable.ic_overcast_icon, "12°"));

		RecyclerView recyclerViewHourlyForecast = mainBinding.rvHourlyForecast;
//		RecyclerView.Adapter hourlyForecastAdapter = new HourlyForecastAdapter(this, hourlyForecasts);
//		recyclerViewHourlyForecast.setAdapter(hourlyForecastAdapter);
//		recyclerViewHourlyForecast.addItemDecoration(new ListPaddingDecoration(recyclerViewHourlyForecast.getContext(), 25, ListPaddingDecoration.Orientation.HORIZONTAL));

		TestAdapter testAdapter = new TestAdapter();
//		List<DayForecastResponse> lst = new ArrayList<>();
//		lst.add(new DayForecastResponse("", "", "", "12C", "", "01:00", "", "", "", "", "", null,
//				"", "", "", ""));
		recyclerViewHourlyForecast.setAdapter(testAdapter);
		recyclerViewHourlyForecast.addItemDecoration(new ListPaddingDecoration(recyclerViewHourlyForecast.getContext(), 25, ListPaddingDecoration.Orientation.HORIZONTAL));
		Log.i(TAG, "mHourlyForecast: " + hourlyForecastList.toString());
		testAdapter.submitList(hourlyForecastList);
	}

	private void createDayForecast() {
		LineChartActivity l = new LineChartActivity(mainBinding.chDayForecast);
		l.getChart().setDragEnabled(false);
		l.getChart().setScaleEnabled(false);
		l.getChart().setDrawBorders(false);
		l.getChart().setOnChartValueSelectedListener(l);
		l.getChart().getLegend().setEnabled(false);
		l.getChart().setDrawGridBackground(false);
		l.getChart().setMaxHighlightDistance(500);
		l.getChart().getDescription().setEnabled(false);
		l.getChart().getAxisRight().setEnabled(false);
		l.setPosition(XAxis.XAxisPosition.BOTTOM);
		l.setAxisYMax(l.getAxisYMax() * 2);
		l.setAxisYMin(l.getAxisYMin() * 2);

		l.getDataSet().setMode(LineDataSet.Mode.CUBIC_BEZIER);
		l.getDataSet().setColor(Color.BLACK);
		l.getDataSet().setLineWidth(2f);
		l.getDataSet().setDrawCircles(false);
		l.getDataSet().setDrawValues(false);
		l.getDataSet().setDrawFilled(true);
		l.getDataSet().setFillDrawable(ContextCompat.getDrawable(this, R.drawable.gradient_dark_purple));
		l.getDataSet().setDrawHorizontalHighlightIndicator(false);
		l.getDataSet().setHighlightLineWidth(2f);
		l.getDataSet().setHighLightColor(getResources().getColor(R.color.darkPurple, null));
		l.getDataSet().setFormLineDashEffect(new DashPathEffect(new float[]{2f, 2f}, 10f));

		l.getChart().setMarker(new LineChartActivity.CustomMarkerView(this, R.layout.marker_layout));

		l.getChart().animateY(1000, Easing.EaseInOutQuad);
	}

	private void scrollViewHourlyForecastRain() {
		ArrayList<HourlyForecastRain> hourlyForecastRains = new ArrayList<>();
		hourlyForecastRains.add(new HourlyForecastRain("7 PM", 27, "27%"));
		hourlyForecastRains.add(new HourlyForecastRain("8 PM", 44, "44%"));
		hourlyForecastRains.add(new HourlyForecastRain("9 PM", 56, "56%"));
		hourlyForecastRains.add(new HourlyForecastRain("10 PM", 88, "88%"));
		hourlyForecastRains.add(new HourlyForecastRain("10 PM", 88, "88%"));
		hourlyForecastRains.add(new HourlyForecastRain("10 PM", 88, "88%"));
		hourlyForecastRains.add(new HourlyForecastRain("10 PM", 88, "88%"));
		hourlyForecastRains.add(new HourlyForecastRain("10 PM", 88, "88%"));
		hourlyForecastRains.add(new HourlyForecastRain("10 PM", 88, "88%"));
		hourlyForecastRains.add(new HourlyForecastRain("10 PM", 88, "88%"));

		TableLayout tableLayout = mainBinding.tlChanceOfRain;

//		for (HourlyForecastRain forecastRain : hourlyForecastRains) {
//			TableRow tableRow = new TableRow(this);
//
//			View view = LayoutInflater.from(this).inflate(R.layout.viewholder_hourly_forecast_rain, tableRow, false);
//			TextView timeView = view.findViewById(R.id.textView_time_chance_rain);
//			LinearProgressIndicator progressBarView = view.findViewById(R.id.linearProgressIndicator_chance_rain);
//			TextView percentView = view.findViewById(R.id.textView_percent_chance_rain);
//
//			timeView.setText(forecastRain.getTime());
//			progressBarView.setProgress(forecastRain.getProgress());
//			percentView.setText(forecastRain.getPercent());
//
//			TableRow.LayoutParams trParams = new TableRow.LayoutParams(200, LayoutParams.WRAP_CONTENT);
//			trParams.span = 0;
//			tableRow.setLayoutParams(trParams);
//			tableRow.addView(view);
//			tableLayout.addView(tableRow);
//		}


		for (int i = 0; i < hourlyForecastRains.size(); i++) {
			HourlyForecastRain forecastRain = hourlyForecastRains.get(i);

			TableRow tableRow = new TableRow(this);
			tableRow.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

			TextView timeView = new TextView(this);
			LinearProgressIndicator progressBarView = new LinearProgressIndicator(this);
			TextView percentView = new TextView(this);

			timeView.setText(forecastRain.getTime());
			percentView.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			timeView.setTypeface(ResourcesCompat.getFont(this, R.font.productsans));
			timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
			timeView.setTextColor(getResources().getColor(R.color.black, null));
			timeView.setGravity(Gravity.END);

			progressBarView.setMax(100);
			progressBarView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			progressBarView.setProgress(forecastRain.getProgress());
			progressBarView.setTrackThickness(UnitConverter.dpToPx(this, 24));
			progressBarView.setIndicatorColor(getResources().getColor(R.color.backgroundPurpleSecond, null));
			progressBarView.setTrackColor(getResources().getColor(R.color.white, null));
			progressBarView.setTrackCornerRadius(UnitConverter.dpToPx(this, 100));
			TableRow.LayoutParams params = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1);
			params.setMargins(UnitConverter.dpToPx(this, 33), 0, UnitConverter.dpToPx(this, 22), UnitConverter.dpToPx(this, 10));
			if (i == hourlyForecastRains.size() - 1) {
				params.setMargins(UnitConverter.dpToPx(this, 33), 0, UnitConverter.dpToPx(this, 22), 0);
			}
			progressBarView.setLayoutParams(params);

			percentView.setText(forecastRain.getPercent());
			percentView.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			percentView.setTypeface(ResourcesCompat.getFont(this, R.font.productsans));
			percentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
			percentView.setTextColor(getResources().getColor(R.color.black, null));
			percentView.setGravity(Gravity.END);


			tableRow.addView(timeView, 0);
			tableRow.addView(progressBarView, 1);
			tableRow.addView(percentView, 2);

			tableLayout.addView(tableRow, -1);
		}
	}

	public void myUpdateOperation() {
		mLocationService.getLocation();
	}
}