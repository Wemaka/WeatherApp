package com.wemaka.weatherapp.data;

public class UvIndex {
	private final String currentUvIndex;
	private final String uvIndexDiff;
	private final String imgChangeUvIndex;

	public UvIndex(String currentUvIndex, String uvIndexDiff, String imgChangeUvIndex) {
		this.currentUvIndex = currentUvIndex;
		this.uvIndexDiff = uvIndexDiff;
		this.imgChangeUvIndex = imgChangeUvIndex;
	}

	public String getCurrentUvIndex() {
		return currentUvIndex;
	}

	public String getUvIndexDiff() {
		return uvIndexDiff;
	}

	public String getImgChangeUvIndex() {
		return imgChangeUvIndex;
	}
}
