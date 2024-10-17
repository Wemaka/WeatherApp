package com.wemaka.weatherapp.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wemaka.weatherapp.R;
import com.wemaka.weatherapp.data.PlaceInfo;

public class SearchMenuAdapter extends ListAdapter<PlaceInfo, SearchMenuAdapter.ViewHolder> {
	private final ClickListener listener;

	public SearchMenuAdapter() {
		this(null);
	}

	public SearchMenuAdapter(ClickListener listener) {
		super(new SearchMenuAdapter.Comparator());
		this.listener = listener;
	}

	@NonNull
	@Override
	public SearchMenuAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_search_menu, parent, false);
		return new SearchMenuAdapter.ViewHolder(view, listener);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.bindTo(getItem(position));
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView textView;
		private ClickListener listener;

		public ViewHolder(@NonNull View itemView, ClickListener listener) {
			super(itemView);
			this.textView = itemView.findViewById(R.id.tvLocationName);
			this.listener = listener;
		}


		public void bindTo(PlaceInfo item) {
			textView.setText(item.getToponymName() + ", " +
					(item.getAdminName1().isEmpty() ? "" : item.getAdminName1() + ", ") +
					item.getCountryName());

			if (listener != null) {
				itemView.setOnClickListener(v -> {
					listener.click(item);
				});
			}
		}
	}

	public static class Comparator extends DiffUtil.ItemCallback<PlaceInfo> {
		@Override
		public boolean areItemsTheSame(@NonNull PlaceInfo oldItem, @NonNull PlaceInfo newItem) {
			return oldItem == newItem;
		}

		@SuppressLint("DiffUtilEquals")
		@Override
		public boolean areContentsTheSame(@NonNull PlaceInfo oldItem, @NonNull PlaceInfo newItem) {
			return oldItem == newItem;
		}
	}

	public interface ClickListener {
		void click(PlaceInfo item);
	}
}