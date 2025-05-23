package com.wemaka.weatherapp.data.repository;

import androidx.datastore.rxjava3.RxDataStore;

import com.wemaka.weatherapp.store.proto.DataStoreProto;
import com.wemaka.weatherapp.store.proto.DaysForecastProto;
import com.wemaka.weatherapp.store.proto.LocationCoordProto;
import com.wemaka.weatherapp.store.proto.SettingsProto;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.Getter;
import lombok.Setter;

public class ProtoDataStoreRepository {
	public static final String TAG = "ProtoDataStoreRepository";
	@NotNull
	public static final ProtoDataStoreRepository INSTANCE;
	@Nullable
	@Setter
	@Getter
	private RxDataStore<DataStoreProto> dataStore;

	static {
		INSTANCE = new ProtoDataStoreRepository();
	}

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
		return dataStore.data()
				.filter(Objects::nonNull)
				.map(data -> data)
				.firstOrError()
				.onErrorComplete();
	}

	public Completable saveSettings(SettingsProto settings) {
		return dataStore.updateDataAsync(data -> Single.just(
				data.newBuilder().settings(settings).build()
		)).ignoreElement();
	}

	public Maybe<SettingsProto> getSettings() {
		return dataStore.data()
				.map(data -> data.settings)
				.firstOrError()
				.onErrorComplete();
	}

	public Completable saveDaysForecastResponse(DaysForecastProto daysForecastResponse) {
		return dataStore.updateDataAsync(data -> Single.just(
				data.newBuilder().forecast(daysForecastResponse).build()
		)).ignoreElement();
	}

	public Maybe<DaysForecastProto> getDaysForecastResponse() {
		return dataStore.data()
				.map(data -> data.forecast)
				.firstOrError()
				.onErrorComplete();
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
