package com.wemaka.weatherapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


import com.google.android.material.progressindicator.LinearProgressIndicator;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.wemaka.weatherapp.DayForecast;
import com.wemaka.weatherapp.LocationService;
import com.wemaka.weatherapp.MainViewModel;
import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.adapter.ViewPagerAdapter;
import com.wemaka.weatherapp.databinding.ActivityMainBinding;
import com.wemaka.weatherapp.domain.HourlyForecastRain;
import com.wemaka.weatherapp.fragment.TodayWeatherFragment;
import com.wemaka.weatherapp.math.UnitConverter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
	public static final String TAG = "MainActivity";
	private ActivityMainBinding binding;
	public LocationService mLocationService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

//		createDayForecast();
//		scrollViewHourlyForecastRain();
		createCustomTabLayout();

		MainViewModel model = new ViewModelProvider(this).get(MainViewModel.class);
		mLocationService = new LocationService(this, model);
		checkLocationPermission();
//		checkLocationProvider();
//		mLocationService.getLocation();

		binding.main.setOnRefreshListener(() -> {
					Log.i(TAG, "onRefresh called from SwipeRefreshLayout");

					myUpdateOperation();
				}
		);

		model.getLiveData().observe(this, item -> {
			DayForecast tf = item.getTodayForecast();

			binding.tvCityCountry.setText(tf.getLocationName());
			binding.tvMainDegree.setText(tf.getTemperature());
			binding.tvFeelsLike.setText("Feels like " + tf.getApparentTemp());
			binding.imgMainWeatherIcon.setImageResource(R.drawable.ic_cloud_and_sun);
			binding.tvWeatherMainText.setText(tf.getWeatherCode());
			binding.tvDegreesTime.setText("Last update\n" + tf.getDate());
		});

		ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
			return insets;
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
//		checkLocationProvider();
	}

	private void createCustomTabLayout() {
		String[] tabTitleButton = new String[]{"Today", "10 days"};
		List<Fragment> fragmentList = new ArrayList<>();
		fragmentList.add(TodayWeatherFragment.newInstance());

		ViewPager2 pager = binding.vpContainer;
		FragmentStateAdapter pageAdapter = new ViewPagerAdapter(this, fragmentList);
		pager.setAdapter(pageAdapter);

		TabLayout tabLayout = binding.tbNavBtn;
		new TabLayoutMediator(tabLayout, pager, (tab, i) -> {
//			tab.setText(fragmentList.get(i).getTabTitle());
//			tab.setText(tabTitleButton[i]);

			LayoutInflater inflater = LayoutInflater.from(this);
			View customView = inflater.inflate(R.layout.custom_tab, tabLayout, false);
			TextView tabTitle = customView.findViewById(R.id.tvTabTitle);
			tabTitle.setText(tabTitleButton[i]);

//			tabLayout.getTabAt(i).setCustomView(R.layout.custom_tab);
//			TextView tabTitle = tabLayout.getTabAt(i).getCustomView().findViewById(R.id.tvTabTitle);
//			tabTitle.setText("Tab " + (i + 1));

			tab.setCustomView(customView);
		}).attach();
		tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getCustomView().findViewById(R.id.lnrlContainerTitle).setBackground(getResources().getDrawable(R.drawable.block_background_select_tab, null));
		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				tab.getCustomView().findViewById(R.id.lnrlContainerTitle).setBackground(getResources().getDrawable(R.drawable.block_background_select_tab, null));
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
				tab.getCustomView().findViewById(R.id.lnrlContainerTitle).setBackground(getResources().getDrawable(R.drawable.block_background_tab, null));
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});
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

		TableLayout tableLayout = binding.tlChanceOfRain;

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