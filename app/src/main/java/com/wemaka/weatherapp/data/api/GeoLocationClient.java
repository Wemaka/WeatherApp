package com.wemaka.weatherapp.data.api;

import com.wemaka.weatherapp.data.model.PlaceInfo;

import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.core.Single;

public interface GeoLocationClient {
	Single<PlaceInfo> fetchNearestPlaceInfo(double latitude, double longitude);

	Single<List<PlaceInfo>> searchLocation(String query);

	Locale getLocale();

	void setLocale(Locale locale);
}
