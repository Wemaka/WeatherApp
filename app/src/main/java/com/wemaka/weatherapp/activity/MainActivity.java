package com.wemaka.weatherapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.wemaka.weatherapp.data.DayForecast;
import com.wemaka.weatherapp.LocationService;
import com.wemaka.weatherapp.MainViewModel;
import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.adapter.ViewPagerAdapter;
import com.wemaka.weatherapp.databinding.ActivityMainBinding;
import com.wemaka.weatherapp.fragment.TodayWeatherFragment;

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

//		binding.main.setOnRefreshListener(() -> {
//					Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
//
//					myUpdateOperation();
//				}
//		);

		binding.motionLayout.setTransitionListener(new MotionLayout.TransitionListener() {
			@Override
			public void onTransitionStarted(MotionLayout motionLayout, int startId, int endId) {
				binding.swipeRefresh.setEnabled(false);
			}

			@Override
			public void onTransitionChange(MotionLayout motionLayout, int startId, int endId, float progress) {
			}

			@Override
			public void onTransitionCompleted(MotionLayout motionLayout, int currentId) {
				if (currentId == R.id.start) {
					binding.swipeRefresh.setEnabled(true);
				}
			}

			@Override
			public void onTransitionTrigger(MotionLayout motionLayout, int triggerId, boolean positive, float progress) {
			}
		});

		model.getLiveData().observe(this, item -> {
			DayForecast tf = item.getTodayForecast();

			binding.tvCityCountry.setText(tf.getLocationName());
			binding.tvMainDegree.setText(tf.getTemperature());
			binding.tvFeelsLike.setText("Feels like " + tf.getApparentTemp());
			binding.imgMainWeatherIcon.setImageResource(Integer.parseInt(tf.getImgWeatherCode()));
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
			LayoutInflater inflater = LayoutInflater.from(this);
			View customView = inflater.inflate(R.layout.custom_tab, tabLayout, false);
			TextView tabTitle = customView.findViewById(R.id.tvTabTitle);
			tabTitle.setText(tabTitleButton[i]);

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
			Snackbar.make(findViewById(R.id.motionLayout), "Enables the location service " +
									"to retrieve weather data for the current location.",
							Snackbar.LENGTH_LONG)
					.setAction("Settings", click -> {
						startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					})
					.setMaxInlineActionWidth(1)
					.show();
		}
	}

	public void myUpdateOperation() {
		mLocationService.getLocation();
	}
}