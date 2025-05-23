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
		ListPreference languageList = findPreference(PREF_KEY_LANGUAGE);
		ListPreference temperatureList = findPreference(PREF_KEY_TEMPERATURE);
		ListPreference windSpeedList = findPreference(PREF_KEY_WIND_SPEED);
		ListPreference pressureList = findPreference(PREF_KEY_AIR_PRESSURE);

		if (languageList != null) {
			languageList.setOnPreferenceChangeListener(this::getLanguagePrefsListener);
		}

		if (temperatureList != null) {
			temperatureList.setOnPreferenceChangeListener(this::getTemperaturePrefsListener);
		}

		if (windSpeedList != null) {
			windSpeedList.setOnPreferenceChangeListener(this::getWindSpeedPrefsListener);
		}

		if (pressureList != null) {
			pressureList.setOnPreferenceChangeListener(this::getPressureListener);
		}
	}

	private boolean getLanguagePrefsListener(Preference preference, Object newValue) {
		Locale newLocale = new Locale(newValue.toString());

		model.fetchNearestPlaceInfo(newLocale);
		((MainActivity) requireActivity()).updateLocale(newLocale);

		return true;
	}

	private boolean getTemperaturePrefsListener(Preference preference, Object newValue) {
		model.changeTemperatureUnit(
				TemperatureUnitProto.valueOf(newValue.toString().toUpperCase())
		);

		return true;
	}

	private boolean getWindSpeedPrefsListener(Preference preference, Object newValue) {
		model.changeSpeedUnit(
				SpeedUnitProto.valueOf(newValue.toString().toUpperCase())
		);

		return true;
	}

	private boolean getPressureListener(Preference preference, Object newValue) {
		model.changePressureUnit(
				PressureUnitProto.valueOf(newValue.toString().toUpperCase())
		);

		return true;
	}
}