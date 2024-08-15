package com.wemaka.weatherapp.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WindSpeed {
	private final String currentWindSpeed;
	private final String windSpeedDiff;
	private final Integer imgIdChangeWindSpeed;
}
