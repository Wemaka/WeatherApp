package com.wemaka.weatherapp.api;

import java.util.Optional;

public enum WeatherCode {
	CLEAR_SKY("Clear sky", 0),
	MAINLY_CLEAR("Mainly clear", 1),
	PARTLY_CLOUDY("Partly cloudy", 2),
	Overcast("Overcast", 3),
	Fog("Fog", 45),
	DEPOSITING_RIME_FOG("Depositing rime fog", 48),
	DRIZZLE_LIGHT("Drizzle light", 51),
	DRIZZLE_MODERATE("Drizzle moderate", 53),
	DRIZZLE_DENSE_INTENSITY("Drizzle dense intensity", 55),
	FREEZING_DRIZZLE_LIGHT("Freezing drizzle light ", 56),
	FREEZING_DRIZZLE_DENSE_INTENSITY("Freezing drizzle dense intensity", 57),
	RAIN_SLIGHT("Rain slight", 61),
	RAIN_MODERATE("Rain moderate", 63),
	RAIN_HEAVY_INTENSITY("Rain heavy intensity", 65),
	FREEZING_RAIN_LIGHT("Freezing Rain light", 66),
	FREEZING_RAIN_HEAVY_INTENSITY("Freezing Rain heavy intensity", 67),
	SNOW_FALL_SLIGHT("Snow fall slight", 71),
	SNOW_FALL_MODERATE("Snow fall moderate", 73),
	SNOW_FALL_HEAVY_INTENSITY("Snow fall heavy intensity", 75),
	SNOW_GRAINS("Snow grains", 77),
	RAIN_SHOWERS_SLIGHT("Rain showers slight", 80),
	RAIN_SHOWERS_MODERATE("Rain showers moderate", 81),
	RAIN_SHOWERS_VIOLENT("Rain showers violent", 82),
	SNOW_SHOWERS_SLIGHT("Snow showers slight", 85),
	SNOW_SHOWERS_HEAVY("Snow showers heavy", 86),
	THUNDERSTORM_SLIGHT("Thunderstorm slight", 95),
	THUNDERSTORM_WITH_HAIL("Thunderstorm with hail", 96),
	THUNDERSTORM_WITH_HAIL2("Thunderstorm with hail", 99),
	;

	String name;
	int code;

	WeatherCode(String name, int code) {
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public int getCode() {
		return code;
	}

	public static Optional<String> getNameByCode(int code) {
		for (WeatherCode weatherCode : WeatherCode.values()) {
			if (weatherCode.code == code) {
				return Optional.of(weatherCode.name);
			}
		}

		return Optional.empty();
	}
}
