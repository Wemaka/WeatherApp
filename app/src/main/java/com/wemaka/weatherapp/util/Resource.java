package com.wemaka.weatherapp.util;

import androidx.annotation.NonNull;

import java.util.Optional;

import lombok.Getter;

@Getter
public class Resource<T> {
	private final T data;
	private final String message;

	public Resource(T data, String message) {
		this.data = data;
		this.message = message;
	}

	public boolean isSuccess() {
		return this instanceof Resource.Success;
	}

	public boolean isError() {
		return this instanceof Resource.Error;
	}

	public boolean isLoading() {
		return this instanceof Resource.Loading;
	}

	@NonNull
	public Optional<T> getSuccessData() {
		if (isSuccess()) {
			return Optional.ofNullable(this.getData());
		}

		return Optional.empty();
	}

	@NonNull
	public Optional<String> getErrorMes() {
		if (isError()) {
			return Optional.ofNullable(this.getMessage());
		}

		return Optional.empty();
	}

	@Getter
	public static class Success<T> extends Resource<T> {
		private final T data;

		public Success(@NonNull T data) {
			super(data, null);

			this.data = data;
		}
	}

	@Getter
	public static class Error<T> extends Resource<T> {
		private final String message;

		public Error(@NonNull String message) {
			super(null, message);

			this.message = message;
		}
	}

	public static class Loading<T> extends Resource<T> {
		public Loading() {
			super(null, null);
		}
	}
}

