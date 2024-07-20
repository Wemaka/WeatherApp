package com.wemaka.weatherapp;

import android.location.Location;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class MainViewModel extends ViewModel {
	private final MutableLiveData<Location> currentLocation = new MutableLiveData<>();

	private final MutableLiveData<DaysForecastResponse> liveData = new MutableLiveData<>();
	private final MutableLiveData<List<DaysForecastResponse>> liveDataList = new MutableLiveData<>();

	public MutableLiveData<Location> getCurrentLocation() {
		return currentLocation;
	}

	public MutableLiveData<DaysForecastResponse> getLiveData() {
		return liveData;
	}

	public void setLiveData(DaysForecastResponse dayForecast) {
		liveData.setValue(dayForecast);
	}
}
