package com.wemaka.weatherapp.data.repository;

import com.wemaka.weatherapp.data.api.GeoNamesClient;
import com.wemaka.weatherapp.data.service.LocationService;
import com.wemaka.weatherapp.data.api.OpenMeteoClient;
import com.wemaka.weatherapp.data.model.PlaceInfo;
import com.wemaka.weatherapp.store.proto.DataStoreProto;
import com.wemaka.weatherapp.store.proto.DaysForecastResponseProto;
import com.wemaka.weatherapp.store.proto.LocationCoordProto;
import com.wemaka.weatherapp.store.proto.SettingsProto;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

public class WeatherForecastRepository {
	private final LocationService locationService;

	public WeatherForecastRepository(LocationService locationService) {
		this.locationService = locationService;
	}

	public Single<DaysForecastResponseProto> fetchWeatherForecast(double latitude, double longitude) {
		return OpenMeteoClient.fetchWeatherForecast(latitude, longitude);
	}

	public Single<PlaceInfo> fetchNearestPlaceInfo(double latitude, double longitude) {
		return GeoNamesClient.fetchNearestPlaceInfo(latitude, longitude);
	}

	public Single<List<PlaceInfo>> searchLocation(String query) {
		return GeoNamesClient.searchLocation(query);
	}

	public Maybe<SettingsProto> getSettings() {
		return ProtoDataStoreRepository.getInstance().getSettings();
	}

	public Completable saveDataStore(DataStoreProto dataStoreProto) {
		return ProtoDataStoreRepository.getInstance().saveDataStore(dataStoreProto);
	}

	public Maybe<DaysForecastResponseProto> getDaysForecastResponse() {
		return ProtoDataStoreRepository.getInstance().getDaysForecastResponse();
	}

	public Completable saveLocationCoord(LocationCoordProto coord) {
		return ProtoDataStoreRepository.getInstance().saveLocationCoord(coord);
	}

	public LocationCoordProto getLocation() {
		return locationService.getLocation();
	}

	public void setLocation(LocationCoordProto location) {
		locationService.setLocation(location);
	}

	public Single<LocationCoordProto> requestLocation() {
		return locationService.requestLocation();
	}
}
