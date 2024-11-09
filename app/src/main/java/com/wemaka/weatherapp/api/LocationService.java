package com.wemaka.weatherapp.api;

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
import com.google.android.gms.tasks.Task;
import com.wemaka.weatherapp.data.store.ProtoDataStoreRepository;
import com.wemaka.weatherapp.store.proto.LocationCoordProto;

import io.reactivex.rxjava3.core.Single;
import lombok.Getter;
import lombok.Setter;


public class LocationService {
	public static final String TAG = "LocationService";
	public static final double[] DEFAULT_COORD = {40.72, -74.00};
	private static final ProtoDataStoreRepository dataStoreRepository = ProtoDataStoreRepository.getInstance();
	private final LocationManager locationManager;
	private final Context context;
	private final FusedLocationProviderClient fusedLocationClient;
	@Getter
	@Setter
	private LocationCoordProto location = new LocationCoordProto(DEFAULT_COORD[0], DEFAULT_COORD[1]);

	public LocationService(Context context) {
		this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		this.context = context;
		this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
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

	private LocationCoordProto handleLocation(Location loc, Runnable lackLocation) {
		if (loc == null) {
			lackLocation.run();
		} else {
			Log.i(TAG, "Location: " + loc.getLatitude() + " - " + loc.getLongitude());

			location = new LocationCoordProto(loc.getLatitude(), loc.getLongitude());
		}

		return location;
	}

	private Task<Location> getCurrentLocation() throws SecurityException {
		int priority;
		if (isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
			priority = Priority.PRIORITY_HIGH_ACCURACY;
		} else if (isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
			priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY;
		} else {
			Log.i(TAG, "permission not enabled");
			throw new SecurityException();
		}

		return fusedLocationClient.getCurrentLocation(priority, new CancellationTokenSource().getToken());
	}

	private Task<Location> getLastLocation() throws SecurityException {
		return fusedLocationClient.getLastLocation();
	}

	public Single<LocationCoordProto> requestLocation() {
		return Single.create(emitter -> {
			if (!isPermissionGranted()) {
				Log.i(TAG, "NO PERMISSION: " + location);
				emitter.onSuccess(location);
				return;
			}

			if (isProviderEnabled()) {
				getCurrentLocation()
						.addOnSuccessListener(location -> {
							emitter.onSuccess(handleLocation(location,
									() -> Log.i(TAG, "Current location = null 2")));
						})
						.addOnFailureListener(e -> {
							Log.e(TAG, "Failed to get current location", e);
							emitter.onError(e);
						});

			} else {
				getLastLocation()
						.addOnSuccessListener(location -> {
							emitter.onSuccess(handleLocation(location,
									() -> Log.i(TAG, "Last location = null 1")));
						})
						.addOnFailureListener(e -> {
							Log.e(TAG, "Failed to get last location", e);
							emitter.onError(e);
						});
			}
		});
	}
}
