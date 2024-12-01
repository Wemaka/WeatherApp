package com.wemaka.weatherapp.ui.viewmodel;


import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.data.model.PlaceInfo;
import com.wemaka.weatherapp.data.repository.WeatherForecastRepository;
import com.wemaka.weatherapp.store.proto.DataStoreProto;
import com.wemaka.weatherapp.store.proto.DayForecastProto;
import com.wemaka.weatherapp.store.proto.DaysForecastProto;
import com.wemaka.weatherapp.store.proto.LocationCoordProto;
import com.wemaka.weatherapp.store.proto.PressureUnitProto;
import com.wemaka.weatherapp.store.proto.SettingsProto;
import com.wemaka.weatherapp.store.proto.SpeedUnitProto;
import com.wemaka.weatherapp.store.proto.TemperatureProto;
import com.wemaka.weatherapp.store.proto.TemperatureUnitProto;
import com.wemaka.weatherapp.util.Resource;
import com.wemaka.weatherapp.util.math.UnitConverter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.Getter;

public class MainViewModel extends AndroidViewModel {
	public static final String TAG = "MainViewModel";
	private final WeatherForecastRepository repository;
	private final CompositeDisposable compositeDisposable = new CompositeDisposable();
	@Getter
	private final MutableLiveData<Resource<DaysForecastProto>> daysForecast = new MutableLiveData<>();
	@Getter
	private final MutableLiveData<Resource<String>> placeName = new MutableLiveData<>();

	public MainViewModel(WeatherForecastRepository repository, @NonNull Application app) {
		super(app);
		this.repository = repository;
		initData();
	}

	@Override
	protected void onCleared() {
		super.onCleared();

		if (!compositeDisposable.isDisposed()) {
			compositeDisposable.clear();
		}
	}

	private void initData() {
		Log.i(TAG, "INIT DATA");

		compositeDisposable.add(repository.getDaysForecastResponse()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						forecast -> daysForecast.postValue(new Resource.Success<>(forecast))
				)
		);

