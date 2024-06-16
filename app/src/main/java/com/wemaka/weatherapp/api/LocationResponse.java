package com.wemaka.weatherapp.api;

public class LocationResponse {
	private final String toponymName;
	private final String countryName;
	private final String countryCode;
	private final String lang;
	private final String adminName1;
	private final String timeZone;
	private final String latitude;
	private final String longitude;

	public LocationResponse(String toponymName, String countryName, String countryCode, String lang, String adminName1, String timeZone, String latitude, String longitude) {
		this.toponymName = toponymName;
		this.countryName = countryName;
		this.countryCode = countryCode;
		this.lang = lang;
		this.adminName1 = adminName1;
		this.timeZone = timeZone;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getToponymName() {
		return toponymName;
	}

	public String getCountryName() {
		return countryName;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getLang() {
		return lang;
	}

	public String getAdminName1() {
		return adminName1;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}
}
