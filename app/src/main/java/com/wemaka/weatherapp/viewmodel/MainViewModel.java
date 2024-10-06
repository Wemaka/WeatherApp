package com.wemaka.weatherapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.wemaka.weatherapp.store.proto.DaysForecastResponseProto;

import lombok.Getter;

public class MainViewModel extends ViewModel {
	@Getter
	private final MutableLiveData<DaysForecastResponseProto> daysForecastResponseData =
			new MutableLiveData<>();

	@Getter
	private final MutableLiveData<String> placeNameData = new MutableLiveData<>();
//	private final MutableLiveData<List<DaysForecastResponse>> liveDataList = new MutableLiveData<>();

//	public void setDaysForecastResponseData(DaysForecastResponseProto forecast) {
//		daysForecastResponseData.setValue(forecast);
//	}
}
