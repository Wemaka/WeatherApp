package com.wemaka.weatherapp.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Pressure {
	private final String currentPressure;
	private final String pressureDiff;
	private final Integer imgIdChangePressure;
}
