package com.wemaka.weatherapp.data.preferences;

import static com.wemaka.weatherapp.activity.MainActivity.TAG;

import android.content.Context;
import android.location.Location;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;


import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class PreferencesManager {
	private static volatile PreferencesManager INSTANCE;
	private static RxDataStore<Preferences> dataStore;
	private static final Preferences.Key<Double> latKey = PreferencesKeys.doubleKey("location_lat");
	private static final Preferences.Key<Double> lonKey = PreferencesKeys.doubleKey("location_lon");
	private static final double[] DEFAULT_LOCATION = {40.72, -74.00};

//	public PreferencesManager(Context context) {
//		dataStore = new RxPreferenceDataStoreBuilder(context, "settings").build();
//		saveLocation(DEFAULT_LOCATION[0], DEFAULT_LOCATION[1]);
//	}

	private PreferencesManager(Context context) {
		dataStore = new RxPreferenceDataStoreBuilder(context, "settings").build();
		saveLocation(DEFAULT_LOCATION[0], DEFAULT_LOCATION[1]);
	}

	public static PreferencesManager getInstance(Context context) {
		if (INSTANCE == null) {
			synchronized (PreferencesManager.class) {
				if (INSTANCE == null) {
					INSTANCE = new PreferencesManager(context);
				}
			}
		}

		return INSTANCE;
	}

	public Completable saveLocation(double latitude, double longitude) {
		return dataStore.updateDataAsync(prefsIn -> {
			MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
			mutablePreferences.set(latKey, latitude);
			mutablePreferences.set(lonKey, longitude);
			return Single.just(mutablePreferences);
		}).ignoreElement();
	}

	public Location getLocation() {
		Preferences prefs = dataStore.data().firstOrError().blockingGet();
		Double lat = prefs.get(latKey);
		Double lon = prefs.get(lonKey);

//		if (lat != null && lon != null) {
//			Location location = new Location("stored");
//			location.setLatitude(lat);
//			location.setLongitude(lon);
//			return location;
//		}

		Location location = new Location("stored");
		location.setLatitude(lat);
		location.setLongitude(lon);
		return location;
	}
}
