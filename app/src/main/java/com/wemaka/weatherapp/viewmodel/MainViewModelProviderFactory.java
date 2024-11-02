package com.wemaka.weatherapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.wemaka.weatherapp.repository.WeatherForecastRepository;

public class MainViewModelProviderFactory implements ViewModelProvider.Factory {
	private final WeatherForecastRepository weatherForecastRepository;
	private final Application app;

	public MainViewModelProviderFactory(WeatherForecastRepository weatherForecastRepository,
	                                    Application app) {
		this.weatherForecastRepository = weatherForecastRepository;
		this.app = app;
	}

	@SuppressWarnings("unchecked")
	@NonNull
	@Override
	public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
		return (T) new MainViewModel(weatherForecastRepository, app);
	}
}
