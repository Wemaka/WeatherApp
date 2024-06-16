package com.wemaka.weatherapp;

import android.location.Location;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class MainViewModel extends ViewModel {
	private final MutableLiveData<Location> currentLocation = new MutableLiveData<>();

	private final MutableLiveData<DayForecastResponse> liveData = new MutableLiveData<>();
	private final MutableLiveData<List<DayForecastResponse>> liveDataList = new MutableLiveData<>();

	public MutableLiveData<Location> getCurrentLocation() {
		return currentLocation;
	}

	public MutableLiveData<DayForecastResponse> getLiveData() {
		return liveData;
	}

	public void setLiveData(DayForecastResponse dayForecast) {
		liveData.setValue(dayForecast);
	}
}
