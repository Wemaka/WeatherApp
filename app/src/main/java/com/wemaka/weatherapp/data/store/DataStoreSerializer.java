package com.wemaka.weatherapp.data.store;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.datastore.core.Serializer;

import com.wemaka.weatherapp.store.proto.DataStoreProto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import kotlin.Unit;
import kotlin.coroutines.Continuation;

public class DataStoreSerializer implements Serializer<DataStoreProto> {
	@Override
	public DataStoreProto getDefaultValue() {
		return new DataStoreProto(null, null);
	}

	@Nullable
	@Override
	public DataStoreProto readFrom(@NonNull InputStream inputStream, @NonNull Continuation<? super DataStoreProto> continuation) {
		try {
			return DataStoreProto.ADAPTER.decode(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	@Override
	public DataStoreProto writeTo(DataStoreProto dataStoreProto, @NonNull OutputStream outputStream, @NonNull Continuation<? super Unit> continuation) {
		try {
			dataStoreProto.encode(outputStream);
			return dataStoreProto;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

