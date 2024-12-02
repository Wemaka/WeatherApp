package com.wemaka.weatherapp.data.api;

import android.content.SharedPreferences;
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
import com.wemaka.weatherapp.store.proto.DayForecastProto;
import com.wemaka.weatherapp.store.proto.DaysForecastProto;
import com.wemaka.weatherapp.store.proto.PrecipitationChanceProto;
import com.wemaka.weatherapp.store.proto.PressureProto;
import com.wemaka.weatherapp.store.proto.TemperatureProto;
import com.wemaka.weatherapp.store.proto.PressureUnitProto;
import com.wemaka.weatherapp.store.proto.SpeedUnitProto;
import com.wemaka.weatherapp.store.proto.TemperatureUnitProto;
import com.wemaka.weatherapp.store.proto.UvIndexProto;
import com.wemaka.weatherapp.store.proto.WindSpeedProto;
import com.wemaka.weatherapp.util.ChangeIndicator;
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
import lombok.Getter;
import lombok.Setter;
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
	@Getter
	@Setter
	private static TemperatureUnitProto temperatureUnit = TemperatureUnitProto.CELSIUS;
	@Getter
	@Setter
	private static SpeedUnitProto speedUnit = SpeedUnitProto.KMH;
	@Getter
	@Setter
	private static PressureUnitProto pressureUnit = PressureUnitProto.HPA;

	public static Single<DaysForecastProto> fetchWeatherForecast(double latitude, double longitude) {
		return Single.create(emitter -> {
			HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder();
			urlBuilder
					.addQueryParameter("latitude", String.valueOf(latitude))
					.addQueryParameter("longitude", String.valueOf(longitude))
					.addQueryParameter("temperature_unit", temperatureUnit.toString().toLowerCase())
					.addQueryParameter("wind_speed_unit", speedUnit.toString().toLowerCase())
					.addQueryParameter("pressure_msl", "hpa")
					.addQueryParameter("timeformat", "unixtime")
					.addQueryParameter("timezone", "auto")
					.addQueryParameter("past_days", pastDays + "")
					.addQueryParameter("forecast_days", forecastDays + "")
					.addQueryParameter("format", "flatbuffers")
					.addQueryParameter("minutely_15", "weather_code,temperature_2m,apparent_temperature,wind_speed_10m")
					.addQueryParameter("hourly", "weather_code,temperature_2m,precipitation_probability,uv_index,pressure_msl,is_day")
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

	private static DaysForecastProto parseWeatherData(WeatherApiResponse response) {
		VariablesWithTime minutely15 = response.minutely15();
		VariablesWithTime hourly = response.hourly();
		VariablesWithTime daily = response.daily();
		VariablesWithTime current = response.current();

		timeZone = TimeZone.getTimeZone(response.timezone());
		timeFormat.setTimeZone(timeZone);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(timeZone);

		int currIndexMinutely15 = getTimeIndex(minutely15.time(), calendar, 15 * 60 * 1000);
		int currIndexHourly = getTimeIndex(hourly.time(), calendar, 60 * 60 * 1000);
		int currIndexDay = getIndexDaily();

		int isDay = getIsDay(hourly, currIndexHourly);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

		DaysForecastProto daysForecastResponse = new DaysForecastProto(
				new DayForecastProto(
						getTemp(minutely15, currIndexMinutely15),
						getApparentTemp(minutely15, currIndexMinutely15),
						getImgWeatherCode(minutely15, currIndexMinutely15, isDay),
						getWeatherCode(minutely15, currIndexMinutely15),
						dateFormat.format(Calendar.getInstance()),
						timeFormat.format(new Date(getSunrise(daily, currIndexDay))),
						timeFormat.format(new Date(getSunset(daily, currIndexDay))),
						getWindSpeed(minutely15, currIndexMinutely15),
						getPrecipitationChance(hourly, currIndexHourly),
						getPressure(hourly, currIndexHourly),
						getUvIndex(hourly, currIndexHourly),
						getHourlyTempForecast(hourly, currIndexHourly, isDay),
						getPrecipitationChanceForecast(hourly, currIndexHourly)
				),
				getWeekTempForecast(hourly, Calendar.getInstance(), currIndexHourly)
		);

		Log.i(TAG, "RESPONSE dayForecastResponse: " + daysForecastResponse);

		return daysForecastResponse;
	}

	private static VariableWithValues getVariableWithValues(VariablesWithTime variable, int variableId) {
		return Objects.requireNonNull(new VariablesSearch(variable).variable(variableId).first());
	}

	private static float getVariableValue(VariablesWithTime variable, int variableId, int index) {
		return getVariableWithValues(variable, variableId).values(index);
	}

	private static int getTimeIndex(long startTime, Calendar calendar, int interval) {
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTimeInMillis(startTime * 1000L);

		return Math.round((calendar.getTimeInMillis() - startCalendar.getTimeInMillis()) / (float) interval);
	}

	private static TemperatureProto getTemp(VariablesWithTime minutely15, int index) {
		return new TemperatureProto(
				null,
				Math.round(getVariableValue(minutely15, Variable.temperature, index)),
				null,
				temperatureUnit
		);
	}

	private static TemperatureProto getApparentTemp(VariablesWithTime minutely15, int index) {
		return new TemperatureProto(
				null,
				Math.round(getVariableValue(minutely15, Variable.apparent_temperature, index)),
				null,
				temperatureUnit
		);
	}

	private static int getImgWeatherCode(VariablesWithTime minutely15, int index, int isDay) {
		return WeatherCode.getIconIdByCode((int) getVariableValue(minutely15, Variable.weather_code, index), isDay == 1).get();
	}

	private static int getWeatherCode(VariablesWithTime minutely15, int index) {
		return WeatherCode.getResIdByCode((int) getVariableValue(minutely15, Variable.weather_code, index)).get();
	}

	private static WindSpeedProto getWindSpeed(VariablesWithTime variables, int index) {
		int currWindSpeed = Math.round(getVariableValue(variables, Variable.wind_speed, index));
		int diffWindSpeed = currWindSpeed - Math.round(getVariableValue(variables, Variable.wind_speed, index - 24));

		return new WindSpeedProto(currWindSpeed, Math.abs(diffWindSpeed),
				ChangeIndicator.getIndicatorValue(diffWindSpeed), speedUnit);
	}

	private static PrecipitationChanceProto getPrecipitationChance(VariablesWithTime variables, int index) {
		int currPrecipitationChance = Math.round(getVariableValue(variables, Variable.precipitation_probability, index));
		int diffPrecipitationChance = currPrecipitationChance - Math.round(getVariableValue(variables, Variable.precipitation_probability, index - 24));

		return new PrecipitationChanceProto("", currPrecipitationChance, Math.abs(diffPrecipitationChance),
				ChangeIndicator.getIndicatorValue(diffPrecipitationChance));
	}

	private static PressureProto getPressure(VariablesWithTime variables, int index) {
		int currPressure = Math.round(getVariableValue(variables, Variable.pressure_msl, index));
		int diffPressure = currPressure - Math.round(getVariableValue(variables, Variable.pressure_msl, index - 24));

		return new PressureProto(currPressure, Math.abs(diffPressure),
				ChangeIndicator.getIndicatorValue(diffPressure), pressureUnit);
	}

	private static UvIndexProto getUvIndex(VariablesWithTime variables, int index) {
		int currUvIndex = Math.round(getVariableValue(variables, Variable.uv_index, index));
		int diffUvIndex = currUvIndex - Math.round(getVariableValue(variables, Variable.uv_index, index - 24));

		return new UvIndexProto(currUvIndex, Math.abs(diffUvIndex),
				ChangeIndicator.getIndicatorValue(diffUvIndex));
	}

	private static int getIsDay(VariablesWithTime variables, int index) {
		return (int) getVariableValue(variables, Variable.is_day, index);
	}

	private static int getIndexDaily() {
		return pastDays;
	}

	private static long getSunrise(VariablesWithTime daily, int index) {
		return getVariableWithValues(daily, Variable.sunrise).valuesInt64(index) * 1000;
	}

	private static long getSunset(VariablesWithTime daily, int index) {
		return getVariableWithValues(daily, Variable.sunset).valuesInt64(index) * 1000;
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
					WeatherCode.getIconIdByCode(wmoIndex, isDay == 1).get(),
					temperatureUnit
			);

			hourlyForecastList.add(oneHourForecast);
		}

		return hourlyForecastList;
	}

	private static List<TemperatureProto> getWeekTempForecast(VariablesWithTime hourly, Calendar calendar, int index) {
		VariableWithValues hourlyTemp = getVariableWithValues(hourly, Variable.temperature);

		int weekDayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		weekDayIndex = (weekDayIndex < 0 ? 6 : weekDayIndex) * 24 + calendar.get(Calendar.HOUR_OF_DAY);

		List<TemperatureProto> weekTempForecast = new ArrayList<>(7 * 24);

		for (int i = index - weekDayIndex; i < index + 7 * 24 - weekDayIndex; i++) {
			weekTempForecast.add(new TemperatureProto(
							null,
							Math.round(hourlyTemp.values(i)),
							null,
							temperatureUnit
					)
			);
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
					null,
					null
			);

			precipitationChanceList.add(oneHourChance);
		}

		return precipitationChanceList;
	}
}
