package com.wemaka.weatherapp;

import androidx.datastore.rxjava3.RxDataStoreBuilder;

import com.wemaka.weatherapp.data.store.DataStoreSerializer;
import com.wemaka.weatherapp.data.repository.ProtoDataStoreRepository;
import com.zeugmasolutions.localehelper.LocaleAwareApplication;

public class App extends LocaleAwareApplication {

	@Override
	public void onCreate() {
		super.onCreate();

		ProtoDataStoreRepository dataStoreRepository = ProtoDataStoreRepository.getInstance();

		if (dataStoreRepository.getDataStore() == null) {
			dataStoreRepository.setDataStore(
					new RxDataStoreBuilder<>(this, "settings.pb", new DataStoreSerializer()).build());
		}
	}
}
