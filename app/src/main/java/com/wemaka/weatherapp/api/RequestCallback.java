package com.wemaka.weatherapp.api;

import com.openmeteo.sdk.WeatherApiResponse;
import com.wemaka.weatherapp.DayForecastResponse;

public interface RequestCallback {
	void onSuccess(WeatherApiResponse response);

	void onFailure(String error);
}
