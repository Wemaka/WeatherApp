package com.wemaka.weatherapp;

import static com.wemaka.weatherapp.activity.MainActivity.TAG;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.res.ResourcesCompat;

import java.lang.reflect.Field;

public class MySearchView extends SearchView {
	private ImageView searchIcon;
	private ImageView searchClose;
	private EditText searchText;

	public MySearchView(@NonNull Context context) {
		super(context);
		init();
	}

	public MySearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MySearchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}


	private void init() {
		searchIcon = findViewById(androidx.appcompat.R.id.search_button);
		searchText = findViewById(androidx.appcompat.R.id.search_src_text);
		searchClose = findViewById(androidx.appcompat.R.id.search_close_btn);

		setBackgroundResource(R.drawable.block_background_tab);
		setColorFilter(R.color.white);
		searchText.setTextColor(getResources().getColor(R.color.black, null));
		searchClose.setColorFilter(R.color.black);
		searchIcon.setColorFilter(R.color.black);

//		setOnSearchClickListener(null);
//
//		setOnCloseListener(null);
	}

	@Override
	public void setOnSearchClickListener(OnClickListener listener) {
		super.setOnSearchClickListener(v -> {
//			setBackgroundResource(R.drawable.block_background_tab);

			if (listener != null) {
				listener.onClick(v);
			}
		});
	}

	@Override
	public void setOnCloseListener(OnCloseListener listener) {
		super.setOnCloseListener(() -> {
//			setBackground(null);

			if (listener != null) {
				listener.onClose();
			}

			return false;
		});
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
