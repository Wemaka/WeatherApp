package com.wemaka.weatherapp.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PrecipitationChance {
	private final String time;
	private final Integer currentPrecipitationChance;
	private final String percent;
	private final String precipitationChanceDiff;
	private final Integer imgIdPrecipitationChance;
}
