package com.wemaka.weatherapp.api;

import static com.wemaka.weatherapp.activity.MainActivity.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.openmeteo.sdk.Aggregation;
import com.openmeteo.sdk.Variable;
import com.openmeteo.sdk.VariableWithValues;
import com.openmeteo.sdk.VariablesSearch;
import com.openmeteo.sdk.VariablesWithTime;
import com.openmeteo.sdk.WeatherApiResponse;
import com.wemaka.weatherapp.DaysForecastResponse;
import com.wemaka.weatherapp.DayForecast;

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
				.addQueryParameter("past_days", "6")
				.addQueryParameter("forecast_days", "10")
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

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e) {
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

					Log.i(TAG, responseBody.toString());
				}
			}
		});

		return null;
	}

	public DaysForecastResponse parseWeatherData(WeatherApiResponse response) {
		VariablesWithTime minutely15 = response.minutely15();
		VariablesWithTime hourly = response.hourly();
		VariablesWithTime daily = response.daily();

		LocationResponse locationResponse = LocationParse.getLocationInfo(response.latitude(), response.longitude());
//		Locale currentLocale = new Locale(locationResponse.getLang(), locationResponse.getCountryCode());
		Locale currentLocale = Locale.getDefault();

		Date currentDate = new Date();
		int currentIndexMinutely15 = getIndexMinutely15(minutely15, currentDate);
		int currentIndexHourly = getIndexHourly(hourly, currentDate);
		int currentIndexDay = getIndexDaily(daily, currentDate);
		DateFormat timeFormat = new SimpleDateFormat("HH:mm", currentLocale);

//		for (long i = daily.time(); i <= daily.timeEnd(); i += daily.interval()) {
//			Date dateDay = new Date(i * 1000L);
//
//			Log.i(TAG, dateDay.toString());
//		}

		DaysForecastResponse daysForecastResponse = new DaysForecastResponse(
				new DayForecast(
						locationResponse.getToponymName(),
						getTemp(minutely15, currentIndexMinutely15),
						getApparentTemp(minutely15, currentIndexMinutely15),
						"",
						getWeatherCode(minutely15, currentIndexMinutely15),
						Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.LONG, currentLocale) +
								" " + new SimpleDateFormat("dd", Locale.getDefault()).format(currentDate) +
								", " + timeFormat.format(currentDate),
						timeFormat.format(new Date(getSunrise(daily, currentIndexDay))),
						timeFormat.format(new Date(getSunset(daily, currentIndexDay))),
						getWindSpeed(minutely15, currentIndexMinutely15),
						"",
						getPrecipitationChance(hourly, currentIndexHourly),
						getPressure(hourly, currentIndexHourly),
						getUvIndex(hourly, currentIndexHourly),
						getHourlyForecast(hourly, currentIndexHourly)
				),
				getWeekTempForecast(hourly, currentDate, currentIndexHourly)
		);

		Log.i(TAG, "RESPONSE dayForecastResponse: " + daysForecastResponse);

		return daysForecastResponse;
	}

	public static int getIndexMinutely15(@NonNull VariablesWithTime minutely15, @NonNull Date date) {
		int inx = 0;
//			Date curDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
		for (long i = minutely15.time(); i <= minutely15.timeEnd(); i += minutely15.interval(), inx++) {
			Date dateMin = new Date(i * 1000L);

			if (dateMin.getDay() == date.getDay() && dateMin.getHours() == date.getHours()) {
				if (date.getMinutes() - dateMin.getMinutes() > 15 / 2 && inx + 1 < minutely15.variablesLength()) {
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

	private int getIndexDaily(VariablesWithTime daily, Date date) {
		int inx = 0;
		for (long i = daily.time(); i <= daily.timeEnd(); i += daily.interval(), inx++) {
			Date dateDay = new Date(i * 1000L);

			if (dateDay.getDay() == date.getDay()) {
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

	private int getIndexHourly(@NonNull VariablesWithTime hourly, @NonNull Date date) {
		int inx = 0;
		for (long i = hourly.time(); i <= hourly.timeEnd(); i += hourly.interval(), inx++) {
			Date dateHour = new Date(i * 1000L);

			if (dateHour.getDate() == date.getDate() && dateHour.getHours() == date.getHours()) {
				break;
			}
		}

		return inx;
	}

	private List<DayForecast> getHourlyForecast(VariablesWithTime hourly, int hourlyIndex) {
		List<DayForecast> hourlyForecastList = new ArrayList<>();
		String nowDate = "Now";

		for (int i = hourlyIndex; i < hourlyIndex + 24; i++) {
			DayForecast oneHourForecast = new DayForecast(
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
					"",
					"",
					null
			);

			nowDate = new SimpleDateFormat("HH:00", Locale.getDefault())
					.format(new Date((hourly.time() + hourly.interval()) * (i + 1) * 1000L));

			hourlyForecastList.add(oneHourForecast);
		}

		return hourlyForecastList;
	}

	private List<Float> getWeekTempForecast(VariablesWithTime hourly, Date date, int index) {
		List<Float> weekTempForecast = new ArrayList<>();
		VariableWithValues hourlyTemp = Objects.requireNonNull(new VariablesSearch(hourly)
				.variable(Variable.temperature)
				.first());

		int weekDay = date.getDay() - 1;
		weekDay = (weekDay < 0 ? 6 : weekDay) * 24 + date.getHours();

		for (int i = index - weekDay; i < index + 7 * 24 - weekDay; i++) {
			weekTempForecast.add((float) (Math.round(hourlyTemp.values(i) * 10) / 10));
		}
		return weekTempForecast;
	}
}
