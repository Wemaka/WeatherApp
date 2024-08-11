package com.wemaka.weatherapp.data;

public class WindSpeed {
	private final String currentWindSpeed;
	private final String windSpeedDiff;
	private final String imgChangeWindSpeed;

	public WindSpeed(String currentWindSpeed, String windSpeedDiff, String imgChangeWindSpeed) {
		this.currentWindSpeed = currentWindSpeed;
		this.windSpeedDiff = windSpeedDiff;
		this.imgChangeWindSpeed = imgChangeWindSpeed;
	}

	public String getCurrentWindSpeed() {
		return currentWindSpeed;
	}

	public String getWindSpeedDiff() {
		return windSpeedDiff;
	}

	public String getImgChangeWindSpeed() {
		return imgChangeWindSpeed;
	}
}
