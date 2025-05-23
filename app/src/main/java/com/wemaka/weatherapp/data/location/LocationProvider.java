package com.wemaka.weatherapp.data.location;

import android.location.LocationManager;

import com.wemaka.weatherapp.store.proto.LocationCoordProto;

import io.reactivex.rxjava3.core.Single;

public interface LocationProvider {
	Single<LocationCoordProto> requestLocation();

	LocationCoordProto getLocation();

	void setLocation(LocationCoordProto location);
}
