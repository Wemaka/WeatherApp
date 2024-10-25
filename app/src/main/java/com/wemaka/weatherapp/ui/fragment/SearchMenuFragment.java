package com.wemaka.weatherapp.ui.fragment;

import static com.wemaka.weatherapp.ui.activity.MainActivity.TAG;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.adapter.SearchMenuAdapter;
import com.wemaka.weatherapp.api.GeoNamesClient;
import com.wemaka.weatherapp.data.store.ProtoDataStoreRepository;
import com.wemaka.weatherapp.databinding.FragmentSearchMenuBinding;
import com.wemaka.weatherapp.store.proto.LocationCoordProto;

import eightbitlab.com.blurview.BlurViewFacade;
import eightbitlab.com.blurview.RenderEffectBlur;
import eightbitlab.com.blurview.RenderScriptBlur;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class SearchMenuFragment extends BottomSheetDialogFragment {
	private FragmentSearchMenuBinding binding;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		binding = FragmentSearchMenuBinding.inflate(getLayoutInflater());
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		addBlurBackground();

		SearchMenuAdapter searchMenuAdapter = new SearchMenuAdapter();
		searchMenuAdapter.setOnItemClickListener(item -> {
			Log.i(TAG, "Click: " + item.getLatitude() + " : " + item.getLongitude());

			ProtoDataStoreRepository.getInstance().saveLocationCoord(
					new LocationCoordProto(
							Double.parseDouble(item.getLatitude()),
							Double.parseDouble(item.getLongitude())
					)
			);

			dismiss();
		});

		binding.rvSearchList.setAdapter(searchMenuAdapter);

		binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				Log.i(TAG, "Click request search");

				GeoNamesClient.searchLocation(query)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.doOnSuccess(searchMenuAdapter::submitList)
						.doOnError(error -> Log.e(TAG, "ERROR REQUEST: api.geonames " + error))
						.subscribe();

				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), getTheme());
		dialog.setOnShowListener(dialogInterface -> {
			BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
			View parentLayout = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
			if (parentLayout != null) {
				BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parentLayout);
				setupFullHeight(parentLayout);
				behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
				behavior.setSkipCollapsed(true);
			}
		});
		return dialog;
	}

	private void setupFullHeight(View bottomSheet) {
		ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
		layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
		bottomSheet.setLayoutParams(layoutParams);
	}

	private void addBlurBackground() {
		float radius = 20f;

		View decorView = requireActivity().getWindow().getDecorView();
		ViewGroup rootView = decorView.findViewById(android.R.id.content);
//		Drawable windowBackground = decorView.getBackground();

		BlurViewFacade blur;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			blur = binding.blurView.setupWith(rootView, new RenderEffectBlur());
		} else {
			blur = binding.blurView.setupWith(rootView, new RenderScriptBlur(requireContext()));
		}

		blur.setFrameClearDrawable(ResourcesCompat.getDrawable(getResources(),
				R.drawable.block_blur_rounded_background, null)).setBlurRadius(radius);

		binding.blurView.setBackgroundResource(R.drawable.block_blur_rounded_background);

		binding.blurView.setClipToOutline(true);
	}
}
