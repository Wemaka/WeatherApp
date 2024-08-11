package com.wemaka.weatherapp.data;

public class Pressure {
	private final String currentPressure;
	private final String pressureDiff;
	private final String imgChangePressure;

	public Pressure(String currentPressure, String pressureDiff, String imgChangePressure) {
		this.currentPressure = currentPressure;
		this.pressureDiff = pressureDiff;
		this.imgChangePressure = imgChangePressure;
	}

	public String getCurrentPressure() {
		return currentPressure;
	}

	public String getPressureDiff() {
		return pressureDiff;
	}

	public String getImgChangePressure() {
		return imgChangePressure;
	}
}
