package com.wemaka.weatherapp.ui.viewmodel;


import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.data.model.PlaceInfo;
import com.wemaka.weatherapp.data.repository.WeatherForecastRepository;
import com.wemaka.weatherapp.network.NetworkChecker;
import com.wemaka.weatherapp.store.proto.DataStoreProto;
import com.wemaka.weatherapp.store.proto.DayForecastProto;
import com.wemaka.weatherapp.store.proto.DaysForecastProto;
import com.wemaka.weatherapp.store.proto.LocationCoordProto;
import com.wemaka.weatherapp.store.proto.PressureUnitProto;
import com.wemaka.weatherapp.store.proto.SettingsProto;
import com.wemaka.weatherapp.store.proto.SpeedUnitProto;
import com.wemaka.weatherapp.store.proto.TemperatureProto;
import com.wemaka.weatherapp.store.proto.TemperatureUnitProto;
import com.wemaka.weatherapp.ui.fragment.SettingsFragment;
import com.wemaka.weatherapp.util.Resource;
import com.wemaka.weatherapp.util.UnitConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.Getter;

@HiltViewModel
public class MainViewModel extends AndroidViewModel {
	public static final String TAG = "MainViewModel";
	private final WeatherForecastRepository repository;
	private final CompositeDisposable compositeDisposable = new CompositeDisposable();
	private final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplication());
	@Getter
	private final MutableLiveData<Resource<DaysForecastProto>> daysForecast = new MutableLiveData<>();
	@Getter
	private final MutableLiveData<Resource<String>> placeName = new MutableLiveData<>();
	private final NetworkChecker networkChecker;

	@Inject
	public MainViewModel(@NonNull WeatherForecastRepository repository, @NonNull Application app, @NonNull NetworkChecker networkChecker) {
		super(app);
		this.repository = repository;
		this.networkChecker = networkChecker;
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
				.doAfterTerminate(this::fetchCurrentWeatherAndPlace)
				.subscribe(
						settings -> {
							placeName.postValue(new Resource.Success<>(settings.locationName));
							setLocation(settings.locationCoord);
						},
						throwable -> {
							Log.e(TAG, "init getSettings error", throwable);
							setLocation(getLocation());
						},
						() -> {
							setLocation(getLocation());
						}
				)
		);

		repository.setTemperatureUnit(
				TemperatureUnitProto.valueOf(sp.getString(SettingsFragment.PREF_KEY_TEMPERATURE, "celsius").toUpperCase()));
		repository.setSpeedUnit(
				SpeedUnitProto.valueOf(sp.getString(SettingsFragment.PREF_KEY_WIND_SPEED, "kmh").toUpperCase()));
		repository.setPressureUnit(
				PressureUnitProto.valueOf(sp.getString(SettingsFragment.PREF_KEY_AIR_PRESSURE, "hpa").toUpperCase()));
	}

	@NonNull
	public LocationCoordProto getLocation() {
		return repository.getLocation();
	}

	public void setLocation(@NonNull LocationCoordProto location) {
		repository.setLocation(location);
	}

	public void saveDataStore(@NonNull DataStoreProto dataStoreProto) {
		compositeDisposable.add(repository.saveDataStore(dataStoreProto)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						() -> Log.d(TAG, "SAVE FORECAST DATASTORE: datastore"),
						error -> Log.e(TAG, "Error saving data", error)
				)
		);
	}

	@NonNull
	public LiveData<LocationCoordProto> getSavedLocationCoord() {
		MutableLiveData<LocationCoordProto> liveData = new MutableLiveData<>();

		compositeDisposable.add(repository.getSettings()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						settings -> {
							liveData.postValue(settings.locationCoord);
						},
						throwable -> {
							Log.e(TAG, "getSavedLocationCoord", throwable);
							liveData.postValue(getLocation());
						},
						() -> {
							Log.d(TAG, "getSavedLocationCoord Complete");
							liveData.postValue(getLocation());
						}
				)
		);

		return liveData;
	}

	@NonNull
	public LiveData<SettingsProto> getSavedSettings() {
		MutableLiveData<SettingsProto> liveData = new MutableLiveData<>();

		compositeDisposable.add(repository.getSettings()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(liveData::postValue)
		);

		return liveData;
	}

	@NonNull
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
		compositeDisposable.add(repository.requestLocation()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						this::fetchWeatherAndPlace,
						error -> Log.e(TAG, error.getMessage(), error)
				)
		);
	}

	public void fetchWeatherAndPlace(@NonNull LocationCoordProto location) {
		safeFetchWeatherForecast(location.latitude, location.longitude);
		safeFetchNearestPlaceInfo(location.latitude, location.longitude);
	}

	public void fetchNearestPlaceInfo(Locale locale) {
		repository.setLocale(locale);
		LocationCoordProto loc = getLocation();
		safeFetchNearestPlaceInfo(loc.latitude, loc.longitude);
	}

	@NonNull
	public LiveData<Resource<List<PlaceInfo>>> searchLocation(@NonNull String query) {
		MutableLiveData<Resource<List<PlaceInfo>>> liveData = new MutableLiveData<>();

		liveData.postValue(new Resource.Loading<>());

		if (!networkChecker.hasInternetConnection()) {
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

	public void changeTemperatureUnit(@NonNull TemperatureUnitProto unit) {
		repository.setTemperatureUnit(unit);

		updateUnit((daysBuilder, dayBuilder) -> {
					dayBuilder.temperature(UnitConverter.updateTemperature(
							dayBuilder.temperature,
							dayBuilder.temperature.temperatureUnit,
							unit));

					dayBuilder.apparentTemp(UnitConverter.updateTemperature(
							dayBuilder.apparentTemp,
							dayBuilder.apparentTemp.temperatureUnit,
							unit));

					dayBuilder.nightTemp(UnitConverter.updateTemperature(
							dayBuilder.nightTemp,
							dayBuilder.nightTemp.temperatureUnit,
							unit));

					dayBuilder.hourlyTempForecast(convertTemperatureList(dayBuilder.hourlyTempForecast, unit));
					daysBuilder.weekTempForecast(convertTemperatureList(daysBuilder.weekTempForecast, unit));
				}
		);
	}

	public void changeSpeedUnit(@NonNull SpeedUnitProto unit) {
		repository.setSpeedUnit(unit);

		updateUnit((daysBuilder, dayBuilder) -> dayBuilder.windSpeed(UnitConverter.updateWindSpeed(
				dayBuilder.windSpeed,
				dayBuilder.windSpeed.speedUnit,
				unit))
		);
	}

	public void changePressureUnit(@NonNull PressureUnitProto unit) {
		repository.setPressureUnit(unit);

		updateUnit((daysBuilder, dayBuilder) -> dayBuilder.pressure(UnitConverter.updatePressure(
				dayBuilder.pressure,
				dayBuilder.pressure.pressureUnit,
				unit))
		);
	}

	private void safeFetchWeatherForecast(double latitude, double longitude) {
		daysForecast.postValue(new Resource.Loading<>());

		if (!networkChecker.hasInternetConnection()) {
			daysForecast.postValue(new Resource.Error<>(getApplication().getResources().getString(R.string.no_internet_connection)));
			return;
		}

		compositeDisposable.add(repository.fetchWeatherForecast(latitude, longitude)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						forecast -> daysForecast.postValue(new Resource.Success<>(formatDaysForecast(forecast))),
						error -> daysForecast.postValue(new Resource.Error<>("Couldn't get the weather forecast"))
				)
		);
	}

	@Nullable
	private DaysForecastProto formatDaysForecast(@Nullable DaysForecastProto daysForecastProto) {
		if (daysForecastProto != null && daysForecastProto.dayForecast != null) {
			DaysForecastProto.Builder daysBuilder = daysForecastProto.newBuilder();
			DayForecastProto.Builder dayBuilder = daysForecastProto.dayForecast.newBuilder();

			// always in open-meteo api
			PressureUnitProto currUnit = dayBuilder.pressure.pressureUnit;

			dayBuilder.pressure(
					dayBuilder.pressure.newBuilder().pressure(
							Math.round(UnitConverter.convertPressure(dayBuilder.pressure.pressure, currUnit,
									repository.getPressureUnit()))
					).build()
			);

			return daysBuilder.dayForecast(dayBuilder.build()).build();
		}


		return daysForecastProto;
	}

	private void safeFetchNearestPlaceInfo(double latitude, double longitude) {
		placeName.postValue(new Resource.Loading<>());

		if (!networkChecker.hasInternetConnection()) {
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

	private void updateUnit(BiConsumer<DaysForecastProto.Builder, DayForecastProto.Builder> dayUpdater) {
		Resource<DaysForecastProto> resource = daysForecast.getValue();
		if (resource == null) {
			return;
		}

		DaysForecastProto days = resource.getData();
		if (days == null || days.dayForecast == null || days.weekTempForecast == null) {
			return;
		}

		DaysForecastProto.Builder daysBuilder = days.newBuilder();
		DayForecastProto.Builder dayBuilder = days.dayForecast.newBuilder();

		dayUpdater.accept(daysBuilder, dayBuilder);

		daysBuilder.dayForecast(dayBuilder.build());
		daysForecast.postValue(new Resource.Success<>(daysBuilder.build()));
	}

	@NonNull
	private List<TemperatureProto> convertTemperatureList(@NonNull List<TemperatureProto> temperatureList, @NonNull TemperatureUnitProto unit) {
		List<TemperatureProto> convertedList = new ArrayList<>();

		for (TemperatureProto temp : temperatureList) {
			int newTemperature = Math.round(UnitConverter.convertTemperature(temp.temperature, temp.temperatureUnit, unit));

			convertedList.add(temp.newBuilder().temperature(newTemperature).temperatureUnit(unit).build());
		}

		return convertedList;
	}
}
