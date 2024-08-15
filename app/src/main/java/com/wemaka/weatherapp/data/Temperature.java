package com.wemaka.weatherapp.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Temperature {
	private final String time;
	private final String temperature;
	private final Integer imgIdWeatherCode;
}
