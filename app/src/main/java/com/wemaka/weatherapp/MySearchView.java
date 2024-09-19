package com.wemaka.weatherapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;

public class MySearchView extends SearchView {
	private final Resources res = Resources.getSystem();
	private final int search_mag_icon = res.getIdentifier("android:id/search_button", null, null);
	private ImageView searchIcon;

	public MySearchView(Context context) {
		super(context);
		init();
	}

	public MySearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MySearchView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public MySearchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init() {
		// Используем post для выполнения после инициализации
		post(() -> {
			searchIcon = findViewById(search_mag_icon);
//			if (searchIcon == null) {
			// Попробуем найти иконку через другие пути или отложенно
//				searchIcon = findIconInViewHierarchy((ViewGroup) getParent());
//			}

			setColorFilter(R.color.white);
		});
	}

	private ImageView findIconInViewHierarchy(ViewGroup viewGroup) {
		for (int i = 0; i < viewGroup.getChildCount(); i++) {
			View child = viewGroup.getChildAt(i);
			if (child instanceof ImageView && child.getId() == search_mag_icon) {
				return (ImageView) child;
			} else if (child instanceof ViewGroup) {
				ImageView icon = findIconInViewHierarchy((ViewGroup) child);
				if (icon != null) {
					return icon;
				}
			}
		}
		return null;
	}


	public void setColorFilter(int color) {
		if (searchIcon != null) {
			searchIcon.setColorFilter(color);
		}
	}

	public void setColorFilter(ColorFilter cf) {
		if (searchIcon != null) {
			searchIcon.setColorFilter(cf);
		}
	}

	public void setColorFilter(int color, PorterDuff.Mode mode) {
		if (searchIcon != null) {
			searchIcon.setColorFilter(color, mode);
		}
	}
}
