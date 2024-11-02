package com.wemaka.weatherapp.ui.view;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import com.wemaka.weatherapp.R;

public class CustomSearchView extends SearchView {
	private ImageView searchIcon;
	private ImageView searchClose;
	private EditText searchText;

	public CustomSearchView(@NonNull Context context) {
		super(context);
		init();
	}

	public CustomSearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CustomSearchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
	}

	@Override
	public void setOnSearchClickListener(OnClickListener listener) {
		super.setOnSearchClickListener(v -> {
			if (listener != null) {
				listener.onClick(v);
			}
		});
	}

	@Override
	public void setOnCloseListener(OnCloseListener listener) {
		super.setOnCloseListener(() -> {
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
