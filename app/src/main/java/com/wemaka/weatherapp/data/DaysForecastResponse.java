package com.wemaka.weatherapp.data;

import java.util.List;

public class DaysForecastResponse {
	private final DayForecast todayForecast;
	private final List<Float> weekTempForecast;

	public DaysForecastResponse(DayForecast todayForecast, List<Float> weekTempForecast) {
		this.todayForecast = todayForecast;
		this.weekTempForecast = weekTempForecast;
	}

	public DayForecast getTodayForecast() {
		return todayForecast;
	}

	public List<Float> getWeekTempForecast() {
		return weekTempForecast;
	}
}
