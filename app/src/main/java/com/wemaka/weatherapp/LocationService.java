package com.wemaka.weatherapp;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;
import static com.wemaka.weatherapp.activity.MainActivity.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.openmeteo.sdk.WeatherApiResponse;
import com.wemaka.weatherapp.api.RequestCallback;
import com.wemaka.weatherapp.api.WeatherParse;
import com.wemaka.weatherapp.data.store.ProtoDataStoreRepository;
import com.wemaka.weatherapp.store.proto.LocationCoordProto;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.Getter;


public class LocationService {
	private final LocationManager locationManager;
	private final Activity activity;
	private final MainViewModel mainViewModel;
	private final FusedLocationProviderClient fusedLocationClient;
	private static final ProtoDataStoreRepository dataStoreRepository =
			ProtoDataStoreRepository.getInstance();
	private static final CompositeDisposable compositeDisposable = new CompositeDisposable();
	@Getter
	private static final LocationCoordProto DEFAULT_LOCATION = new LocationCoordProto(40.72, -74.00);
	@Getter
	private LocationCoordProto location = DEFAULT_LOCATION;

	public LocationService(Activity activity, MainViewModel mainViewModel) {
		this.locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		this.activity = activity;
		this.mainViewModel = mainViewModel;
		this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
	}

	private boolean isProviderEnabled() {
		return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public boolean isPermissionGranted(String p) {
		return ActivityCompat.checkSelfPermission(activity, p) == PackageManager.PERMISSION_GRANTED;
	}

	public boolean isPermissionGranted() {
		return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
				ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
	}

	public void fetchLocation() {
		AtomicReference<LocationCoordProto> locCoord = new AtomicReference<>(DEFAULT_LOCATION);

		compositeDisposable.add(dataStoreRepository.getSettings()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.doAfterTerminate(() -> {
					Log.i(TAG, "COMPLETE SETTINGS GET 1");

					if (!isPermissionGranted()) {
						Log.i(TAG, "NO PERMISSION");
						weatherRequest(locCoord.get());
						return;
					}

					if (isProviderEnabled()) {
						getCurrentLocation();
					} else {
						getLastLocation();
					}
				})
				.subscribe(
						settings -> {
							Log.i(TAG, "SETTINGS GET 1: " + settings);
							locCoord.set(settings.locationCoord);
						},
						throwable -> Log.e(TAG, "LocationService#getLocation", throwable)
				)
		);
	}

	private void handleLocation(Location location, Runnable lackLocation) {
		if (location == null) {
			lackLocation.run();

			compositeDisposable.add(dataStoreRepository.getSettings()
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(
							settings -> {
								Log.i(TAG, "SETTINGS GET 2");
								weatherRequest(settings.locationCoord);
							},
							throwable -> Log.e(TAG, "LocationService#handleLocation 1", throwable),
							() -> {
								weatherRequest(DEFAULT_LOCATION);
							}
					)
			);
		} else {
			Log.i(TAG, "Location: " + location.getLatitude() + " - " + location.getLongitude());

			weatherRequest(new LocationCoordProto(location.getLatitude(), location.getLongitude()));
		}
	}

	private void getLastLocation() throws SecurityException {
		fusedLocationClient.getLastLocation()
				.addOnSuccessListener(activity, location -> {
					handleLocation(location, () -> {
						Log.i(TAG, "Last location = null 1");
					});
				})
				.addOnFailureListener(e -> {
					Log.e(TAG, "Failed to get last location", e);
				});
	}

	private void getCurrentLocation() throws SecurityException {
		int priority;
		if (isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
			priority = Priority.PRIORITY_HIGH_ACCURACY;
		} else if (isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
			priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY;
		} else {
			Log.i(TAG, "permission not enabled");
			throw new SecurityException();
		}

		fusedLocationClient.getCurrentLocation(priority,
						new CancellationTokenSource().getToken())
				.addOnSuccessListener(activity, location -> {
					handleLocation(location, () -> {
						Log.i(TAG, "Current location = null 2");
					});
				})
				.addOnFailureListener(e -> {
					Log.e(TAG, "Failed to get current location", e);
				});
	}

	private void weatherRequest(LocationCoordProto loc) {
		if (loc == null) {
			return;
		}

		WeatherParse weatherParse = new WeatherParse();

		weatherParse.request(
				loc.latitude,
				loc.longitude,
				new RequestCallback() {
					@Override
					public void onSuccess(WeatherApiResponse response) {
//						DaysForecastResponseProto daysForecast = weatherParse.parseWeatherData(response);

//						SettingsProto settingsProto = new SettingsProto(
//								new LocationCoordProto(loc.latitude, loc.longitude)
//						);
//
//						dataStoreRepository.saveDataStore(new DataStoreProto(settingsProto, daysForecast))
//								.doOnComplete(() -> Log.i(TAG, "SAVE FORECAST DATASTORE"))
//								.subscribe();

						location = new LocationCoordProto(loc.latitude, loc.longitude);

						mainViewModel.getDaysForecastResponseData().postValue(weatherParse.parseWeatherData(response));
					}

					@Override
					public void onFailure(String error) {
						Log.e(TAG, "ERROR REQUEST: api.open-meteo " + error);
					}
				}
		);
	}

	public void clearDisposables() {
		if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
			compositeDisposable.dispose();
		}
	}
}
