package com.wemaka.weatherapp;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;
import static com.wemaka.weatherapp.activity.MainActivity.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnSuccessListener;
import com.openmeteo.sdk.WeatherApiResponse;
import com.wemaka.weatherapp.api.RequestCallback;
import com.wemaka.weatherapp.api.WeatherParse;
import com.wemaka.weatherapp.data.MyLocation;
import com.wemaka.weatherapp.data.Settings;
import com.wemaka.weatherapp.data.preferences.PreferencesManager;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public class LocationService {
	private final LocationManager locationManager;
	private final Activity activity;
	private final MainViewModel mainViewModel;
	private final FusedLocationProviderClient fusedLocationClient;
	private static final PreferencesManager preferencesManager = PreferencesManager.getInstance();

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

	@SuppressLint("MissingPermission")
	public void getLocation() {
		Location loc = PreferencesManager.getInstance().getLocation();
		Log.i(TAG, "PREFERENCES LOC: " + loc.getProvider());

		if (!isPermissionGranted()) {
			weatherRequest(preferencesManager.getLocation());
			return;
		}

		if (isProviderEnabled()) {
			getCurrentLocation();
		} else {
			getLastLocation();
		}
	}

	private void handleLocation(Location location, Runnable lackLocation) {
		if (location == null) {
			lackLocation.run();
			weatherRequest(preferencesManager.getLocation());
		} else {
			Log.i(TAG, "Location: " + location.getLatitude() + ", " + location.getLongitude());

			preferencesManager.saveLocation(location.getLatitude(), location.getLongitude())
					.andThen(Completable.fromAction(() -> {
						Log.i(TAG, "Saved location");
						weatherRequest(preferencesManager.getLocation());
					})).subscribe();
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

	private void weatherRequest(Location loc) {
		if (loc == null) {
			return;
		}

		WeatherParse weatherParse = new WeatherParse();

		weatherParse.request(
				loc.getLatitude(),
				loc.getLongitude(),
				new RequestCallback() {
					@Override
					public void onSuccess(WeatherApiResponse response) {
						mainViewModel.getLiveData().postValue(weatherParse.parseWeatherData(response));
					}

					@Override
					public void onFailure(String error) {
						Log.e(TAG, "ERROR REQUEST: api.open-meteo " + error);
					}
				}
		);
	}
}
