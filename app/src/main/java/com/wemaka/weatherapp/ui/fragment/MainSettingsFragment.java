package com.wemaka.weatherapp.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.databinding.FragmentMainSettingsBinding;
import com.wemaka.weatherapp.ui.viewmodel.MainViewModel;

public class MainSettingsFragment extends Fragment {
	public static final String TAG = "MainSettingsFragment";
	private FragmentMainSettingsBinding binding;
	private MainViewModel model;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		binding = FragmentMainSettingsBinding.inflate(getLayoutInflater());

		getChildFragmentManager()
				.beginTransaction()
				.replace(binding.settingsContainer.getId(), SettingsFragment.newInstance())
				.commit();

		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.btnBack.setOnClickListener(v -> {
			backAnimation();
		});

		requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
				new OnBackPressedCallback(true) {
					@Override
					public void handleOnBackPressed() {
						backAnimation();
					}
				}
		);
	}

	private void backAnimation() {
		Fragment fragment = requireActivity().getSupportFragmentManager().findFragmentById(R.id.placeHolder);

		if (fragment != null && fragment.getView() != null) {
			Animator animator = AnimatorInflater.loadAnimator(requireContext(), R.animator.slide_right);
			animator.setTarget(fragment.getView().findViewById(R.id.swipeRefresh));
			animator.start();
		}

		requireActivity().getSupportFragmentManager().popBackStack();
	}

	public static MainSettingsFragment newInstance() {
		return new MainSettingsFragment();
	}
}