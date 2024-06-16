package com.wemaka.weatherapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wemaka.weatherapp.R;

public class TomorrowWeatherFragment extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_tomorrow_weather, container, false);
	}

	public static TomorrowWeatherFragment newInstance() {
		return new TomorrowWeatherFragment();
	}

	public String getTabTitle() {
		return "Tomorrow";
	}
}