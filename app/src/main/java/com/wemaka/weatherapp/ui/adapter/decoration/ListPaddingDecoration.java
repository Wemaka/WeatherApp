package com.wemaka.weatherapp.ui.adapter.decoration;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wemaka.weatherapp.util.math.UnitConverter;

public class ListPaddingDecoration extends RecyclerView.ItemDecoration {
	private final int padding;
	private final Orientation orientation;

	public ListPaddingDecoration(@NonNull Context context, int paddingDp, Orientation orientation) {
		this.padding = UnitConverter.dpToPx(context, paddingDp);
		this.orientation = orientation;
	}

	public ListPaddingDecoration(@NonNull Context context, int paddingDp) {
		this.padding = UnitConverter.dpToPx(context, paddingDp);
		this.orientation = Orientation.VERTICAL;
	}

	@Override
	public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
		final int itemPosition = parent.getChildAdapterPosition(view);
		final RecyclerView.Adapter adapter = parent.getAdapter();


		if (orientation.equals(Orientation.HORIZONTAL)) {
			outRect.right = padding;

			if ((adapter != null) && (itemPosition == adapter.getItemCount() - 1)) {
				outRect.right = 0;
			}
		}

		if (orientation.equals(Orientation.VERTICAL)) {
			outRect.bottom = padding;

			if ((adapter != null) && (itemPosition == adapter.getItemCount() - 1)) {
				outRect.bottom = 0;
			}
		}
	}

	public enum Orientation {
		HORIZONTAL,
		VERTICAL
	}
}
