package com.wemaka.weatherapp.api;

import static com.wemaka.weatherapp.activity.MainActivity.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.openmeteo.sdk.Variable;
import com.openmeteo.sdk.VariableWithValues;
import com.openmeteo.sdk.VariablesSearch;
import com.openmeteo.sdk.VariablesWithTime;
import com.openmeteo.sdk.WeatherApiResponse;
import com.wemaka.weatherapp.data.ChangeIndicator;
import com.wemaka.weatherapp.data.DaysForecastResponse;
import com.wemaka.weatherapp.data.DayForecast;
import com.wemaka.weatherapp.data.LocationResponse;
import com.wemaka.weatherapp.data.PrecipitationChance;
import com.wemaka.weatherapp.data.Pressure;
import com.wemaka.weatherapp.data.Temperature;
import com.wemaka.weatherapp.data.UvIndex;
import com.wemaka.weatherapp.data.WindSpeed;

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
	private final int pastDays = 6;
	private final int forecastDays = 10;

	public WeatherApiResponse request(double latitude, double longitude, RequestCallback callback) {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder();
		urlBuilder
				.addQueryParameter("latitude", String.valueOf(latitude))
				.addQueryParameter("longitude", String.valueOf(longitude))
				.addQueryParameter("temperature_unit", "celsius")
				.addQueryParameter("wind_speed_unit", "kmh")
				.addQueryParameter("timeformat", "unixtime")
				.addQueryParameter("past_days", pastDays + "")
				.addQueryParameter("forecast_days", forecastDays + "")
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

		DaysForecastResponse daysForecastResponse = new DaysForecastResponse(
				new DayForecast(
						locationResponse.getToponymName(),
						getTemp(minutely15, currentIndexMinutely15),
						getApparentTemp(minutely15, currentIndexMinutely15),
						getImgWeatherCode(minutely15, currentIndexMinutely15),
						getWeatherCode(minutely15, currentIndexMinutely15),
						Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.LONG, currentLocale) +
								" " + new SimpleDateFormat("dd", Locale.getDefault()).format(currentDate) +
								", " + timeFormat.format(currentDate),
						timeFormat.format(new Date(getSunrise(daily, currentIndexDay))),
						timeFormat.format(new Date(getSunset(daily, currentIndexDay))),
						getWindSpeed(minutely15, currentIndexMinutely15),
						getPrecipitationChance(hourly, currentIndexHourly),
						getPressure(hourly, currentIndexHourly),
						getUvIndex(hourly, currentIndexHourly),
						getHourlyTempForecast(hourly, currentIndexHourly),
						getPrecipitationChanceForecast(hourly, currentIndexHourly)
				),
				getWeekTempForecast(hourly, currentDate, currentIndexHourly)
		);

		Log.i(TAG, "RESPONSE dayForecastResponse: " + daysForecastResponse);

		return daysForecastResponse;
	}

	private int getIndexMinutely15(@NonNull VariablesWithTime minutely15, @NonNull Date date) {
		Date startDate = new Date(minutely15.time() * 1000L);

		return pastDays * 24 * 60 / 15 +
				Math.round((float) ((date.getHours() - startDate.getHours()) * 60 + startDate.getMinutes()) / 15);
	}

	private float getValueMinutely15(int variable, VariablesWithTime minutely15, int index) {
		return Objects.requireNonNull(new VariablesSearch(minutely15)
						.variable(variable)
						.first())
				.values(index);
	}

	private String getTemp(VariablesWithTime minutely15, int index) {
		return Math.round(getValueMinutely15(Variable.temperature, minutely15, index)) + "°";
	}

	private String getApparentTemp(VariablesWithTime minutely15, int index) {
		return Math.round(getValueMinutely15(Variable.apparent_temperature, minutely15,
				index)) + "°";
	}

	private int getImgWeatherCode(VariablesWithTime minutely15, int index) {
		return WeatherCode.getIconIdByCode((int) getValueMinutely15(Variable.weather_code,
				minutely15, index), true).get();
	}

	private String getWeatherCode(VariablesWithTime minutely15, int index) {
		return WeatherCode.getNameByCode((int) getValueMinutely15(Variable.weather_code,
				minutely15, index)).get();
	}

	private WindSpeed getWindSpeed(VariablesWithTime minutely15, int index) {
		int currWindSpeed = Math.round(getValueMinutely15(Variable.wind_speed, minutely15, index));
		int diffWindSpeed = currWindSpeed - Math.round(getValueMinutely15(Variable.wind_speed,
				minutely15,
				index - 1));

		return new WindSpeed(currWindSpeed + "km/h", Math.abs(diffWindSpeed) + "km/h",
				ChangeIndicator.getIndicatorValue(diffWindSpeed));
	}

	private PrecipitationChance getPrecipitationChance(VariablesWithTime minutely15, int index) {
		int currPrecipitationChance =
				Math.round(getValueMinutely15(Variable.precipitation_probability, minutely15, index));
		int diffPrecipitationChance =
				currPrecipitationChance - Math.round(getValueMinutely15(Variable.precipitation_probability, minutely15, index - 1));

		return new PrecipitationChance("", currPrecipitationChance,
				currPrecipitationChance + "%", Math.abs(diffPrecipitationChance) + "%",
				ChangeIndicator.getIndicatorValue(diffPrecipitationChance));
	}

	private Pressure getPressure(VariablesWithTime minutely15, int index) {
		int currPressure = Math.round(getValueMinutely15(Variable.pressure_msl, minutely15, index));
		int diffPressure = currPressure - Math.round(getValueMinutely15(Variable.pressure_msl,
				minutely15, index - 1));

		return new Pressure(currPressure + "hpa", diffPressure + "hpa",
				ChangeIndicator.getIndicatorValue(diffPressure));
	}

	private UvIndex getUvIndex(VariablesWithTime minutely15, int index) {
		int currUvIndex = Math.round(getValueMinutely15(Variable.uv_index, minutely15, index));
		int diffUvIndex = currUvIndex - Math.round(getValueMinutely15(Variable.uv_index,
				minutely15, index - 1));

		return new UvIndex(String.valueOf(currUvIndex), String.valueOf(diffUvIndex),
				ChangeIndicator.getIndicatorValue(diffUvIndex));
	}

	private int getIndexDaily(VariablesWithTime daily, Date date) {
		return pastDays;
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
		Date startDate = new Date(hourly.time() * 1000L);

		return pastDays * 24 + date.getHours() - startDate.getHours();
	}

	private List<Temperature> getHourlyTempForecast(VariablesWithTime hourly, int hourlyIndex) {
		List<Temperature> hourlyForecastList = new ArrayList<>();
		String nowDate = "Now";

		for (int i = hourlyIndex; i < hourlyIndex + 24; i++) {
			int wmoIndex =
					(int) new VariablesSearch(hourly).variable(Variable.weather_code).first().values(i);

			Temperature oneHourForecast = new Temperature(
					nowDate,
					Math.round(new VariablesSearch(hourly).variable(Variable.temperature).first().values(i)) + "°",
					WeatherCode.getIconIdByCode(wmoIndex, true).get()
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

		int weekDayIndex = date.getDay() - 1;
		weekDayIndex = (weekDayIndex < 0 ? 6 : weekDayIndex) * 24 + date.getHours();

		for (int i = index - weekDayIndex; i < index + 7 * 24 - weekDayIndex; i++) {
			weekTempForecast.add((float) (Math.round(hourlyTemp.values(i) * 10) / 10));
		}
		return weekTempForecast;
	}

	private List<PrecipitationChance> getPrecipitationChanceForecast(VariablesWithTime hourly, int hourlyIndex) {
		List<PrecipitationChance> precipitationChanceList = new ArrayList<>();
		String nowDate = "Now";
		VariableWithValues hourlyPrecipitation = Objects.requireNonNull(new VariablesSearch(hourly)
				.variable(Variable.precipitation_probability)
				.first());

		for (int i = hourlyIndex; i < hourlyIndex + 24; i++) {
			PrecipitationChance oneHourChance = new PrecipitationChance(
					nowDate,
					(int) hourlyPrecipitation.values(i),
					(int) hourlyPrecipitation.values(i) + "%",
					"",
					null
			);

			nowDate = new SimpleDateFormat("HH:00", Locale.getDefault())
					.format(new Date((hourly.time() + hourly.interval()) * (i + 1) * 1000L));

			precipitationChanceList.add(oneHourChance);
		}

		return precipitationChanceList;
	}
}
