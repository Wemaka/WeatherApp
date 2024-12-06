package com.wemaka.weatherapp.util.math;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;

import com.wemaka.weatherapp.store.proto.PressureUnitProto;
import com.wemaka.weatherapp.store.proto.SpeedUnitProto;
import com.wemaka.weatherapp.store.proto.TemperatureUnitProto;

import org.jetbrains.annotations.NotNull;

public class UnitConverter {
	public static final String TAG = "UnitConverter";

	public static int dpToPx(@NotNull Context context, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}

	public static float convertTemperature(float temperature, TemperatureUnitProto fromUnit, TemperatureUnitProto toUnit) {
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

	public static float convertSpeed(float speed, SpeedUnitProto fromUnit, SpeedUnitProto toUnit) {
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

	public static float convertPressure(float pressure, PressureUnitProto fromUnit, PressureUnitProto toUnit) {
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
}
