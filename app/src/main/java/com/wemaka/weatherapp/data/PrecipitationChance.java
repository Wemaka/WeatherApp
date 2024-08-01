package com.wemaka.weatherapp.data;

public class PrecipitationChance {
	private final String time;
	private final int progress;
	private final String percent;

	public PrecipitationChance(String time, int progress, String percent) {
		this.time = time;
		this.progress = progress;
		this.percent = percent;
	}

	public String getTime() {
		return time;
	}

	public int getProgress() {
		return progress;
	}

	public String getPercent() {
		return percent;
	}
}
