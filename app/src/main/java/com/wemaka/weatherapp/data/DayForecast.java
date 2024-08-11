package com.wemaka.weatherapp.data;

import java.util.List;

public class DayForecast {
	private final String locationName;
	private final String temperature;
	private final String apparentTemp;
	private final String imgWeatherCode;
	private final String weatherCode;
	private final String date;
	private final String sunrise;
	private final String sunset;
	private final WindSpeed windSpeed;
	private final String imgPrecipitationChance;
	private final PrecipitationChance precipitationChance;
	private final Pressure pressure;
	private final UvIndex uvIndex;
	private final List<Temperature> hourlyTempForecast;
	private final List<PrecipitationChance> precipitationChanceForecast;

	public DayForecast(String locationName, String temperature, String apparentTemp, String imgWeatherCode, String weatherCode, String date, String sunrise, String sunset, WindSpeed windSpeed, String imgPrecipitationChance, PrecipitationChance precipitationChance, Pressure pressure, UvIndex uvIndex, List<Temperature> hourlyTempForecast, List<PrecipitationChance> precipitationChanceForecast) {
		this.locationName = locationName;
		this.temperature = temperature;
		this.apparentTemp = apparentTemp;
		this.imgWeatherCode = imgWeatherCode;
		this.weatherCode = weatherCode;
		this.date = date;
		this.sunrise = sunrise;
		this.sunset = sunset;
		this.windSpeed = windSpeed;
		this.imgPrecipitationChance = imgPrecipitationChance;
		this.precipitationChance = precipitationChance;
		this.pressure = pressure;
		this.uvIndex = uvIndex;
		this.hourlyTempForecast = hourlyTempForecast;
		this.precipitationChanceForecast = precipitationChanceForecast;
	}

	public String getLocationName() {
		return locationName;
	}

	public String getTemperature() {
		return temperature;
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

	public WindSpeed getWindSpeed() {
		return windSpeed;
	}

	public String getImgPrecipitationChance() {
		return imgPrecipitationChance;
	}

	public PrecipitationChance getPrecipitationChance() {
		return precipitationChance;
	}

	public Pressure getPressure() {
		return pressure;
	}

	public UvIndex getUvIndex() {
		return uvIndex;
	}

	public List<Temperature> getHourlyTempForecast() {
		return hourlyTempForecast;
	}

	public List<PrecipitationChance> getPrecipitationChanceForecast() {
		return precipitationChanceForecast;
	}
}