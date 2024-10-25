package com.wemaka.weatherapp;

import android.app.Application;
import android.content.Context;

import androidx.datastore.rxjava3.RxDataStoreBuilder;

import com.wemaka.weatherapp.data.store.DataStoreSerializer;
import com.wemaka.weatherapp.data.store.ProtoDataStoreRepository;
import com.zeugmasolutions.localehelper.LocaleHelper;

import java.util.Locale;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		ProtoDataStoreRepository dataStoreRepository = ProtoDataStoreRepository.getInstance();

		if (dataStoreRepository.getDataStore() == null) {
			dataStoreRepository.setDataStore(
					new RxDataStoreBuilder<>(this, "settings.pb", new DataStoreSerializer()).build());
		}
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.INSTANCE.onAttach(base));
	}
}
