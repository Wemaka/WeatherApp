package com.wemaka.weatherapp.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UvIndex {
	private final String currentUvIndex;
	private final String uvIndexDiff;
	private final Integer imgIdChangeUvIndex;
}
