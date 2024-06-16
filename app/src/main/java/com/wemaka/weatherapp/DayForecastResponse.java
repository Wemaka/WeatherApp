package com.wemaka.weatherapp;

import java.util.List;

public class DayForecastResponse {
	private final String locationName;
	private final String currentTemp;
	private final String apparentTemp;
	private final String imgWeatherCode;
	private final String weatherCode;
	private final String date;
	private final String windSpeed;
	private final String imgPrecipitationChance;
	private final String precipitationChance;
	private final String pressure;
	private final String uvIndex;
	private final List<DayForecastResponse> hourlyForecast;
	private final String dayForecast;
	private final String precipitationChanceForecast;
	private final String sunrise;
	private final String sunset;

	public DayForecastResponse(String locationName, String currentTemp, String apparentTemp, String imgWeatherCode, String weatherCode, String date, String windSpeed, String imgPrecipitationChance, String precipitationChance, String pressure, String uvIndex, List<DayForecastResponse> hourlyForecast, String dayForecast, String precipitationChanceForecast, String sunrise, String sunset) {
		this.locationName = locationName;
		this.currentTemp = currentTemp;
		this.apparentTemp = apparentTemp;
		this.imgWeatherCode = imgWeatherCode;
		this.weatherCode = weatherCode;
		this.date = date;
		this.windSpeed = windSpeed;
		this.imgPrecipitationChance = imgPrecipitationChance;
		this.precipitationChance = precipitationChance;
		this.pressure = pressure;
		this.uvIndex = uvIndex;
		this.hourlyForecast = hourlyForecast;
		this.dayForecast = dayForecast;
		this.precipitationChanceForecast = precipitationChanceForecast;
		this.sunrise = sunrise;
		this.sunset = sunset;
	}

	public String getLocationName() {
		return locationName;
	}

	public String getCurrentTemp() {
		return currentTemp;
	}

	public String getApparentTemp() {
		return apparentTemp;
	}

	public String getImgWeatherCode() {
		return imgWeatherCode;
	}

	public String getWeatherCode() {
		return weatherCode;
	}

	public String getDate() {
		return date;
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

	public List<DayForecastResponse> getHourlyForecast() {
		return hourlyForecast;
	}

	public String getDayForecast() {
		return dayForecast;
	}

	public String getPrecipitationChanceForecast() {
		return precipitationChanceForecast;
	}

	public String getSunrise() {
		return sunrise;
	}

	public String getSunset() {
		return sunset;
	}
}
