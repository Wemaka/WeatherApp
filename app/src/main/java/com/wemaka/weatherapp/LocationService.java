package com.wemaka.weatherapp;

import static com.wemaka.weatherapp.activity.MainActivity.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.activity.result.ActivityResultCallback;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.openmeteo.sdk.WeatherApiResponse;
import com.wemaka.weatherapp.api.RequestCallback;
import com.wemaka.weatherapp.api.WeatherParse;
import com.wemaka.weatherapp.databinding.ActivityMainBinding;

import java.util.concurrent.Executor;

public class LocationService {
	private LocationManager locationManager;
	private Activity activity;
	private boolean enableNetProvider = false;
	private double[] currentLocation = new double[2];
	private MainViewModel mainViewModel;
	private FusedLocationProviderClient fusedLocationClient;

	public LocationService(Activity activity, MainViewModel mainViewModel) {
		this.locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		this.activity = activity;
		this.mainViewModel = mainViewModel;
		this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
	}

	private boolean isLocationEnabled() {
		return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	@SuppressLint("MissingPermission")
	public void getLocation() {
		Log.i(TAG, "getLocation");
		if (!isLocationEnabled()) {
			return;
		}

//		ActivityCompat.requestPermissions(activity,
//				new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
//						Manifest.permission.ACCESS_FINE_LOCATION}, 200);

		if (!isPermission(Manifest.permission.ACCESS_COARSE_LOCATION) && !isPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
			return;
		}

		fusedLocationClient.getLastLocation()
				.addOnSuccessListener(activity, location -> {
					if (location != null) {
						Log.i(TAG, "Location: " + location.getLatitude() + ", " + location.getLongitude());

						mainViewModel.getCurrentLocation().setValue(location);
						locationRequest();
					} else {
						Log.i(TAG, "Location = null");

						WeatherParse weatherParse = new WeatherParse();

						weatherParse.request(
								55.6069,
								37.5199,
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
				});
	}

	public boolean isEnableNetProvider() {
		return enableNetProvider;
	}

	public boolean isPermission(String p) {
		return ActivityCompat.checkSelfPermission(activity, p) == PackageManager.PERMISSION_GRANTED;
	}

//	private final LocationListener locationListener = new LocationListener() {
//		@Override
//		public void onLocationChanged(@NonNull Location location) {
//			Log.i(TAG, "Location: " + location.getLatitude() + ", " + location.getLongitude());
//			setCurrentLocation(new double[]{location.getLatitude(), location.getLongitude()});
//
//			mainViewModel.getCurrentLocation().setValue(location);
//			setLocationViewModel();
//		}
//
//		@Override
//		public void onProviderDisabled(@NonNull String provider) {
//			Log.i(TAG, provider + " disable");
//			enableNetProvider = false;
//		}
//
//		@SuppressLint("MissingPermission")
//		@Override
//		public void onProviderEnabled(@NonNull String provider) {
//			Log.i(TAG,
//					"permission 2 : " + isPermission(Manifest.permission.ACCESS_COARSE_LOCATION));
//
//			if (isPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
//				Log.i(TAG,
//						"ACCESS_COARSE_LOCATION: " + provider + " enabled" + " lastLocation: " + locationManager.getLastKnownLocation(provider));
//			}
//
//			if (isPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
//				Log.i(TAG,
//						"ACCESS_FINE_LOCATION: " + provider + " enabled" + " lastLocation: " + locationManager.getLastKnownLocation(provider));
//			}
//
//			enableNetProvider = true;
////			setLocationViewModel();
//		}
//	};

	private void setCurrentLocation(double[] currentLocation) {
		this.currentLocation = currentLocation;
	}

	public void locationRequest() {
		WeatherParse weatherParse = new WeatherParse();

		weatherParse.request(
				mainViewModel.getCurrentLocation().getValue().getLatitude(),
				mainViewModel.getCurrentLocation().getValue().getLongitude(),
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
