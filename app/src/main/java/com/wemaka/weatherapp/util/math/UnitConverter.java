package com.wemaka.weatherapp.util.math;

import android.content.Context;
import android.util.TypedValue;

import org.jetbrains.annotations.NotNull;

public class UnitConverter {
	public static int dpToPx(@NotNull Context context, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}
}
