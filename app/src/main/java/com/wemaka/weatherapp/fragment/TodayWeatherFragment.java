package com.wemaka.weatherapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wemaka.weatherapp.R;

public class TodayWeatherFragment extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_today_weather, container, false);
	}

	public static TodayWeatherFragment newInstance() {
		return new TodayWeatherFragment();
	}

	public String getTabTitle() {
		return "Today";
	}
}