		compositeDisposable.add(repository.getSettings()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						settings -> placeName.postValue(new Resource.Success<>(settings.locationName))
				)
		);

		compositeDisposable.add(repository.getSettings()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.doAfterTerminate(this::fetchCurrentWeatherAndPlace)
				.subscribe(
						settings -> {
							Log.i(TAG, "init getSettings: " + settings);
							setLocation(settings.locationCoord);
						},
						throwable -> {
							Log.e(TAG, "init getSettings error", throwable);
							setLocation(getLocation());
						},
						() -> {
							Log.i(TAG, "init getSettings complete");
							setLocation(getLocation());
						}
				)
		);
	}

	public LocationCoordProto getLocation() {
		return repository.getLocation();
	}

	public void setLocation(LocationCoordProto location) {
		Log.i(TAG, "SET LOCATION: " + location);
		repository.setLocation(location);
	}

	public void saveDataStore(DataStoreProto dataStoreProto) {
		compositeDisposable.add(repository.saveDataStore(dataStoreProto)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						() -> Log.i(TAG, "SAVE FORECAST DATASTORE: datastore"),
						error -> Log.e(TAG, "Error saving data", error)
				)
		);
	}

	public LiveData<LocationCoordProto> getSavedLocationCoord() {
		MutableLiveData<LocationCoordProto> liveData = new MutableLiveData<>();

		compositeDisposable.add(repository.getSettings()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						settings -> {
							Log.i(TAG, "getLocationCoord 1: " + settings);
							liveData.postValue(settings.locationCoord);
						},
						throwable -> {
							Log.e(TAG, "getSavedLocationCoord", throwable);
							liveData.postValue(getLocation());
						},
						() -> {
							Log.e(TAG, "getSavedLocationCoord Complete");
							liveData.postValue(getLocation());
						}
				)
		);

		return liveData;
	}

	public LiveData<SettingsProto> getSavedSettings() {
		MutableLiveData<SettingsProto> liveData = new MutableLiveData<>();

		compositeDisposable.add(repository.getSettings()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(liveData::postValue)
		);

		return liveData;
	}

	public LiveData<DaysForecastProto> getSavedDaysForecast() {
		MutableLiveData<DaysForecastProto> liveData = new MutableLiveData<>();

		compositeDisposable.add(repository.getDaysForecastResponse()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(liveData::postValue)
		);

		return liveData;
	}

	public void fetchCurrentWeatherAndPlace() {
		Log.i(TAG, "fetchCurrentWeatherAndPlace()");

		compositeDisposable.add(repository.requestLocation()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						this::fetchWeatherAndPlace,
						error -> Log.e(TAG, error.getMessage(), error)
				)
		);
	}

	public void fetchWeatherAndPlace(LocationCoordProto location) {
		setLocation(location);
		safeFetchWeatherForecast(location.latitude, location.longitude);
		safeFetchNearestPlaceInfo(location.latitude, location.longitude);
	}

	public void fetchNearestPlaceInfo(double latitude, double longitude) {
		safeFetchNearestPlaceInfo(latitude, longitude);
	}

	public void fetchWeatherForecast() {
		LocationCoordProto loc = getLocation();
		safeFetchWeatherForecast(loc.latitude, loc.longitude);
	}

	public void fetchNearestPlaceInfo() {
		LocationCoordProto loc = getLocation();
		safeFetchNearestPlaceInfo(loc.latitude, loc.longitude);
	}

	public void fetchWeatherForecast(double latitude, double longitude) {
		safeFetchWeatherForecast(latitude, longitude);
	}

	private void safeFetchWeatherForecast(double latitude, double longitude) {
		daysForecast.postValue(new Resource.Loading<>());

		if (!hasInternetConnection()) {
			daysForecast.postValue(new Resource.Error<>(getApplication().getResources().getString(R.string.no_internet_connection)));
			return;
		}

		compositeDisposable.add(repository.fetchWeatherForecast(latitude, longitude)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						forecast -> daysForecast.postValue(new Resource.Success<>(forecast)),
						error -> daysForecast.postValue(new Resource.Error<>("Couldn't get the weather forecast"))
				)
		);
	}

	private void safeFetchNearestPlaceInfo(double latitude, double longitude) {
		placeName.postValue(new Resource.Loading<>());

		if (!hasInternetConnection()) {
			placeName.postValue(new Resource.Error<>(getApplication().getResources().getString(R.string.no_internet_connection)));
			return;
		}

		compositeDisposable.add(repository.fetchNearestPlaceInfo(latitude, longitude)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						response -> placeName.postValue(new Resource.Success<>(response.getToponymName())),
						error -> placeName.postValue(new Resource.Error<>("Couldn't get the name of the area"))
				)
		);
	}

	public LiveData<Resource<List<PlaceInfo>>> searchLocation(String query) {
		MutableLiveData<Resource<List<PlaceInfo>>> liveData = new MutableLiveData<>();

		liveData.postValue(new Resource.Loading<>());

		if (!hasInternetConnection()) {
			liveData.postValue(new Resource.Error<>(getApplication().getResources().getString(R.string.no_internet_connection)));

		} else {
			compositeDisposable.add(repository.searchLocation(query)
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(
							response -> liveData.postValue(new Resource.Success<>(response)),
							error -> liveData.postValue(new Resource.Error<>("Couldn't get the name of the area"))
					)
			);
		}

		return liveData;
	}

	public boolean hasInternetConnection() {
		ConnectivityManager connectivityManager =
				(ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);

		Network activeNetwork = connectivityManager.getActiveNetwork();
		if (activeNetwork == null) {
			return false;
		}

		NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
		if (capabilities == null) {
			return false;
		}

		return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
				capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
				capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
	}


	public void changeTemperatureUnit(TemperatureUnitProto unit) {
		Resource<DaysForecastProto> resource = daysForecast.getValue();

		if (resource != null) {
			DaysForecastProto days = resource.getData();

			if (days != null && days.dayForecast != null && days.weekTempForecast != null) {
				DaysForecastProto.Builder daysBuilder = days.newBuilder();
				DayForecastProto.Builder dayBuilder = days.dayForecast.newBuilder();

				TemperatureUnitProto currUnit = dayBuilder.temperature.temperatureUnit;

				dayBuilder.temperature(
						dayBuilder.temperature.newBuilder().temperature(
								Math.round(UnitConverter.convertTemperature(dayBuilder.temperature.temperature, currUnit, unit))
						).temperatureUnit(unit).build()
				);
				dayBuilder.apparentTemp(
						dayBuilder.apparentTemp.newBuilder().temperature(
								Math.round(UnitConverter.convertTemperature(dayBuilder.apparentTemp.temperature, currUnit, unit))
						).temperatureUnit(unit).build()
				);
				dayBuilder.hourlyTempForecast(convertTemperatureList(dayBuilder.hourlyTempForecast, unit));

				daysBuilder.weekTempForecast(convertTemperatureList(daysBuilder.weekTempForecast, unit));
				daysBuilder.dayForecast(dayBuilder.build());

				daysForecast.postValue(new Resource.Success<>(daysBuilder.build()));
			}
		}
	}

	private List<TemperatureProto> convertTemperatureList(List<TemperatureProto> temperatureList,
	                                                      TemperatureUnitProto unit) {
		if (temperatureList == null || temperatureList.isEmpty()) {
			return null;
		}

		List<TemperatureProto> convertedList = new ArrayList<>();

		for (TemperatureProto temp : temperatureList) {
			int newTemperature = Math.round(UnitConverter.convertTemperature(temp.temperature, temp.temperatureUnit, unit));

			convertedList.add(temp.newBuilder().temperature(newTemperature).temperatureUnit(unit).build());
		}

		return convertedList;
	}

	public void changeSpeedUnit(SpeedUnitProto unit) {
		Resource<DaysForecastProto> resource = daysForecast.getValue();

		if (resource != null) {
			DaysForecastProto days = resource.getData();

			if (days != null && days.dayForecast != null && days.weekTempForecast != null) {
				DaysForecastProto.Builder daysBuilder = days.newBuilder();
				DayForecastProto.Builder dayBuilder = days.dayForecast.newBuilder();

				SpeedUnitProto currUnit = dayBuilder.windSpeed.speedUnit;

				dayBuilder.windSpeed(
						dayBuilder.windSpeed.newBuilder().speed(
								Math.round(UnitConverter.convertSpeed(dayBuilder.windSpeed.speed, currUnit, unit))
						).speedUnit(unit).build()
				);

				daysBuilder.dayForecast(dayBuilder.build());

				daysForecast.postValue(new Resource.Success<>(daysBuilder.build()));
			}
		}
	}

	public void changePressureUnit(PressureUnitProto unit) {
		Resource<DaysForecastProto> resource = daysForecast.getValue();

		if (resource != null) {
			DaysForecastProto days = resource.getData();

			if (days != null && days.dayForecast != null && days.weekTempForecast != null) {
				DaysForecastProto.Builder daysBuilder = days.newBuilder();
				DayForecastProto.Builder dayBuilder = days.dayForecast.newBuilder();

				// always in open-meteo api
				PressureUnitProto currUnit = PressureUnitProto.HPA;

				dayBuilder.pressure(
						dayBuilder.pressure.newBuilder().pressure(
								Math.round(UnitConverter.convertPressure(dayBuilder.pressure.pressure, currUnit, unit))
						).pressureUnit(unit).build()
				);

				daysBuilder.dayForecast(dayBuilder.build());

				daysForecast.postValue(new Resource.Success<>(daysBuilder.build()));
			}
		}
	}
}
