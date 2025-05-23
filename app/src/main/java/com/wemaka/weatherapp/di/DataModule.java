package com.wemaka.weatherapp.di;

import android.content.Context;

import com.wemaka.weatherapp.data.api.GeoLocationClient;
import com.wemaka.weatherapp.data.api.GeoNamesClient;
import com.wemaka.weatherapp.data.api.OpenMeteoClient;
import com.wemaka.weatherapp.data.api.WeatherClient;
import com.wemaka.weatherapp.data.location.FusedLocationProvider;
import com.wemaka.weatherapp.data.location.LocationProvider;
import com.wemaka.weatherapp.data.repository.WeatherForecastRepository;
import com.wemaka.weatherapp.network.ConnectionNetworkProvider;
import com.wemaka.weatherapp.network.NetworkChecker;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DataModule {
	@Provides
	@Singleton
	public static WeatherClient provideWeatherClient() {
		return new OpenMeteoClient();
	}

	@Provides
	@Singleton
	public static GeoLocationClient provideGeoLocationClient() {
		return new GeoNamesClient();
	}

	@Provides
	@Singleton
	public static WeatherForecastRepository provideRepository(LocationProvider locationProvider,
	                                                          WeatherClient weatherClient,
	                                                          GeoLocationClient geoLocationClient) {
		return new WeatherForecastRepository(locationProvider, weatherClient, geoLocationClient);
	}

	@Provides
	@Singleton
	public static LocationProvider provideLocationProvider(@ApplicationContext Context context) {
		return new FusedLocationProvider(context);
	}

	@Provides
	@Singleton
	public static NetworkChecker provideNetworkChecker(@ApplicationContext Context context) {
		return new ConnectionNetworkProvider(context);
	}
}