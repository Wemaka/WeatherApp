package com.wemaka.weatherapp.data.store;

import static com.wemaka.weatherapp.activity.MainActivity.TAG;

import android.util.Log;

import androidx.datastore.rxjava3.RxDataStore;

import com.wemaka.weatherapp.store.proto.DataStoreProto;
import com.wemaka.weatherapp.store.proto.DaysForecastResponseProto;
import com.wemaka.weatherapp.store.proto.LocationCoordProto;
import com.wemaka.weatherapp.store.proto.SettingsProto;

import java.util.Objects;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.Getter;
import lombok.Setter;

public class ProtoDataStoreRepository {
	public static final String TAG = "ProtoDataStoreRepository";
	@Getter
	private static final ProtoDataStoreRepository instance = new ProtoDataStoreRepository();
	@Setter
	@Getter
	private RxDataStore<DataStoreProto> dataStore;

	private ProtoDataStoreRepository() {
	}

	public Completable saveDataStore(DataStoreProto dataStoreProto) {
		return dataStore.updateDataAsync(data -> Single.just(
				data.newBuilder()
						.settings(dataStoreProto.settings)
						.forecast(dataStoreProto.forecast)
						.build()
		)).ignoreElement();
	}

	public Maybe<DataStoreProto> getDataStoreProto() {
		return dataStore.data().filter(Objects::nonNull).map(data -> data).firstElement();
	}

	public Completable saveSettings(SettingsProto settings) {
		return dataStore.updateDataAsync(data -> Single.just(
				data.newBuilder().settings(settings).build()
		)).ignoreElement();
	}

	public Maybe<SettingsProto> getSettings() {
		return dataStore.data()
				.doOnNext(data -> Log.i(TAG, "Raw data: " + data))
				.filter(data -> data.settings != null).map(data -> data.settings)
				.doOnNext(settings -> Log.i(TAG, "Filtered settings: " + settings))
				.firstElement();
	}

	public Completable saveDaysForecastResponse(DaysForecastResponseProto daysForecastResponse) {
		return dataStore.updateDataAsync(data -> Single.just(
				data.newBuilder().forecast(daysForecastResponse).build()
		)).ignoreElement();
	}

	public Maybe<DaysForecastResponseProto> getDaysForecastResponse() {
		return dataStore.data().filter(data -> data.forecast != null).map(data -> data.forecast).firstElement();
	}

	public Completable saveLocationCoord(LocationCoordProto coord) {
		return dataStore.updateDataAsync(data -> Single.just(
				data.newBuilder().settings(
						data.settings.newBuilder().locationCoord(coord).build()
				).build()
		)).ignoreElement();
	}

	public Flowable<LocationCoordProto> getFlowLocationCoord() {
		return dataStore.data().filter(data -> data.settings != null && data.settings.locationCoord != null)
				.map(data -> {
					if (data.settings == null) {
						return new LocationCoordProto(0.0, 0.0);
					}

					return data.settings.locationCoord;
				}).distinctUntilChanged((prev, current) ->
						prev.latitude == current.latitude && prev.longitude == current.longitude);
	}
}
