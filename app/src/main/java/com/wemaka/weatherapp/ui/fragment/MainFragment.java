package com.wemaka.weatherapp.ui.fragment;

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
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.util.Resource;
import com.wemaka.weatherapp.databinding.FragmentMainBinding;
import com.wemaka.weatherapp.store.proto.DataStoreProto;
import com.wemaka.weatherapp.store.proto.DayForecastProto;
import com.wemaka.weatherapp.store.proto.SettingsProto;
import com.wemaka.weatherapp.ui.activity.MainActivity;
import com.wemaka.weatherapp.viewmodel.MainViewModel;

import java.util.Map;

public class MainFragment extends Fragment {
	public static final String TAG = "MainFragment";
	private FragmentMainBinding binding;
	private MainViewModel model;
	private final ActivityResultLauncher<String[]> locationPermissionRequest =
			registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onLocationPermissionResult);


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		binding = FragmentMainBinding.inflate(getLayoutInflater());

		getChildFragmentManager()
				.beginTransaction()
				.replace(binding.flTodayWeather.getId(), TodayWeatherFragment.newInstance())
				.commit();

		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		model = ((MainActivity) requireActivity()).getModel();

		initSavedData();
		initUi();
		observeViewModel();

//		binding.tvCityCountry.setOnClickListener(v -> {
//			LocaleHelper.INSTANCE.setLocale(requireContext(), new Locale("en"));
//
//			recreate(requireActivity());
//		});
	}

	@Override
	public void onStop() {
		super.onStop();

		Log.i(TAG, "ON STOP");

		DataStoreProto dataStoreProto = new DataStoreProto(
				new SettingsProto(model.getLocation(), model.getPlaceName().getValue().getData()),
				model.getDaysForecast().getValue().getData()
		);

		model.saveDataStore(dataStoreProto);
	}

	public static MainFragment newInstance() {
		return new MainFragment();
	}

	private void initSavedData() {
		model.getSavedDaysForecast().observe(getViewLifecycleOwner(), forecast -> {
			model.getDaysForecast().postValue(new Resource.Success<>(forecast));
		});

		model.getSavedSettings().observe(getViewLifecycleOwner(), settings -> {
			model.getPlaceName().postValue(new Resource.Success<>(settings.locationName));
		});

		model.getSavedLocationCoord().observe(getViewLifecycleOwner(), location -> {
			Log.i(TAG, "GET SAVE LOCATION COORDS: " + location);

			model.setLocation(location);

			handleLocationPermission();
		});
	}

	private void initUi() {
		binding.swipeRefresh.setOnRefreshListener(() -> {
					Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
					handleLocationPermission();
				}
		);

		binding.searchBtn.setOnClickListener(v -> {
			SearchMenuFragment searchBottomSheet = new SearchMenuFragment();
			searchBottomSheet.show(requireActivity().getSupportFragmentManager(),
					"SearchBottomSheet");
		});

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
				if (currentId == R.id.startHeader) {
					binding.swipeRefresh.setEnabled(true);
				}
			}

			@Override
			public void onTransitionTrigger(MotionLayout motionLayout, int triggerId, boolean positive, float progress) {
			}
		});
	}

	private void observeViewModel() {
		model.getDaysForecast().observe(getViewLifecycleOwner(), resource -> {
			if (resource.isLoading()) {
				binding.swipeRefresh.setRefreshing(true);

			} else if (resource.isSuccess() && resource.getData() != null) {
				binding.swipeRefresh.setRefreshing(false);

				DayForecastProto df = resource.getData().dayForecast;
				binding.tvMainDegree.setText(df.temperature);
				binding.tvFeelsLike.setText(getString(R.string.degree_feels_like, df.apparentTemp));
				binding.imgMainWeatherIcon.setImageResource(df.imgIdWeatherCode);
				binding.tvWeatherMainText.setText(df.weatherCode);
				binding.tvLastUpdate.setText(getString(R.string.info_last_update, df.date));

			} else if (resource.isError() && resource.getMessage() != null) {
				binding.swipeRefresh.setRefreshing(false);
				showToast(resource.getMessage());
			}
		});

		model.getPlaceName().observe(getViewLifecycleOwner(), resource -> {
			if (resource.isSuccess() && resource.getData() != null) {
				binding.tvCityCountry.setText(resource.getData());

			} else if (resource.isError()) {
				Log.i(TAG, resource.getErrorMes().orElse(""));
			}
		});
	}

	private void showToast(String message) {
		Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
	}

	private void onLocationPermissionResult(Map<String, Boolean> result) {
		Boolean fineLocationGranted = result.getOrDefault(
				Manifest.permission.ACCESS_FINE_LOCATION, false);
		Boolean coarseLocationGranted = result.getOrDefault(
				Manifest.permission.ACCESS_COARSE_LOCATION, false);

		if ((fineLocationGranted != null && fineLocationGranted) || (coarseLocationGranted != null && coarseLocationGranted)) {
			ensureLocationProviderEnabled();
		} else {
			Log.i(TAG, "No location access granted");
		}

//		model.fetchCurrentWeatherAndPlace();
	}

	private void handleLocationPermission() {
		if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
				ActivityCompat.checkSelfPermission(requireContext(),
						Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

//			model.fetchCurrentWeatherAndPlace();

			ensureLocationProviderEnabled();
		} else {
			locationPermissionRequest.launch(new String[]{
					Manifest.permission.ACCESS_FINE_LOCATION,
					Manifest.permission.ACCESS_COARSE_LOCATION
			});
		}

		model.fetchCurrentWeatherAndPlace();
	}

	private void ensureLocationProviderEnabled() {
		LocationManager lm =
				(LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
		if (!(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || lm.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
			Snackbar.make(binding.clMain, "Enables the location service " +
									"to retrieve weather data for the current location.",
							Snackbar.LENGTH_LONG)
					.setAction("Settings", click -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
					.setMaxInlineActionWidth(1)
					.show();
		}
	}
}