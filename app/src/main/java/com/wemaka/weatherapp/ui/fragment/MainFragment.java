package com.wemaka.weatherapp.ui.fragment;

import static androidx.core.app.ActivityCompat.recreate;

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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.api.LocationService;
import com.wemaka.weatherapp.data.store.ProtoDataStoreRepository;
import com.wemaka.weatherapp.databinding.FragmentMainBinding;
import com.wemaka.weatherapp.store.proto.DataStoreProto;
import com.wemaka.weatherapp.store.proto.DayForecastProto;
import com.wemaka.weatherapp.store.proto.SettingsProto;
import com.wemaka.weatherapp.viewmodel.MainViewModel;
import com.zeugmasolutions.localehelper.LocaleHelper;

import java.util.Locale;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainFragment extends Fragment {
	public static final String TAG = "MainFragment";
	private FragmentMainBinding binding;
	private MainViewModel model;
	private LocationService mLocationService;
	private final ActivityResultLauncher<String[]> locationPermissionRequest =
			registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onLocationPermissionResult);
	private static final CompositeDisposable compositeDisposable = new CompositeDisposable();
	private final ProtoDataStoreRepository dataStoreRepository = ProtoDataStoreRepository.getInstance();


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

		model = new ViewModelProvider(this).get(MainViewModel.class);

		initDataStore();
		initLocationService();
		initUi();
		observeViewModel();

		binding.tvCityCountry.setOnClickListener(v -> {

//			Locale locale = new Locale("en");
//			Locale.setDefault(locale);
//			Resources standardResources = getResources();
//			AssetManager assets = standardResources.getAssets();
//			DisplayMetrics metrics = standardResources.getDisplayMetrics();
//			Configuration config = new Configuration(standardResources.getConfiguration());
//			config.setLocale(locale);
//			Resources res = new Resources(assets, metrics, config);
//			getApplicationContext().createConfigurationContext(config);
//			recreate();

			LocaleHelper.INSTANCE.setLocale(requireContext(), new Locale("ru"));
			recreate(requireActivity());
		});
	}

	@Override
	public void onStop() {
		super.onStop();

		Log.i(TAG, "ON STOP");

		SettingsProto settingsProto = new SettingsProto(mLocationService.getLocation(),
				model.getPlaceNameData().getValue());

		compositeDisposable.add(ProtoDataStoreRepository.getInstance()
				.saveDataStore(new DataStoreProto(settingsProto, model.getDaysForecastResponseData().getValue()))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.doOnComplete(() -> Log.i(TAG, "SAVE FORECAST DATASTORE: datastore"))
				.doOnError(e -> Log.e(TAG, "Error saving data", e))
				.subscribe()
		);
	}

	@Override
	public void onDestroy() {
		mLocationService.clearDisposables();

		if (!compositeDisposable.isDisposed()) {
			compositeDisposable.clear();
		}

		Log.i(TAG, "ON DESTROY");

		super.onDestroy();
	}

	public static MainFragment newInstance() {
		return new MainFragment();
	}

	private void initLocationService() {
		mLocationService = new LocationService(requireContext(), model);
		handleLocationPermission();
	}

	private void initDataStore() {
		compositeDisposable.add(dataStoreRepository.getDaysForecastResponse()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(days -> model.getDaysForecastResponseData().setValue(days))
		);

		compositeDisposable.add(dataStoreRepository.getSettings()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(place -> model.getPlaceNameData().setValue(place.locationName))
		);
//
//		compositeDisposable.add(ProtoDataStoreRepository.getInstance().getFlowLocationCoord()
//				.subscribeOn(Schedulers.io())
//				.observeOn(AndroidSchedulers.mainThread())
//				.subscribe(
//						locationCoord -> {
//							if (!mLocationService.getLocation().equals(locationCoord)) {
//								mLocationService.fetchLocation();
//							}
//						},
//						throwable -> Log.e(TAG, "Error observing location coordinates", throwable)
//				));
	}

	private void initUi() {
//		List<Fragment> fragmentList = new ArrayList<>();
//		fragmentList.add(TodayWeatherFragment.newInstance());
//
//		ViewPager2 pager = binding.vpContainer;
//		FragmentStateAdapter pageAdapter = new ViewPagerAdapter(requireActivity(), fragmentList);
//		pager.setAdapter(pageAdapter);

		binding.swipeRefresh.setOnRefreshListener(() -> {
					Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
					handleLocationPermission();
//					mLocationService.fetchLocation();
					binding.swipeRefresh.setRefreshing(false);
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
		model.getDaysForecastResponseData().observe(getViewLifecycleOwner(), forecast -> {
			DayForecastProto df = forecast.dayForecast;

			Log.i(TAG, "EEEEEEEEEEEEEEEEEEEEEEEEEEEEe observeViewModel: " + model);

			binding.tvMainDegree.setText(df.temperature);
			binding.tvFeelsLike.setText(getString(R.string.degree_feels_like, df.apparentTemp));
			binding.imgMainWeatherIcon.setImageResource(df.imgIdWeatherCode);
			binding.tvWeatherMainText.setText(df.weatherCode);
			binding.tvLastUpdate.setText(getString(R.string.info_last_update, df.date));
		});

		model.getPlaceNameData().observe(getViewLifecycleOwner(), place -> {
			binding.tvCityCountry.setText(place);
		});
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

		mLocationService.fetchLocation();
	}

	private void handleLocationPermission() {
		if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
				ActivityCompat.checkSelfPermission(requireContext(),
						Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			ensureLocationProviderEnabled();
		} else {
			locationPermissionRequest.launch(new String[]{
					Manifest.permission.ACCESS_FINE_LOCATION,
					Manifest.permission.ACCESS_COARSE_LOCATION
			});
		}
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