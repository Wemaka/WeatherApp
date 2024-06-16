package com.wemaka.weatherapp.api;

import static com.wemaka.weatherapp.activity.MainActivity.TAG;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.openmeteo.sdk.Variable;
import com.openmeteo.sdk.VariablesSearch;
import com.openmeteo.sdk.VariablesWithTime;
import com.openmeteo.sdk.WeatherApiResponse;
import com.wemaka.weatherapp.DayForecastResponse;
import com.wemaka.weatherapp.MainViewModel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class WeatherParse {
	private static final String baseUrl = "https://api.open-meteo.com/v1/forecast";

	public WeatherApiResponse request(double latitude, double longitude, RequestCallback callback) {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder();
		urlBuilder
				.addQueryParameter("latitude", String.valueOf(latitude))
				.addQueryParameter("longitude", String.valueOf(longitude))
				.addQueryParameter("temperature_unit", "celsius")
				.addQueryParameter("wind_speed_unit", "kmh")
				.addQueryParameter("timeformat", "unixtime")
				.addQueryParameter("past_days", "1")
				.addQueryParameter("forecast_days", "2")
				.addQueryParameter("format", "flatbuffers")
				.addQueryParameter("minutely_15", "weather_code,temperature_2m,apparent_temperature,wind_speed_10m")
				.addQueryParameter("hourly", "weather_code,temperature_2m,precipitation_probability,uv_index,pressure_msl")
				.addQueryParameter("daily", "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset");

		String url = urlBuilder.build().toString();
		Log.i(TAG, "URL open-meteo: " + url);

		Request request = new Request.Builder()
				.url(url).method("GET", null)
				.build();

		OkHttpClient client = new OkHttpClient();
//		WeatherApiResponse weatherApiResponse = null;


		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e) {
//				Log.e(TAG, "ERROR REQUEST: api.open-meteo " + e.getMessage());
//				e.printStackTrace();
				callback.onFailure(e.getMessage());
			}

			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
				try (ResponseBody responseBody = response.body()) {
					if (!response.isSuccessful()) {
						Log.e(TAG, "ERROR REQUEST SERVER: api.open-meteo " + response);
					}

					byte[] responseIN = responseBody.bytes();
					ByteBuffer buffer = ByteBuffer.wrap(responseIN).order(ByteOrder.LITTLE_ENDIAN);
					WeatherApiResponse weatherApiResponse =
							WeatherApiResponse.getRootAsWeatherApiResponse((ByteBuffer) buffer.position(4));
					buffer.clear();

					callback.onSuccess(weatherApiResponse);
//					parseWeatherData(weatherApiResponse);

					Log.i(TAG, responseBody.toString());
				}
			}
		});

		return null;
	}

	public DayForecastResponse parseWeatherData(WeatherApiResponse response) {
		VariablesWithTime minutely15 = response.minutely15();
		VariablesWithTime hourly = response.hourly();
		VariablesWithTime daily = response.daily();
		LocationResponse locationResponse = LocationParse.getLocationInfo(response.latitude(), response.longitude());
//		Locale currentLocale = new Locale(locationResponse.getLang(), locationResponse.getCountryCode());
		Locale currentLocale = Locale.getDefault();

		Date currentDate = new Date();
		int currentIndexMinutely15 = getCurrentIndexMinutely15(minutely15, currentDate);
		int currentIndexHourly = getCurrentIndexHourly(hourly, currentDate);
		int currentIndexDay = getCurrentIndexDaily(daily, currentDate);
		DateFormat timeFormat = new SimpleDateFormat("HH:mm", currentLocale);

//		String month = Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.LONG, currentLocale);
//		String time = timeFormat.format(currentDate);

		List<DayForecastResponse> hourlyForecastList = getHourlyForecast(hourly, currentIndexHourly);
		DayForecastResponse dayForecastResponse = new DayForecastResponse(
				locationResponse.getToponymName(),
				getTemp(minutely15, currentIndexMinutely15),
				getApparentTemp(minutely15, currentIndexMinutely15),
				"",
				getWeatherCode(minutely15, currentIndexMinutely15),
				Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.LONG, currentLocale) +
						" " + new SimpleDateFormat("dd", Locale.getDefault()).format(currentDate) +
						", " + timeFormat.format(currentDate),
				getWindSpeed(minutely15, currentIndexMinutely15),
				"",
				getPrecipitationChance(hourly, currentIndexHourly),
				getPressure(hourly, currentIndexHourly),
				getUvIndex(hourly, currentIndexHourly),
				hourlyForecastList,
				"",
				"",
				timeFormat.format(new Date(getSunrise(daily, currentIndexDay))),
				timeFormat.format(new Date(getSunset(daily, currentIndexDay)))
		);

		Log.i(TAG, "RESPONSE dayForecastResponse: " + dayForecastResponse);

		return dayForecastResponse;
	}

	private int getCurrentIndexMinutely15(VariablesWithTime minutely15, Date curDate) {
		int inx = 0;
//			Date curDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
		for (long i = minutely15.time(); i <= minutely15.timeEnd(); i += minutely15.interval(), inx++) {
			Date date = new Date(i * 1000L);

			if (date.getDay() == curDate.getDay() && date.getHours() == curDate.getHours()) {
				if (curDate.getMinutes() - date.getMinutes() > 15 / 2 && inx + 1 < minutely15.variablesLength()) {
					inx++;
				}
				break;
			}
		}

		return inx;
	}

	private float getValueMinutely15(int variable, VariablesWithTime minutely15, int index) {
		return Objects.requireNonNull(new VariablesSearch(minutely15)
						.variable(variable)
						.first())
				.values(index);
	}

	private String getTemp(VariablesWithTime minutely15, int index) {
//		return Math.round(
//				new VariablesSearch(minutely15)
//						.variable(Variable.temperature)
//						.first()
//						.values(getCurrentIndexMinutely15(minutely15, curDate))
//		);

		return Math.round(getValueMinutely15(Variable.temperature, minutely15, index)) + "°";
	}

	private String getApparentTemp(VariablesWithTime minutely15, int index) {
//		return Math.round(
//				new VariablesSearch(minutely15)
//						.variable(Variable.apparent_temperature)
//						.first()
//						.values(getCurrentIndexMinutely15(minutely15, curDate))
//		);

		return Math.round(getValueMinutely15(Variable.apparent_temperature, minutely15,
				index)) + "°";
	}

	private String getWeatherCode(VariablesWithTime minutely15, int index) {
		return WeatherCode.getNameByCode((int) getValueMinutely15(Variable.weather_code,
				minutely15, index)).get();
	}

	private String getWindSpeed(VariablesWithTime minutely15, int index) {
		return Math.round(getValueMinutely15(Variable.wind_speed, minutely15, index)) + "km/h";
	}

	private String getPrecipitationChance(VariablesWithTime minutely15, int index) {
		return Math.round(getValueMinutely15(Variable.precipitation_probability, minutely15,
				index)) + "%";
	}

	private String getPressure(VariablesWithTime minutely15, int index) {
		return Math.round(getValueMinutely15(Variable.pressure_msl, minutely15, index)) + "hpa";
	}

	private String getUvIndex(VariablesWithTime minutely15, int index) {
		return String.valueOf(Math.round(getValueMinutely15(Variable.uv_index, minutely15, index)));
	}

	private int getCurrentIndexDaily(VariablesWithTime daily, Date curDate) {
		int inx = 0;
		for (long i = daily.time(); i <= daily.timeEnd(); i += daily.interval(), inx++) {
			Date date = new Date(i * 1000L);

			if (date.getDay() == curDate.getDay()) {
				break;
			}
		}

		return inx;
	}

	private long getSunrise(VariablesWithTime daily, int index) {
		return Objects.requireNonNull(new VariablesSearch(daily)
						.variable(Variable.sunrise)
						.first())
				.valuesInt64(index) * 1000;
	}

	private long getSunset(VariablesWithTime daily, int index) {
		return Objects.requireNonNull(new VariablesSearch(daily)
						.variable(Variable.sunset)
						.first())
				.valuesInt64(index) * 1000;
	}

	private int getCurrentIndexHourly(VariablesWithTime hourly, Date curDate) {
		int inx = 0;
		for (long i = hourly.time(); i <= hourly.timeEnd(); i += hourly.interval(), inx++) {
			Date date = new Date(i * 1000L);

			if (date.getDate() == curDate.getDate() && date.getHours() == curDate.getHours()) {
				break;
			}
		}

		return inx;
	}

	private List<DayForecastResponse> getHourlyForecast(VariablesWithTime hourly, int curIndex) {
		List<DayForecastResponse> hourlyForecastList = new ArrayList<>();
		String nowDate = "Now";

		for (int i = curIndex; i < curIndex + 24; i++) {
			DayForecastResponse oneHourForecast = new DayForecastResponse(
					"",
					Math.round(new VariablesSearch(hourly).variable(Variable.temperature).first().values(i)) + "°",
					"",
					"",
					"",
					nowDate,
					"",
					"",
					"",
					"",
					"",
					null,
					"",
					"",
					"",
					""
			);

			nowDate = new SimpleDateFormat("HH:00", Locale.getDefault())
					.format(new Date((hourly.time() + hourly.interval()) * (i + 1) * 1000L));

			hourlyForecastList.add(oneHourForecast);
		}

		return hourlyForecastList;
	}


//	byte[] responseIN = new byte[0];
//		try {
//		responseIN = response.body().bytes();
//	} catch (IOException e) {
//		throw new RuntimeException(e);
//	}
//	ByteBuffer buffer = ByteBuffer.wrap(responseIN).order(ByteOrder.LITTLE_ENDIAN);
//	WeatherApiResponse mApiResponse = WeatherApiResponse.getRootAsWeatherApiResponse((ByteBuffer) buffer.position(4));
//
//	VariablesWithTime minutely15 = mApiResponse.minutely15();
//	VariablesWithTime hourly = mApiResponse.hourly();
//	VariablesWithTime daily = mApiResponse.daily();
//
//	VariableWithValues maxTm = new VariablesSearch(daily).variable(Variable.temperature).first();
//
//		for (int i = 0; i < maxTm.valuesLength(); i++) {
//		System.out.println(maxTm.values(i));
//	}
//
//		buffer.clear();
}
