package com.wemaka.weatherapp.ui.activity;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.wemaka.weatherapp.api.LocationService;
import com.wemaka.weatherapp.databinding.ActivityMainBinding;
import com.wemaka.weatherapp.repository.WeatherForecastRepository;
import com.wemaka.weatherapp.ui.fragment.MainFragment;
import com.wemaka.weatherapp.viewmodel.MainViewModel;
import com.wemaka.weatherapp.viewmodel.MainViewModelProviderFactory;
import com.zeugmasolutions.localehelper.LocaleHelper;

import lombok.Getter;

public class MainActivity extends AppCompatActivity {
	public static final String TAG = "MainActivity";
	private ActivityMainBinding binding;
	@Getter
	private MainViewModel model;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		binding = ActivityMainBinding.inflate(getLayoutInflater());

		setContentView(binding.getRoot());

		LocationService locationService = new LocationService(this);
		WeatherForecastRepository repository = new WeatherForecastRepository(locationService);
		MainViewModelProviderFactory viewModelProviderFactory =
				new MainViewModelProviderFactory(repository, this.getApplication());

		model = new ViewModelProvider(this, viewModelProviderFactory).get(MainViewModel.class);

		getSupportFragmentManager()
				.beginTransaction()
				.replace(binding.placeHolder.getId(), MainFragment.newInstance())
				.commit();

		ViewCompat.setOnApplyWindowInsetsListener(binding.placeHolder, (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
			return insets;
		});
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.INSTANCE.onAttach(base));
	}
}