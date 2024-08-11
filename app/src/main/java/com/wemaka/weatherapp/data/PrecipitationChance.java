package com.wemaka.weatherapp.data;

public class PrecipitationChance {
	private final String time;
	private final int currentPrecipitationChance;
	private final String percent;
	private final String precipitationChanceDiff;
	private final String imgPrecipitationChance;

	public PrecipitationChance(String time, int currentPrecipitationChance, String percent,
	                           String precipitationChanceDiff, String imgPrecipitationChance) {
		this.time = time;
		this.currentPrecipitationChance = currentPrecipitationChance;
		this.percent = percent;
		this.precipitationChanceDiff = precipitationChanceDiff;
		this.imgPrecipitationChance = imgPrecipitationChance;
	}

	public String getTime() {
		return time;
	}

	public int getCurrentPrecipitationChance() {
		return currentPrecipitationChance;
	}

	public String getPercent() {
		return percent;
	}

	public String getPrecipitationChanceDiff() {
		return precipitationChanceDiff;
	}

	public String getImgPrecipitationChance() {
		return imgPrecipitationChance;
	}
}
