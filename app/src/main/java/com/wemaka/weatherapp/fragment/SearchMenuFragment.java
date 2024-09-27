package com.wemaka.weatherapp.fragment;

import static com.wemaka.weatherapp.activity.MainActivity.TAG;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.databinding.FragmentSearchMenuBinding;

import java.util.ArrayList;

import eightbitlab.com.blurview.BlurViewFacade;
import eightbitlab.com.blurview.RenderEffectBlur;
import eightbitlab.com.blurview.RenderScriptBlur;


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

		ArrayList<String> lst = new ArrayList<>();
		lst.add("Moscow");
		lst.add("Sankt Peterburg");
		lst.add("Perm");

		binding.searchList.setAdapter(new ArrayAdapter<>(requireContext(),
				android.R.layout.simple_list_item_1, lst));
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
