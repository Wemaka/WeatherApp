package com.wemaka.weatherapp.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.data.api.GeoNamesClient;
import com.wemaka.weatherapp.data.api.OpenMeteoClient;
import com.wemaka.weatherapp.store.proto.PressureUnitProto;
import com.wemaka.weatherapp.store.proto.SpeedUnitProto;
import com.wemaka.weatherapp.store.proto.TemperatureUnitProto;
import com.wemaka.weatherapp.ui.MainActivity;
import com.wemaka.weatherapp.ui.viewmodel.MainViewModel;

import java.util.Locale;

public class SettingsFragment extends PreferenceFragmentCompat {
	public static final String TAG = "SettingsFragment";
	public static final String PREF_KEY_LANGUAGE = "languagePrefs";
	public static final String PREF_KEY_TEMPERATURE = "temperaturePrefs";
	public static final String PREF_KEY_WIND_SPEED = "windSpeedPrefs";
	public static final String PREF_KEY_AIR_PRESSURE = "airPressurePrefs";
	private MainViewModel model;

	@Override
	public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
		setPreferencesFromResource(R.xml.root_preferences, rootKey);
	}

	@NonNull
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		model = ((MainActivity) requireActivity()).getModel();

		setSettingsListeners();
	}

	@NonNull
	public static SettingsFragment newInstance() {
		return new SettingsFragment();
	}

	private void setSettingsListeners() {
		ListPreference languageList = getPreferenceManager().findPreference(PREF_KEY_LANGUAGE);
		ListPreference temperatureList = getPreferenceManager().findPreference(PREF_KEY_TEMPERATURE);
		ListPreference windSpeedList = getPreferenceManager().findPreference(PREF_KEY_WIND_SPEED);
		ListPreference pressureList = getPreferenceManager().findPreference(PREF_KEY_AIR_PRESSURE);

		if (languageList != null) {
			languageList.setOnPreferenceChangeListener(getLanguagePrefsListener());
		}

		if (temperatureList != null) {
			temperatureList.setOnPreferenceChangeListener(getTemperaturePrefsListener());
		}

		if (windSpeedList != null) {
			windSpeedList.setOnPreferenceChangeListener(getWindSpeedPrefsListener());
		}

		if (pressureList != null) {
			pressureList.setOnPreferenceChangeListener(getPressureListener());
		}
	}

	@NonNull
	private Preference.OnPreferenceChangeListener getLanguagePrefsListener() {
		return (preference, newValue) -> {
			Log.i(TAG, "Change language: " + newValue.toString());

			Locale newLocale = new Locale(newValue.toString());

			GeoNamesClient.setLocale(newLocale);
			model.fetchNearestPlaceInfo();

			((MainActivity) requireActivity()).updateLocale(newLocale);

			return true;
		};
	}

	@NonNull
	private Preference.OnPreferenceChangeListener getTemperaturePrefsListener() {
		return (preference, newValue) -> {
			Log.i(TAG, "Change temperature unit: " + newValue.toString());

			TemperatureUnitProto newUnit = TemperatureUnitProto.valueOf(newValue.toString().toUpperCase());

			OpenMeteoClient.setTemperatureUnit(newUnit);
			model.changeTemperatureUnit(newUnit);

			return true;
		};
	}

	@NonNull
	private Preference.OnPreferenceChangeListener getWindSpeedPrefsListener() {
		return (preference, newValue) -> {
			Log.i(TAG, "Change wind speed unit: " + newValue.toString());

			SpeedUnitProto newUnit = SpeedUnitProto.valueOf(newValue.toString().toUpperCase());

			OpenMeteoClient.setSpeedUnit(newUnit);
			model.changeSpeedUnit(newUnit);

			return true;
		};
	}

	@NonNull
	private Preference.OnPreferenceChangeListener getPressureListener() {
		return (preference, newValue) -> {
			Log.i(TAG, "Change pressure unit: " + newValue.toString());

			PressureUnitProto newUnit = PressureUnitProto.valueOf(newValue.toString().toUpperCase());

			OpenMeteoClient.setPressureUnit(newUnit);
			model.changePressureUnit(newUnit);

			return true;
		};
	}
}