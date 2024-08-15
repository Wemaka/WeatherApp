package com.wemaka.weatherapp.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LocationResponse {
	private final String toponymName;
	private final String countryName;
	private final String countryCode;
	private final String lang;
	private final String adminName1;
	private final String timeZone;
	private final String latitude;
	private final String longitude;
}
