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
import com.wemaka.weatherapp.data.preferences.PreferencesManager;


public class LocationService {
	private LocationManager locationManager;
	private Activity activity;
	private MainViewModel mainViewModel;
	private FusedLocationProviderClient fusedLocationClient;

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
		Location loc = PreferencesManager.getInstance(activity).getLocation();
		Log.i(TAG, "PREFERENCES LOC: " + loc.getProvider());

		mainViewModel.getCurrentLocation().setValue(loc);

		//! getLastLocation(); getCurrentLocation(); передать в них колбеки для уведомления
		// успеха или неудачи получения местоположения

		if (isProviderEnabled() && isPermissionGranted()) {
			getCurrentLocation();
			return;
		}

		if (!isProviderEnabled() && isPermissionGranted()) {
			getLastLocation();
			return;
		}

//		if (!isProviderEnabled() || !isPermissionGranted()) {
//			Log.i(TAG, "provider or permission not enabled");
//
//			Location loc = PreferencesManager.getInstance(activity).getLocation();
//			Log.i(TAG, "PREFERENCES LOC: " + loc.getProvider());
//
//			mainViewModel.getCurrentLocation().setValue(loc);
//
//			return;
//		}

	}

	private void handleLocation(Location location, Runnable lackLocation) {
		if (location == null) {
			lackLocation.run();
			return;
		}

		Log.i(TAG, "Location: " + location.getLatitude() + ", " + location.getLongitude());

		mainViewModel.getCurrentLocation().setValue(location);
	}

	private void getLastLocation() throws SecurityException {
		fusedLocationClient.getLastLocation()
				.addOnSuccessListener(activity, location -> {
					handleLocation(location, () -> {
						Log.i(TAG, "location = null 1");
						getCurrentLocation();
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

		fusedLocationClient.getCurrentLocation(priority, new CancellationTokenSource().getToken())
				.addOnSuccessListener(activity, location -> {
					handleLocation(location, () -> {
						Log.i(TAG, "Location = null 2");

						//! кринж. добавить колбек
//						Location loc = PreferencesManager.getInstance(activity).getLocation();
//						mainViewModel.getCurrentLocation().setValue(loc);
					});
				})
				.addOnFailureListener(e -> {
					Log.e(TAG, "Failed to get current location", e);
				});
	}

	private interface LocationCallback {
		void onSuccess(boolean response);

//		void onFailure(String error);
	}
}
