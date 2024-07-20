package com.wemaka.weatherapp;

public class DaysForecastResponse {
	private final String locationName;
	private final String currentTemp;
	private final String apparentTemp;
	private final String imgWeatherCode;
	private final String weatherCode;
	private final String date;
	private final String sunrise;
	private final String sunset;
	private final DayForecast todayForecast;
	private final DayForecast tomorrowForecast;

	public DaysForecastResponse(String locationName, String currentTemp, String apparentTemp, String imgWeatherCode, String weatherCode, String date, String sunrise, String sunset, DayForecast todayForecast, DayForecast tomorrowForecast) {
		this.locationName = locationName;
		this.currentTemp = currentTemp;
		this.apparentTemp = apparentTemp;
		this.imgWeatherCode = imgWeatherCode;
		this.weatherCode = weatherCode;
		this.date = date;
		this.sunrise = sunrise;
		this.sunset = sunset;
		this.todayForecast = todayForecast;
		this.tomorrowForecast = tomorrowForecast;
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

	public String getSunrise() {
		return sunrise;
	}

	public String getSunset() {
		return sunset;
	}

	public DayForecast getTodayForecast() {
		return todayForecast;
	}

	public DayForecast getTomorrowForecast() {
		return tomorrowForecast;
	}
}
