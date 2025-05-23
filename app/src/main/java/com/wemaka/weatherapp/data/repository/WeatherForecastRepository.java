package com.wemaka.weatherapp.data.repository;

import com.wemaka.weatherapp.data.api.GeoLocationClient;
import com.wemaka.weatherapp.data.api.GeoNamesClient;
import com.wemaka.weatherapp.data.api.WeatherClient;
import com.wemaka.weatherapp.data.location.FusedLocationProvider;
import com.wemaka.weatherapp.data.location.LocationProvider;
import com.wemaka.weatherapp.data.model.PlaceInfo;
import com.wemaka.weatherapp.store.proto.DataStoreProto;
import com.wemaka.weatherapp.store.proto.DaysForecastProto;
import com.wemaka.weatherapp.store.proto.LocationCoordProto;
import com.wemaka.weatherapp.store.proto.PressureUnitProto;
import com.wemaka.weatherapp.store.proto.SettingsProto;
import com.wemaka.weatherapp.store.proto.SpeedUnitProto;
import com.wemaka.weatherapp.store.proto.TemperatureUnitProto;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

public class WeatherForecastRepository {
	private final LocationProvider fusedLocationProvider;
	private final WeatherClient weatherClient;
	private final GeoLocationClient geoLocationClient;

	@Inject
	public WeatherForecastRepository(LocationProvider fusedLocationProvider,
	                                 WeatherClient weatherClient,
	                                 GeoLocationClient geoLocationClient) {
		this.fusedLocationProvider = fusedLocationProvider;
		this.weatherClient = weatherClient;
		this.geoLocationClient = geoLocationClient;
	}

	public Single<DaysForecastProto> fetchWeatherForecast(double latitude, double longitude) {
		return weatherClient.fetchWeatherForecast(latitude, longitude);
	}

	public TemperatureUnitProto getTemperatureUnit() {
		return weatherClient.getTemperatureUnit();
	}

	public SpeedUnitProto getSpeedUnit() {
		return weatherClient.getSpeedUnit();
	}

	public PressureUnitProto getPressureUnit() {
		return weatherClient.getPressureUnit();
	}

	public void setTemperatureUnit(TemperatureUnitProto temperatureUnit) {
		weatherClient.setTemperatureUnit(temperatureUnit);
	}

	public void setSpeedUnit(SpeedUnitProto speedUnit) {
		weatherClient.setSpeedUnit(speedUnit);
	}

	public void setPressureUnit(PressureUnitProto pressureUnit) {
		weatherClient.setPressureUnit(pressureUnit);
	}

	public Single<PlaceInfo> fetchNearestPlaceInfo(double latitude, double longitude) {
		return geoLocationClient.fetchNearestPlaceInfo(latitude, longitude);
	}

	public Single<List<PlaceInfo>> searchLocation(String query) {
		return geoLocationClient.searchLocation(query);
	}

	public Maybe<SettingsProto> getSettings() {
		return ProtoDataStoreRepository.INSTANCE.getSettings();
	}

	public Completable saveDataStore(DataStoreProto dataStoreProto) {
		return ProtoDataStoreRepository.INSTANCE.saveDataStore(dataStoreProto);
	}

	public Maybe<DaysForecastProto> getDaysForecastResponse() {
		return ProtoDataStoreRepository.INSTANCE.getDaysForecastResponse();
	}

	public Completable saveLocationCoord(LocationCoordProto coord) {
		return ProtoDataStoreRepository.INSTANCE.saveLocationCoord(coord);
	}

	public LocationCoordProto getLocation() {
		return fusedLocationProvider.getLocation();
	}

	public void setLocation(LocationCoordProto location) {
		fusedLocationProvider.setLocation(location);
	}

	public Single<LocationCoordProto> requestLocation() {
		return fusedLocationProvider.requestLocation();
	}

	public void setLocale(Locale locale) {
		geoLocationClient.setLocale(locale);
	}
}
