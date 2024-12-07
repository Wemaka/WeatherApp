package com.wemaka.weatherapp.util;

import androidx.annotation.NonNull;

import com.wemaka.weatherapp.R;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WeatherCode {
	CLEAR_SKY(0, R.string.clear_sky, R.drawable.ic_clear_day, R.drawable.ic_clear_night),
	MAINLY_CLEAR(1, R.string.mainly_clear, R.drawable.ic_cloudy_2_day, R.drawable.ic_cloudy_2_night),
	PARTLY_CLOUDY(2, R.string.partly_cloudy, R.drawable.ic_cloudy_2_day, R.drawable.ic_cloudy_2_night),
	OVERCAST(3, R.string.overcast, R.drawable.ic_cloudy, R.drawable.ic_cloudy),
	FOG(45, R.string.fog, R.drawable.ic_fog, R.drawable.ic_fog),
	ICE_FOG(48, R.string.ice_fog, R.drawable.ic_ice_fog, R.drawable.ic_ice_fog),
	LIGHT_DRIZZLE(51, R.string.light_drizzle, R.drawable.ic_drizzle_1_day, R.drawable.ic_drizzle_1_night),
	DRIZZLE(53, R.string.drizzle, R.drawable.ic_drizzle_2_day, R.drawable.ic_drizzle_2_night),
	HEAVY_DRIZZLE(55, R.string.heavy_drizzle, R.drawable.ic_drizzle_3_day, R.drawable.ic_drizzle_3_night),
	LIGHT_FREEZING_DRIZZLE(56, R.string.light_freezing_drizzle, R.drawable.ic_drizzle_and_snow_1_day, R.drawable.ic_drizzle_and_snow_1_night),
	FREEZING_DRIZZLE(57, R.string.freezing_drizzle, R.drawable.ic_drizzle_and_snow_3_day, R.drawable.ic_drizzle_and_snow_3_night),
	LIGHT_RAIN(61, R.string.light_rain, R.drawable.ic_rainy_1_day, R.drawable.ic_rainy_1_night),
	RAIN(63, R.string.rain, R.drawable.ic_rainy_2_day, R.drawable.ic_rainy_2_night),
	HEAVY_RAIN(65, R.string.heavy_rain, R.drawable.ic_rainy_3, R.drawable.ic_rainy_3),
	LIGHT_FREEZING_RAIN(66, R.string.light_freezing_rain, R.drawable.ic_rainy_and_snow_1_day, R.drawable.ic_rainy_and_snow_1_night),
	FREEZING_RAIN(67, R.string.freezing_rain, R.drawable.ic_rainy_and_snow_3, R.drawable.ic_rainy_and_snow_3),
	LIGHT_SNOW(71, R.string.light_snow, R.drawable.ic_snowy_1_day, R.drawable.ic_snowy_1_night),
	SNOW(73, R.string.snow, R.drawable.ic_snowy_2_day, R.drawable.ic_snowy_2_night),
	HEAVY_SNOW(75, R.string.heavy_snow, R.drawable.ic_snowy_3, R.drawable.ic_snowy_3),
	SNOW_GRAINS(77, R.string.snow_grains, R.drawable.ic_hail, R.drawable.ic_hail),
	LIGHT_SHOWERS(80, R.string.light_showers, R.drawable.ic_showers_1, R.drawable.ic_showers_1),
	SHOWERS(81, R.string.showers, R.drawable.ic_showers_2, R.drawable.ic_showers_2),
	HEAVY_SHOWERS(82, R.string.heavy_showers, R.drawable.ic_showers_3, R.drawable.ic_showers_3),
	LIGHT_SNOW_SHOWERS(85, R.string.light_snow_showers, R.drawable.ic_showers_and_snow_1, R.drawable.ic_showers_and_snow_1),
	SNOW_SHOWERS(86, R.string.snow_showers, R.drawable.ic_showers_and_snow_3, R.drawable.ic_showers_and_snow_3),
	THUNDERSTORM(95, R.string.thunderstorm, R.drawable.ic_thunderstorms, R.drawable.ic_thunderstorms),
	LIGHT_THUNDERSTORM_HAIL(96, R.string.light_thunderstorm_hail, R.drawable.ic_thunderstorms_and_hail, R.drawable.ic_thunderstorms_and_hail),
	THUNDERSTORM_HAIL(99, R.string.thunderstorm_hail, R.drawable.ic_thunderstorms_and_hail_heavy, R.drawable.ic_thunderstorms_and_hail_heavy),
	;

	private final int code;
	private final int resId;
	private final int iconDayId;
	private final int iconNightId;

	@NonNull
	public static Optional<WeatherCode> searchWeatherCode(int code) {
		WeatherCode[] weatherValues = WeatherCode.values();

		int left = 0;
		int right = weatherValues.length - 1;

		while (left <= right) {
			int mid = left + (right - left) / 2;
			WeatherCode midWeather = weatherValues[mid];

			if (code > midWeather.code) {
				left = mid + 1;
			} else if (code < midWeather.code) {
				right = mid - 1;
			} else {
				return Optional.of(midWeather);
			}
		}

		return Optional.empty();
	}

	@NonNull
	public static Optional<Integer> getResIdByCode(int code) {
		return searchWeatherCode(code).map(w -> w.resId);
	}

	@NonNull
	public static Optional<Integer> getIconIdByCode(int code, boolean isDay) {
		return searchWeatherCode(code).map(w -> isDay ? w.iconDayId : w.iconNightId);
	}
}
