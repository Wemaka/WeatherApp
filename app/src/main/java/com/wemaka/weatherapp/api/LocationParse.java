package com.wemaka.weatherapp.api;

import static com.wemaka.weatherapp.activity.MainActivity.TAG;

import android.util.Log;

import com.wemaka.weatherapp.data.LocationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LocationParse {
	private static final String baseUrl = "http://api.geonames.org/findNearbyPlaceNameJSON";
	private static final String myName = "my_weather_app";

	public static LocationResponse getLocationInfo(double latitude, double longitude) {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder();
		urlBuilder
				.addQueryParameter("lat", String.valueOf(latitude))
				.addQueryParameter("lng", String.valueOf(longitude))
				.addQueryParameter("style", "FULL")
				.addQueryParameter("cities", "cities1000")
				.addQueryParameter("username", myName);

		String url = urlBuilder.build().toString();

		Log.i(TAG, "URL api.geonames: " + url);

		Request request = new Request.Builder()
				.url(url).method("GET", null)
				.build();

		OkHttpClient client = new OkHttpClient();
		try (Response response = client.newCall(request).execute()) {
			String jsonResponse = response.body().string();
			JSONObject jsonObject = new JSONObject(jsonResponse);
			JSONObject geoJson = jsonObject.getJSONArray("geonames").getJSONObject(0);

			Log.i(TAG, "RESPONSE getLocationInfo: " + jsonResponse);

			return parseGeoJson(geoJson);

		} catch (IOException e) {
			Log.e(TAG, "ERROR REQUEST: api.geonames\n" + e.getMessage());
			throw new RuntimeException(e);
		} catch (JSONException e) {
			Log.e(TAG, "ERROR JSON OBJECT: api.geonames\n" + e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private static LocationResponse parseGeoJson(JSONObject jsonObject) throws JSONException {
		return new LocationResponse(
				jsonObject.getString("toponymName"),
				jsonObject.getString("countryName"),
				jsonObject.getString("countryCode"),
				jsonObject.has("alternateNames") ?
						jsonObject.getJSONArray("alternateNames").getJSONObject(0).getString(
								"lang") : "en",
				jsonObject.getString("adminName1"),
				jsonObject.getJSONObject("timezone").getString("timeZoneId"),
				jsonObject.getString("lat"),
				jsonObject.getString("lng")
		);
	}
}
