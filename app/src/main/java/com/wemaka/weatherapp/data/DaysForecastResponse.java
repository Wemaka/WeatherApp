package com.wemaka.weatherapp.data;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DaysForecastResponse {
	private final DayForecast todayForecast;
	private final List<Float> weekTempForecast;
}
