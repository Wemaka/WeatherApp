package com.wemaka.weatherapp.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.wemaka.weatherapp.databinding.ActivityMainBinding;
import com.wemaka.weatherapp.ui.fragment.MainFragment;
import com.wemaka.weatherapp.ui.viewmodel.MainViewModel;
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity;

import dagger.hilt.android.AndroidEntryPoint;
import lombok.Getter;

@AndroidEntryPoint
public class MainActivity extends LocaleAwareCompatActivity {
	public static final String TAG = "MainActivity";
	private ActivityMainBinding binding;
	@Getter
	private MainViewModel model;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		model = new ViewModelProvider(this).get(MainViewModel.class);

		if (savedInstanceState == null) {
			getSupportFragmentManager()
					.beginTransaction()
					.replace(binding.placeHolder.getId(), MainFragment.newInstance())
					.commit();
		}

		ViewCompat.setOnApplyWindowInsetsListener(binding.placeHolder, (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
			return insets;
		});
	}
}