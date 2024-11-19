package com.wemaka.weatherapp.data.api;

import android.net.TrafficStats;
import android.util.Log;

import androidx.annotation.NonNull;

import com.wemaka.weatherapp.data.model.PlaceInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Single;
import lombok.Getter;
import lombok.Setter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class GeoNamesClient {
	public static final String TAG = "GeoNamesClient";
	private static final String baseUrl = "http://api.geonames.org";
	private static final String myName = "my_weather_app";
	private static final OkHttpClient client = new OkHttpClient.Builder()
			.connectTimeout(10, TimeUnit.SECONDS)
			.readTimeout(30, TimeUnit.SECONDS)
			.build();
	@Getter
	@Setter
	private static Locale locale = Locale.getDefault();

	public static Single<PlaceInfo> fetchNearestPlaceInfo(double latitude, double longitude) {
		return Single.create(emitter -> {
			HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder();
			urlBuilder.addPathSegment("findNearbyPlaceNameJSON")
					.addQueryParameter("lat", String.valueOf(latitude))
					.addQueryParameter("lng", String.valueOf(longitude))
					.addQueryParameter("style", "FULL")
					.addQueryParameter("cities", "cities1000")
					.addQueryParameter("lang", locale.getLanguage())
					.addQueryParameter("username", myName);

			String url = urlBuilder.build().toString();

			Log.i(TAG, "URL api.geonames: " + url);

			TrafficStats.setThreadStatsTag(112);
			Request request = new Request.Builder()
					.url(url).method("GET", null)
					.build();

			client.newCall(request).enqueue(new Callback() {
				@Override
				public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
					try (ResponseBody responseBody = response.body()) {
						if (!response.isSuccessful()) {
							emitter.onError(new IOException("ERROR REQUEST SERVER: api.geonames " + response));
						}

						if (responseBody == null) {
							emitter.onError(new IOException("Empty body"));
							return;
						}

						String jsonResponse = responseBody.string();
						responseBody.close();
						Log.i(TAG, "RESPONSE fetchNearestPlaceInfo: " + jsonResponse);
						JSONObject jsonObject = new JSONObject(jsonResponse);

						if (jsonObject.getJSONArray("geonames").length() > 0) {
							JSONObject geoJson = jsonObject.getJSONArray("geonames").getJSONObject(0);
							emitter.onSuccess(parsePlaceInfoJson(geoJson));
						} else {
							emitter.onError(new JSONException("Array 'geonames' is empty"));
						}

					} catch (IOException | JSONException e) {
						emitter.onError(e);
					}
				}

				@Override
				public void onFailure(@NonNull Call call, @NonNull IOException e) {
					emitter.onError(e);
				}
			});
		});
	}

	private static PlaceInfo parsePlaceInfoJson(JSONObject jsonObject) throws JSONException {
		return new PlaceInfo(
				jsonObject.getString("name"),
				jsonObject.getString("countryName"),
				jsonObject.getString("countryCode"),
				jsonObject.has("alternateNames") ?
						jsonObject.getJSONArray("alternateNames").getJSONObject(0).getString(
								"lang") : "en",
				jsonObject.getString("adminName1"),
				jsonObject.getString("lat"),
				jsonObject.getString("lng")
		);
	}

	public static Single<List<PlaceInfo>> searchLocation(String query) {
		return Single.create(emitter -> {
			HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder();
			urlBuilder.addPathSegment("searchJSON")
					.addQueryParameter("q", query)
					.addQueryParameter("style", "LONG")
					.addQueryParameter("maxRows", "15")
					.addQueryParameter("fuzzy", "0.8")
					.addQueryParameter("lang", Locale.getDefault().getLanguage())
					.addQueryParameter("username", myName);

			String url = urlBuilder.build().toString();

			Log.i(TAG, "URL api.geonames: " + url);

			TrafficStats.setThreadStatsTag(113);
			Request request = new Request.Builder()
					.url(url).method("GET", null)
					.build();

			client.newCall(request).enqueue(new Callback() {
				@Override
				public void onFailure(@NonNull Call call, @NonNull IOException e) {
					emitter.onError(e);
				}

				@Override
				public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
					try (ResponseBody responseBody = response.body()) {
						if (!response.isSuccessful()) {
							emitter.onError(new IOException("ERROR REQUEST SERVER: api.geonames " + response));
						}

						if (responseBody == null) {
							emitter.onError(new IOException("Empty body"));
							return;
						}

						String jsonResponse = responseBody.string();
						responseBody.close();

						JSONObject jsonObject = new JSONObject(jsonResponse);

						Log.i(TAG, "RESPONSE searchLocation: " + jsonResponse);

						emitter.onSuccess(parseSearch(jsonObject.getJSONArray("geonames")));

					} catch (IOException | JSONException e) {
						emitter.onError(e);
					}
				}
			});
		});
	}

	private static List<PlaceInfo> parseSearch(JSONArray jsonArray) throws JSONException {
		List<PlaceInfo> listQuery = new ArrayList<>();

		for (int i = 0; i < jsonArray.length(); i++) {
			listQuery.add(parsePlaceInfoJson(jsonArray.getJSONObject(i)));
		}

		Log.i(TAG, "listQuery: " + listQuery);

		return listQuery;
	}
}
