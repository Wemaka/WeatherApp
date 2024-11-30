package com.wemaka.weatherapp.util.math;

import android.content.Context;
import android.util.TypedValue;

import com.wemaka.weatherapp.store.proto.TemperatureUnitProto;

import org.jetbrains.annotations.NotNull;

public class UnitConverter {
	public static int dpToPx(@NotNull Context context, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}

	public static float convertTemperature(float temperature, TemperatureUnitProto fromUnit,
	                                       TemperatureUnitProto toUnit) {
		if (fromUnit == toUnit) {
			return temperature;
		}

		if (fromUnit == TemperatureUnitProto.FAHRENHEIT && toUnit == TemperatureUnitProto.CELSIUS) {
			return fahrenheitToCelsius(temperature);
		}

		if (fromUnit == TemperatureUnitProto.CELSIUS && toUnit == TemperatureUnitProto.FAHRENHEIT) {
			return celsiusToFahrenheit(temperature);
		}

		throw new IllegalArgumentException("Unsupported temperature conversion");
	}

	public static float fahrenheitToCelsius(float fahrenheitTemp) {
		return (fahrenheitTemp - 32) * 5 / 9;
	}

	public static float celsiusToFahrenheit(float celsiusTemp) {
		return celsiusTemp * 9 / 5 + 32;
	}
}
