package com.wemaka.weatherapp.data.api;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.net.TrafficStats;
import android.util.Log;

import androidx.annotation.NonNull;

import com.openmeteo.sdk.Variable;
import com.openmeteo.sdk.VariableWithValues;
import com.openmeteo.sdk.VariablesSearch;
import com.openmeteo.sdk.VariablesWithTime;
import com.openmeteo.sdk.WeatherApiResponse;
import com.wemaka.weatherapp.util.ChangeIndicator;
import com.wemaka.weatherapp.store.proto.DayForecastProto;
import com.wemaka.weatherapp.store.proto.DaysForecastResponseProto;
import com.wemaka.weatherapp.store.proto.PrecipitationChanceProto;
import com.wemaka.weatherapp.store.proto.PressureProto;
import com.wemaka.weatherapp.store.proto.TemperatureProto;
import com.wemaka.weatherapp.store.proto.UvIndexProto;
import com.wemaka.weatherapp.store.proto.WindSpeedProto;
import com.wemaka.weatherapp.util.WeatherCode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Single;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class OpenMeteoClient {
	public static final String TAG = "OpenMeteoClient";
	private static final String baseUrl = "https://api.open-meteo.com/v1/forecast";
	private static final int pastDays = 6;
	private static final int forecastDays = 10;
	private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
	private static TimeZone timeZone = TimeZone.getDefault();
	private static final OkHttpClient client = new OkHttpClient.Builder()
			.connectTimeout(10, TimeUnit.SECONDS)
			.readTimeout(30, TimeUnit.SECONDS)
			.build();

	public static Single<DaysForecastResponseProto> fetchWeatherForecast(double latitude, double longitude) {
		return Single.create(emitter -> {
			HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder();
			urlBuilder
					.addQueryParameter("latitude", String.valueOf(latitude))
					.addQueryParameter("longitude", String.valueOf(longitude))
					.addQueryParameter("temperature_unit", "celsius")
					.addQueryParameter("wind_speed_unit", "kmh")
					.addQueryParameter("pressure_msl", "hpa")
					.addQueryParameter("timeformat", "unixtime")
					.addQueryParameter("timezone", "auto")
					.addQueryParameter("past_days", pastDays + "")
					.addQueryParameter("forecast_days", forecastDays + "")
					.addQueryParameter("format", "flatbuffers")
					.addQueryParameter("current", "is_day")
					.addQueryParameter("minutely_15", "weather_code,temperature_2m,apparent_temperature,wind_speed_10m")
					.addQueryParameter("hourly", "weather_code,temperature_2m,precipitation_probability,uv_index,pressure_msl")
					.addQueryParameter("daily", "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset");

			String url = urlBuilder.build().toString();

			Log.i(TAG, "URL open-meteo: " + url);

			TrafficStats.setThreadStatsTag(111);
			Request request = new Request.Builder()
					.url(url).method("GET", null)
					.build();

			client.newCall(request).enqueue(new Callback() {
				@Override
				public void onResponse(@NonNull Call call, @NonNull Response response) {
					try (ResponseBody responseBody = response.body()) {
						if (!response.isSuccessful()) {
							emitter.onError(new IOException("ERROR REQUEST SERVER: api.open-meteo " + response));
						}

						if (responseBody == null) {
							emitter.onError(new IOException("Empty body"));
							return;
						}

						byte[] responseIN = responseBody.bytes();
						responseBody.close();
						ByteBuffer buffer = ByteBuffer.wrap(responseIN).order(ByteOrder.LITTLE_ENDIAN);
						WeatherApiResponse weatherApiResponse =
								WeatherApiResponse.getRootAsWeatherApiResponse((ByteBuffer) buffer.position(4));
						buffer.clear();

						Log.i(TAG, responseBody.toString());

						emitter.onSuccess(parseWeatherData(weatherApiResponse));
					} catch (IOException e) {
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

	private static DaysForecastResponseProto parseWeatherData(WeatherApiResponse response) {
		VariablesWithTime minutely15 = response.minutely15();
		VariablesWithTime hourly = response.hourly();
		VariablesWithTime daily = response.daily();
		VariablesWithTime current = response.current();

		timeZone = TimeZone.getTimeZone(response.timezone());
		timeFormat.setTimeZone(timeZone);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(timeZone);

		int currentIndexMinutely15 = getIndexMinutely15(minutely15, calendar);
		int currentIndexHourly = getIndexHourly(hourly, calendar);
		int currentIndexDay = getIndexDaily();

		int isDay = (int) Objects.requireNonNull(new VariablesSearch(current).variable(Variable.is_day).first()).value();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

		DaysForecastResponseProto daysForecastResponse = new DaysForecastResponseProto(
				new DayForecastProto(
						getTemp(minutely15, currentIndexMinutely15),
						getApparentTemp(minutely15, currentIndexMinutely15),
						getImgWeatherCode(minutely15, currentIndexMinutely15, isDay),
						getWeatherCode(minutely15, currentIndexMinutely15),
						dateFormat.format(Calendar.getInstance()),
						timeFormat.format(new Date(getSunrise(daily, currentIndexDay))),
						timeFormat.format(new Date(getSunset(daily, currentIndexDay))),
						getWindSpeed(minutely15, currentIndexMinutely15),
						getPrecipitationChance(hourly, currentIndexHourly),
						getPressure(hourly, currentIndexHourly),
						getUvIndex(hourly, currentIndexHourly),
						getHourlyTempForecast(hourly, currentIndexHourly, isDay),
						getPrecipitationChanceForecast(hourly, currentIndexHourly)
				),
				getWeekTempForecast(hourly, Calendar.getInstance(), currentIndexHourly)
		);

		Log.i(TAG, "RESPONSE dayForecastResponse: " + daysForecastResponse);

		return daysForecastResponse;
	}

	private static int getIndexMinutely15(@NonNull VariablesWithTime minutely15,
	                                      @NonNull Calendar calendar) {
		Calendar startCalendar = (Calendar) calendar.clone();
		startCalendar.setTimeInMillis(minutely15.time() * 1000L);

		return pastDays * 24 * 60 / 15 +
				Math.round((float) (
						(calendar.get(Calendar.HOUR_OF_DAY) - startCalendar.get(Calendar.HOUR_OF_DAY))
								* 60 + startCalendar.get(Calendar.MINUTE)
				) / 15);
	}

	private static float getValueMinutely15(int variable, VariablesWithTime minutely15, int index) {
		return Objects.requireNonNull(new VariablesSearch(minutely15).variable(variable).first())
				.values(index);
	}

	private static int getTemp(VariablesWithTime minutely15, int index) {
		return Math.round(getValueMinutely15(Variable.temperature, minutely15, index));
	}

	private static int getApparentTemp(VariablesWithTime minutely15, int index) {
		return Math.round(getValueMinutely15(Variable.apparent_temperature, minutely15,
				index));
	}

	private static int getImgWeatherCode(VariablesWithTime minutely15, int index, int isDay) {
		return WeatherCode.getIconIdByCode((int) getValueMinutely15(Variable.weather_code,
				minutely15, index), isDay == 1).get();
	}

	private static int getWeatherCode(VariablesWithTime minutely15, int index) {
		return WeatherCode.getResIdByCode((int) getValueMinutely15(Variable.weather_code,
				minutely15, index)).get();
	}

	private static WindSpeedProto getWindSpeed(VariablesWithTime minutely15, int index) {
		int currWindSpeed = Math.round(getValueMinutely15(Variable.wind_speed, minutely15, index));
		int diffWindSpeed = currWindSpeed - Math.round(getValueMinutely15(Variable.wind_speed,
				minutely15,
				index - 1));

		return new WindSpeedProto(currWindSpeed, Math.abs(diffWindSpeed),
				ChangeIndicator.getIndicatorValue(diffWindSpeed));
	}

	private static PrecipitationChanceProto getPrecipitationChance(VariablesWithTime minutely15, int index) {
		int currPrecipitationChance =
				Math.round(getValueMinutely15(Variable.precipitation_probability, minutely15, index));
		int diffPrecipitationChance =
				currPrecipitationChance - Math.round(getValueMinutely15(Variable.precipitation_probability, minutely15, index - 1));

		return new PrecipitationChanceProto("", currPrecipitationChance,
				currPrecipitationChance, Math.abs(diffPrecipitationChance),
				ChangeIndicator.getIndicatorValue(diffPrecipitationChance));
	}

	private static PressureProto getPressure(VariablesWithTime minutely15, int index) {
		int currPressure = Math.round(getValueMinutely15(Variable.pressure_msl, minutely15, index));
		int diffPressure = currPressure - Math.round(getValueMinutely15(Variable.pressure_msl,
				minutely15, index - 1));

		return new PressureProto(currPressure, diffPressure,
				ChangeIndicator.getIndicatorValue(diffPressure));
	}

	private static UvIndexProto getUvIndex(VariablesWithTime minutely15, int index) {
		int currUvIndex = Math.round(getValueMinutely15(Variable.uv_index, minutely15, index));
		int diffUvIndex = currUvIndex - Math.round(getValueMinutely15(Variable.uv_index,
				minutely15, index - 1));

		return new UvIndexProto(currUvIndex, diffUvIndex,
				ChangeIndicator.getIndicatorValue(diffUvIndex));
	}

	private static int getIndexDaily() {
		return pastDays;
	}

	private static long getSunrise(VariablesWithTime daily, int index) {
		return Objects.requireNonNull(new VariablesSearch(daily)
						.variable(Variable.sunrise)
						.first())
				.valuesInt64(index) * 1000;
	}

	private static long getSunset(VariablesWithTime daily, int index) {
		return Objects.requireNonNull(new VariablesSearch(daily)
						.variable(Variable.sunset)
						.first())
				.valuesInt64(index) * 1000;
	}

	private static int getIndexHourly(@NonNull VariablesWithTime hourly, @NonNull Calendar calendar) {
		Calendar startCalendar = (Calendar) calendar.clone();
		startCalendar.setTimeInMillis(hourly.time() * 1000L);

		return pastDays * 24 + calendar.get(Calendar.HOUR_OF_DAY) - startCalendar.get(Calendar.HOUR_OF_DAY);
	}

	private static List<TemperatureProto> getHourlyTempForecast(VariablesWithTime hourly, int hourlyIndex, int isDay) {
		List<TemperatureProto> hourlyForecastList = new ArrayList<>();

		VariableWithValues variableWeatherCode = new VariablesSearch(hourly).variable(Variable.weather_code).first();
		VariableWithValues variableTemp = new VariablesSearch(hourly).variable(Variable.temperature).first();

		for (int i = hourlyIndex; i < hourlyIndex + 24; i++) {
			int wmoIndex = (int) variableWeatherCode.values(i);

			TemperatureProto oneHourForecast = new TemperatureProto(
					timeFormat.format(new Date((hourly.time() + (long) hourly.interval() * i) * 1000L)),
					Math.round(variableTemp.values(i)),
					WeatherCode.getIconIdByCode(wmoIndex, isDay == 1).get()
			);

			hourlyForecastList.add(oneHourForecast);
		}

		return hourlyForecastList;
	}

	private static List<Float> getWeekTempForecast(VariablesWithTime hourly, Calendar calendar, int index) {
		List<Float> weekTempForecast = new ArrayList<>();
		VariableWithValues hourlyTemp = Objects.requireNonNull(new VariablesSearch(hourly)
				.variable(Variable.temperature)
				.first());

		int weekDayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		weekDayIndex = (weekDayIndex < 0 ? 6 : weekDayIndex) * 24 + calendar.get(Calendar.HOUR_OF_DAY);

		for (int i = index - weekDayIndex; i < index + 7 * 24 - weekDayIndex; i++) {
			weekTempForecast.add((float) (Math.round(hourlyTemp.values(i) * 10) / 10));
		}

		return weekTempForecast;
	}

	private static List<PrecipitationChanceProto> getPrecipitationChanceForecast(VariablesWithTime hourly, int hourlyIndex) {
		List<PrecipitationChanceProto> precipitationChanceList = new ArrayList<>();

		VariableWithValues hourlyPrecipitation = Objects.requireNonNull(new VariablesSearch(hourly)
				.variable(Variable.precipitation_probability)
				.first());

		for (int i = hourlyIndex; i < hourlyIndex + 24; i++) {
			PrecipitationChanceProto oneHourChance = new PrecipitationChanceProto(
					timeFormat.format(new Date((hourly.time() + (long) hourly.interval() * i) * 1000L)),
					(int) hourlyPrecipitation.values(i),
					(int) hourlyPrecipitation.values(i),
					null,
					0
			);

			precipitationChanceList.add(oneHourChance);
		}

		return precipitationChanceList;
	}
}
