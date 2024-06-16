package com.wemaka.weatherapp.domain;

public class HourlyForecast {
	private String time;
	private int iconResource;
	private String degree;

	public HourlyForecast(String time, int iconResource, String degree) {
		this.time = time;
		this.iconResource = iconResource;
		this.degree = degree;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setIconResource(int iconResource) {
		this.iconResource = iconResource;
	}

	public void setDegree(String degree) {
		this.degree = degree;
	}

	public String getTime() {
		return time;
	}

	public int getIconResource() {
		return iconResource;
	}

	public String getDegree() {
		return degree;
	}
}
