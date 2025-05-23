package com.wemaka.weatherapp.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class ConnectionNetworkProvider implements NetworkChecker {
	private final Context context;

	public ConnectionNetworkProvider(@ApplicationContext Context context) {
		this.context = context;
	}

	@Override
	public boolean hasInternetConnection() {
		ConnectivityManager connectivityManager =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		Network activeNetwork = connectivityManager.getActiveNetwork();
		if (activeNetwork == null) {
			return false;
		}

		NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
		if (capabilities == null) {
			return false;
		}

		return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
				capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
				capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
				capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
	}
}
