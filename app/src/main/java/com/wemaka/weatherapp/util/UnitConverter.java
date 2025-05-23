package com.wemaka.weatherapp.util;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.NonNull;

import com.wemaka.weatherapp.store.proto.PressureProto;
import com.wemaka.weatherapp.store.proto.PressureUnitProto;
import com.wemaka.weatherapp.store.proto.SpeedUnitProto;
import com.wemaka.weatherapp.store.proto.TemperatureProto;
import com.wemaka.weatherapp.store.proto.TemperatureUnitProto;
import com.wemaka.weatherapp.store.proto.WindSpeedProto;

import org.jetbrains.annotations.NotNull;

public class UnitConverter {
	public static final String TAG = "UnitConverter";

	public static int dpToPx(@NotNull Context context, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}

	public static float convertTemperature(float temperature, @NonNull TemperatureUnitProto fromUnit, @NonNull TemperatureUnitProto toUnit) {
		float tempCelsius;

		switch (fromUnit) {
			case FAHRENHEIT:
				tempCelsius = (temperature - 32) * 5 / 9;
				break;
			default:
				tempCelsius = temperature;
		}

		switch (toUnit) {
			case FAHRENHEIT:
				return tempCelsius * 9 / 5 + 32;
			default:
				return tempCelsius;
		}
	}

	public static float convertSpeed(float speed, @NonNull SpeedUnitProto fromUnit, @NonNull SpeedUnitProto toUnit) {
		float tempMetersPerSecond;

		switch (fromUnit) {
			case KMH:
				tempMetersPerSecond = (float) (speed / 3.6);
				break;
			case MPH:
				tempMetersPerSecond = (float) (speed * 0.446944);
				break;
			default:
				tempMetersPerSecond = speed;
		}

		switch (toUnit) {
			case KMH:
				return (float) (tempMetersPerSecond * 3.6);
			case MPH:
				return (float) (tempMetersPerSecond / 0.446944);
			default:
				return tempMetersPerSecond;
		}
	}

	public static float convertPressure(float pressure, @NonNull PressureUnitProto fromUnit, @NonNull PressureUnitProto toUnit) {
		float tempHpa;

		switch (fromUnit) {
			case MMHG:
				tempHpa = (float) (pressure * 1.33322);
				break;
			case INHG:
				tempHpa = (float) (pressure * 33.8638);
				break;
			default:
				tempHpa = pressure;
		}

		switch (toUnit) {
			case MMHG:
				return (float) (tempHpa / 1.33322);
			case INHG:
				return (float) (tempHpa / 33.8638);
			default:
				return tempHpa;
		}
	}

	public static TemperatureProto updateTemperature(TemperatureProto temperature, TemperatureUnitProto from,
	                                                 TemperatureUnitProto to) {
		return temperature.newBuilder()
				.temperature(Math.round(convertTemperature(temperature.temperature, from, to)))
				.temperatureUnit(to)
				.build();
	}

	public static WindSpeedProto updateWindSpeed(WindSpeedProto windSpeed, SpeedUnitProto from, SpeedUnitProto to) {
		return windSpeed.newBuilder()
				.speed(Math.round(convertSpeed(windSpeed.speed, from, to)))
				.speedUnit(to)
				.build();
	}

	public static PressureProto updatePressure(PressureProto pressure, PressureUnitProto from, PressureUnitProto to) {
		return pressure.newBuilder()
				.pressure(Math.round(convertPressure(pressure.pressure, from, to)))
				.pressureUnit(to)
				.build();
	}
}
