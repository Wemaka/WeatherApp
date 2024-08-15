package com.wemaka.weatherapp.data;

import com.wemaka.weatherapp.R;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChangeIndicator {
	UP(R.drawable.ic_arrow_upgrade),
	DOWN(R.drawable.ic_arrow_downgrade),
	UNCHANGED(R.drawable.ic_arrow_unchanged),
	;

	int iconId;

	public static int getIndicatorValue(int value) {
		return value > 0 ? UP.getIconId() :
				value < 0 ? DOWN.getIconId() : UNCHANGED.getIconId();
	}
}

