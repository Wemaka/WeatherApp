package com.wemaka.weatherapp.data.preferences;

import android.content.Context;
import android.location.Location;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;


import com.wemaka.weatherapp.data.Settings;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.Getter;
import lombok.Setter;

public class PreferencesManager {
	@Getter
	private static final PreferencesManager instance = new PreferencesManager();
	@Setter
	@Getter
//	private RxDataStore<Preferences> dataStore;
	private AtomicReference<RxDataStore<Preferences>> dataStore = new AtomicReference<>();
	private static final Preferences.Key<Double> latKey = PreferencesKeys.doubleKey("location_lat");
	private static final Preferences.Key<Double> lonKey = PreferencesKeys.doubleKey("location_lon");
	private static final double[] DEFAULT_LOCATION = {40.72, -74.00};

	private PreferencesManager() {
	}

	public synchronized Completable saveSettings(Settings settings) {
		return dataStore.get().updateDataAsync(prefsIn -> {
			MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();

			if (settings.getLocation() != null) {
				mutablePreferences.set(latKey, settings.getLocation().getLatitude());
				mutablePreferences.set(latKey, settings.getLocation().getLongitude());
			}

			return Single.just(mutablePreferences);
		}).ignoreElement();
	}


	public synchronized Completable saveLocation(double latitude, double longitude) {
		return dataStore.get().updateDataAsync(prefsIn -> {
			MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
			mutablePreferences.set(latKey, latitude);
			mutablePreferences.set(lonKey, longitude);
			return Single.just(mutablePreferences);
		}).ignoreElement();
	}

	public synchronized Location getLocation() {
		Preferences prefs = dataStore.get().data().firstOrError().blockingGet();
		Double lat = prefs.get(latKey);
		Double lon = prefs.get(lonKey);

		Location location = new Location("stored");

		if (lat != null && lon != null) {
			location.setLatitude(lat);
			location.setLongitude(lon);
			return location;
		}

		location.setLatitude(DEFAULT_LOCATION[0]);
		location.setLongitude(DEFAULT_LOCATION[1]);
		return location;
	}
}
