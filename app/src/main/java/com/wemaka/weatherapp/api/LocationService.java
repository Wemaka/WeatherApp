package com.wemaka.weatherapp.api;

import static com.wemaka.weatherapp.ui.activity.MainActivity.TAG;

import android.Manifest;
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
import com.wemaka.weatherapp.data.store.ProtoDataStoreRepository;
import com.wemaka.weatherapp.store.proto.LocationCoordProto;
import com.wemaka.weatherapp.viewmodel.MainViewModel;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.Getter;


public class LocationService {
	private static final ProtoDataStoreRepository dataStoreRepository = ProtoDataStoreRepository.getInstance();
	@Getter
	private static final LocationCoordProto DEFAULT_LOCATION = new LocationCoordProto(40.72, -74.00);
	private final LocationManager locationManager;
	private final Context context;
	private final MainViewModel mainViewModel;
	private final FusedLocationProviderClient fusedLocationClient;
	private final AtomicReference<LocationCoordProto> location = new AtomicReference<>(DEFAULT_LOCATION);
	private final CompositeDisposable compositeDisposable = new CompositeDisposable();
	private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

	public LocationService(Context context, MainViewModel mainViewModel) {
		this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		this.context = context;
		this.mainViewModel = mainViewModel;
		this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
	}

	public LocationCoordProto getLocation() {
		return location.get();
	}

	private boolean isProviderEnabled() {
		return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public boolean isPermissionGranted(String p) {
		return ActivityCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED;
	}

	public boolean isPermissionGranted() {
		return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
				ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
	}

	public void fetchLocation() {
		compositeDisposable.add(dataStoreRepository.getSettings()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.doAfterTerminate(() -> {
					Log.i(TAG, "COMPLETE SETTINGS GET 1");

					if (!isPermissionGranted()) {
						Log.i(TAG, "NO PERMISSION");
						fetchWeatherAndPlaceName(getLocation());
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
							location.set(settings.locationCoord);
						},
						throwable -> Log.e(TAG, "LocationService#fetchLocation", throwable),
						() -> Log.i(TAG, "getSettings completed without emitting any data")
				)
		);
	}

	private void handleLocation(Location loc, Runnable lackLocation) {
		if (loc == null) {
			lackLocation.run();
		} else {
			Log.i(TAG, "Location: " + loc.getLatitude() + " - " + loc.getLongitude());

			location.set(new LocationCoordProto(loc.getLatitude(), loc.getLongitude()));
		}

		fetchWeatherAndPlaceName(getLocation());
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

		CancellationTokenSource tokenSource = new CancellationTokenSource();

		fusedLocationClient.getCurrentLocation(priority, tokenSource.getToken())
				.addOnSuccessListener(location -> {
					handleLocation(location, () -> {
						Log.i(TAG, "Current location = null 2");
					});
					tokenSource.cancel();
				})
				.addOnFailureListener(e -> {
					Log.e(TAG, "Failed to get current location", e);
					tokenSource.cancel();
				});
	}

	private void getLastLocation() throws SecurityException {
		CancellationTokenSource tokenSource = new CancellationTokenSource();

		fusedLocationClient.getLastLocation()
				.addOnSuccessListener(location -> {
					handleLocation(location, () -> {
						Log.i(TAG, "Last location = null 1");
					});
					tokenSource.cancel();
				})
				.addOnFailureListener(e -> {
					Log.e(TAG, "Failed to get last location", e);
					tokenSource.cancel();
				});
	}

	public void fetchWeatherAndPlaceName(LocationCoordProto loc) {
		if (loc == null) {
			return;
		}

		compositeDisposable.add(OpenMeteoClient.fetchWeatherForecast(loc.latitude, loc.longitude)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						response -> {
							mainViewModel.getDaysForecastResponseData().postValue(OpenMeteoClient.parseWeatherData(response));
						},
						error -> Log.e(TAG, "ERROR REQUEST: api.open-meteo " + error)
				)
		);

		compositeDisposable.add(GeoNamesClient.fetchNearestPlaceInfo(loc.latitude, loc.longitude)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						response -> {
							mainViewModel.getPlaceNameData().postValue(response.getToponymName());
						},
						error -> Log.e(TAG, "ERROR REQUEST: api.geonames " + error)
				)
		);
	}

	public void clearDisposables() {
		if (!compositeDisposable.isDisposed()) {
			compositeDisposable.clear();
		}

		cancellationTokenSource.cancel();
	}
}
