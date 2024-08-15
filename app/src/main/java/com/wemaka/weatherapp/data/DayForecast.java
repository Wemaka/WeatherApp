package com.wemaka.weatherapp.data;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DayForecast {
	private final String locationName;
	private final String temperature;
	private final String apparentTemp;
	private final Integer imgIdWeatherCode;
	private final String weatherCode;
	private final String date;
	private final String sunrise;
	private final String sunset;
	private final WindSpeed windSpeed;
	private final PrecipitationChance precipitationChance;
	private final Pressure pressure;
	private final UvIndex uvIndex;
	private final List<Temperature> hourlyTempForecast;
	private final List<PrecipitationChance> precipitationChanceForecast;
}
