package com.wemaka.weatherapp.domain;

public class HourlyForecastRain {
	private String time;
	private int progress;
	private String percent;

	public HourlyForecastRain(String time, int progress, String percent) {
		this.time = time;
		this.progress = progress;
		this.percent = percent;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public void setPercent(String percent) {
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
