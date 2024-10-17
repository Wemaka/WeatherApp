package com.wemaka.weatherapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.datastore.rxjava3.RxDataStoreBuilder;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;
import com.wemaka.weatherapp.LocaleHelper;
import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.adapter.ViewPagerAdapter;
import com.wemaka.weatherapp.api.LocationService;
import com.wemaka.weatherapp.data.store.DataStoreSerializer;
import com.wemaka.weatherapp.data.store.ProtoDataStoreRepository;
import com.wemaka.weatherapp.databinding.ActivityMainBinding;
import com.wemaka.weatherapp.fragment.SearchMenuFragment;
import com.wemaka.weatherapp.fragment.TodayWeatherFragment;
import com.wemaka.weatherapp.store.proto.DataStoreProto;
import com.wemaka.weatherapp.store.proto.DayForecastProto;
import com.wemaka.weatherapp.store.proto.SettingsProto;
import com.wemaka.weatherapp.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
	public static final String TAG = "MainActivity";
	private ActivityMainBinding binding;
	private LocationService mLocationService;
	private MainViewModel model;
	private final ActivityResultLauncher<String[]> locationPermissionRequest =
			registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onLocationPermissionResult);
	private static final CompositeDisposable compositeDisposable = new CompositeDisposable();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		model = new ViewModelProvider(this).get(MainViewModel.class);

		setContentView(binding.getRoot());

		initLocationService();
		initUi();
		initDataStore();
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


			LocaleHelper.setLocale(this, "ru");
			recreate();
		});

		ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
			return insets;
		});
	}

	@Override
	protected void onStop() {
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
	protected void onDestroy() {
		mLocationService.clearDisposables();

		if (!compositeDisposable.isDisposed()) {
			compositeDisposable.clear();
		}

		Log.i(TAG, "ON DESTROY");

		super.onDestroy();
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.onAttach(base));
	}

	private void initLocationService() {
		mLocationService = new LocationService(this, model);
		handleLocationPermission();
	}

	private void initDataStore() {
		ProtoDataStoreRepository dataStoreRepository = ProtoDataStoreRepository.getInstance();

		if (dataStoreRepository.getDataStore() == null) {
			dataStoreRepository.setDataStore(
					new RxDataStoreBuilder<>(this, "settings.pb", new DataStoreSerializer()).build());
		}

		compositeDisposable.add(
				dataStoreRepository.getDaysForecastResponse()
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(days -> model.getDaysForecastResponseData().setValue(days))
		);

		compositeDisposable.add(
				dataStoreRepository.getSettings()
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(place -> model.getPlaceNameData().setValue(place.locationName))
		);

		compositeDisposable.add(ProtoDataStoreRepository.getInstance().getFlowLocationCoord()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						locationCoord -> {
							if (!mLocationService.getLocation().equals(locationCoord)) {
								mLocationService.fetchLocation();
							}
						},
						throwable -> Log.e(TAG, "Error observing location coordinates", throwable)
				));
	}

	private void initUi() {
		List<Fragment> fragmentList = new ArrayList<>();
		fragmentList.add(TodayWeatherFragment.newInstance());

		ViewPager2 pager = binding.vpContainer;
		FragmentStateAdapter pageAdapter = new ViewPagerAdapter(this, fragmentList);
		pager.setAdapter(pageAdapter);

		binding.swipeRefresh.setOnRefreshListener(() -> {
					Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
					handleLocationPermission();
//					mLocationService.fetchLocation();
					binding.swipeRefresh.setRefreshing(false);
				}
		);

		binding.searchBtn.setOnClickListener(v -> {
			SearchMenuFragment searchBottomSheet = new SearchMenuFragment();
			searchBottomSheet.show(getSupportFragmentManager(), "SearchBottomSheet");
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
		model.getDaysForecastResponseData().observe(this, forecast -> {
			DayForecastProto df = forecast.dayForecast;

			binding.tvMainDegree.setText(df.temperature);
			binding.tvFeelsLike.setText(getString(R.string.degree_feels_like, df.apparentTemp));
			binding.imgMainWeatherIcon.setImageResource(df.imgIdWeatherCode);
			binding.tvWeatherMainText.setText(df.weatherCode);
			binding.tvLastUpdate.setText(getString(R.string.info_last_update, df.date));
		});

		model.getPlaceNameData().observe(this, place -> {
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
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			ensureLocationProviderEnabled();
		} else {
			locationPermissionRequest.launch(new String[]{
					Manifest.permission.ACCESS_FINE_LOCATION,
					Manifest.permission.ACCESS_COARSE_LOCATION
			});
		}
	}

	private void ensureLocationProviderEnabled() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || lm.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
			Snackbar.make(binding.motionLayout, "Enables the location service " +
									"to retrieve weather data for the current location.",
							Snackbar.LENGTH_LONG)
					.setAction("Settings", click -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
					.setMaxInlineActionWidth(1)
					.show();
		}
	}
}