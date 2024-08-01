package com.wemaka.weatherapp.data;

public class Temperature {
	private final String time;
	private final String temperature;
	private final String imgWeatherCode;

	public Temperature(String time, String temperature, String imgWeatherCode) {
		this.time = time;
		this.temperature = temperature;
		this.imgWeatherCode = imgWeatherCode;
	}

	public String getTime() {
		return time;
	}

	public String getTemperature() {
		return temperature;
	}

	public String getImgWeatherCode() {
		return imgWeatherCode;
	}
}
