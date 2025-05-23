package com.wemaka.weatherapp.data.api;

import com.wemaka.weatherapp.store.proto.DaysForecastProto;
import com.wemaka.weatherapp.store.proto.PressureUnitProto;
import com.wemaka.weatherapp.store.proto.SpeedUnitProto;
import com.wemaka.weatherapp.store.proto.TemperatureUnitProto;

import io.reactivex.rxjava3.core.Single;

public interface WeatherClient {
	Single<DaysForecastProto> fetchWeatherForecast(double latitude, double longitude);

	TemperatureUnitProto getTemperatureUnit();

	SpeedUnitProto getSpeedUnit();

	PressureUnitProto getPressureUnit();

	void setTemperatureUnit(TemperatureUnitProto unit);

	void setSpeedUnit(SpeedUnitProto unit);

	void setPressureUnit(PressureUnitProto unit);
}
