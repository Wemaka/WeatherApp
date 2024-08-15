package com.wemaka.weatherapp.api;

import com.wemaka.weatherapp.R;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WeatherCode {
	CLEAR_SKY("Clear Sky", 0, R.drawable.ic_clear_day, R.drawable.ic_clear_night),
	MAINLY_CLEAR("Mainly Clear", 1, R.drawable.ic_cloudy_2_day, R.drawable.ic_cloudy_2_night),
	PARTLY_CLOUDY("Partly Cloudy", 2, R.drawable.ic_cloudy_2_day, R.drawable.ic_cloudy_2_night),
	OVERCAST("Overcast", 3, R.drawable.ic_cloudy, R.drawable.ic_cloudy),
	FOG("Fog", 45, R.drawable.ic_fog, R.drawable.ic_fog),
	ICE_FOG("Ice Fog", 48, R.drawable.ic_ice_fog, R.drawable.ic_ice_fog),
	LIGHT_DRIZZLE("Light Drizzle", 51, R.drawable.ic_drizzle_1_day, R.drawable.ic_drizzle_1_night),
	DRIZZLE("Drizzle", 53, R.drawable.ic_drizzle_2_day, R.drawable.ic_drizzle_2_night),
	HEAVY_DRIZZLE("Heavy Drizzle", 55, R.drawable.ic_drizzle_3_day, R.drawable.ic_drizzle_3_night),
	LIGHT_FREEZING_DRIZZLE("Light Freezing Drizzle", 56, R.drawable.ic_drizzle_and_snow_1_day,
			R.drawable.ic_drizzle_and_snow_1_night),
	FREEZING_DRIZZLE("Freezing Drizzle", 57, R.drawable.ic_drizzle_and_snow_3_day,
			R.drawable.ic_drizzle_and_snow_3_night),
	LIGHT_RAIN("Light Rain", 61, R.drawable.ic_rainy_1_day, R.drawable.ic_rainy_1_night),
	RAIN("Rain", 63, R.drawable.ic_rainy_2_day, R.drawable.ic_rainy_2_night),
	HEAVY_RAIN("Heavy Rain", 65, R.drawable.ic_rainy_3, R.drawable.ic_rainy_3),
	LIGHT_FREEZING_RAIN("Light Freezing Rain", 66, R.drawable.ic_rainy_and_snow_1_day,
			R.drawable.ic_rainy_and_snow_1_night),
	FREEZING_RAIN("Freezing Rain", 67, R.drawable.ic_rainy_and_snow_3, R.drawable.ic_rainy_and_snow_3),
	LIGHT_SNOW("Light Snow", 71, R.drawable.ic_snowy_1_day, R.drawable.ic_snowy_1_night),
	SNOW("Snow", 73, R.drawable.ic_snowy_2_day, R.drawable.ic_snowy_2_night),
	HEAVY_SNOW("Heavy Snow", 75, R.drawable.ic_snowy_3, R.drawable.ic_snowy_3),
	SNOW_GRAINS("Snow grains", 77, R.drawable.ic_hail, R.drawable.ic_hail),
	LIGHT_SHOWERS("Light Showers", 80, R.drawable.ic_showers_1, R.drawable.ic_showers_1),
	SHOWERS("Showers", 81, R.drawable.ic_showers_2, R.drawable.ic_showers_2),
	HEAVY_SHOWERS("Heavy Showers", 82, R.drawable.ic_showers_3, R.drawable.ic_showers_3),
	LIGHT_SNOW_SHOWERS("Light Snow Showers", 85, R.drawable.ic_showers_and_snow_1, R.drawable.ic_showers_and_snow_1),
	SNOW_SHOWERS("Snow Showers", 86, R.drawable.ic_showers_and_snow_3,
			R.drawable.ic_showers_and_snow_3),
	THUNDERSTORM("Thunderstorm", 95, R.drawable.ic_thunderstorms, R.drawable.ic_thunderstorms),
	LIGHT_THUNDERSTORM_HAIL("Light T-storm w/ hail", 96, R.drawable.ic_thunderstorms_and_hail, R.drawable.ic_thunderstorms_and_hail),
	THUNDERSTORM_HAIL("T-storm w/ hail", 99, R.drawable.ic_thunderstorms_and_hail_heavy, R.drawable.ic_thunderstorms_and_hail_heavy),
	;

	String name;
	int code;
	int iconDayId;
	int iconNightId;

	public static Optional<String> getNameByCode(int code) {
		for (WeatherCode weatherCode : WeatherCode.values()) {
			if (weatherCode.code == code) {
				return Optional.of(weatherCode.name);
			}
		}

		return Optional.empty();
	}

	public static Optional<Integer> getIconIdByCode(int code, boolean isDay) {
		for (WeatherCode weatherCode : WeatherCode.values()) {
			if (weatherCode.code == code) {
				if (isDay) {
					return Optional.of(weatherCode.iconDayId);
				}
				return Optional.of(weatherCode.iconNightId);
			}
		}

		return Optional.empty();
	}
}
