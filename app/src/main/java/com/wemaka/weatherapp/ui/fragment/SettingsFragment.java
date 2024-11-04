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
import com.zeugmasolutions.localehelper.LocaleHelper;

import java.util.Locale;

public class SettingsFragment extends PreferenceFragmentCompat {
	public static final String TAG = "SettingsFragment";

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.root_preferences, rootKey);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ListPreference listLang = getPreferenceManager().findPreference("language");

		if (listLang != null) {
			listLang.setOnPreferenceChangeListener((preference, newValue) -> {
				Log.i(TAG, "Change language: " + newValue.toString());
				LocaleHelper.INSTANCE.setLocale(requireContext(), new Locale(newValue.toString()));
				requireActivity().recreate();
				return false;
			});
		}
	}

	public static SettingsFragment newInstance() {
		return new SettingsFragment();
	}
}