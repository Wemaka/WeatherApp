package com.wemaka.weatherapp;

import java.util.List;

public class DayForecast {
	private final String windSpeed;
	private final String imgPrecipitationChance;
	private final String precipitationChance;
	private final String pressure;
	private final String uvIndex;
	private final List<DaysForecastResponse> hourlyForecast;

	public DayForecast(String windSpeed, String imgPrecipitationChance, String precipitationChance, String pressure, String uvIndex, List<DaysForecastResponse> hourlyForecast) {
		this.windSpeed = windSpeed;
		this.imgPrecipitationChance = imgPrecipitationChance;
		this.precipitationChance = precipitationChance;
		this.pressure = pressure;
		this.uvIndex = uvIndex;
		this.hourlyForecast = hourlyForecast;
	}

	public String getWindSpeed() {
		return windSpeed;
	}

	public String getImgPrecipitationChance() {
		return imgPrecipitationChance;
	}

	public String getPrecipitationChance() {
		return precipitationChance;
	}

	public String getPressure() {
		return pressure;
	}

	public String getUvIndex() {
		return uvIndex;
	}

	public List<DaysForecastResponse> getHourlyForecast() {
		return hourlyForecast;
	}
}
