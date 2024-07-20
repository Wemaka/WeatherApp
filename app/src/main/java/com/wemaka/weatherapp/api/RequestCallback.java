package com.wemaka.weatherapp.api;

import com.openmeteo.sdk.WeatherApiResponse;

public interface RequestCallback {
	void onSuccess(WeatherApiResponse response);

	void onFailure(String error);
}
