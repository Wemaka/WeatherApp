package com.wemaka.weatherapp;

import android.location.Location;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.wemaka.weatherapp.data.DaysForecastResponse;

import java.util.List;

import lombok.Getter;

public class MainViewModel extends ViewModel {
	@Getter
	private final MutableLiveData<Location> currentLocation = new MutableLiveData<>();

	@Getter
	private final MutableLiveData<DaysForecastResponse> liveData = new MutableLiveData<>();
	private final MutableLiveData<List<DaysForecastResponse>> liveDataList = new MutableLiveData<>();

	public void setLiveData(DaysForecastResponse dayForecast) {
		liveData.setValue(dayForecast);
	}
}
