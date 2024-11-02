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

import java.util.List;

public class SearchMenuAdapter extends ListAdapter<PlaceInfo, SearchMenuAdapter.ViewHolder> {
	private ItemClickListener listener;

	public SearchMenuAdapter() {
		super(new Comparator());
	}

	@NonNull
	@Override
	public SearchMenuAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_search_menu, parent, false);
		return new SearchMenuAdapter.ViewHolder(view, listener);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		PlaceInfo placeInfo = getItem(position);

		holder.bindTo(placeInfo);
		holder.setListener(listener, placeInfo);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
		if (!payloads.isEmpty() && payloads.contains(PayloadType.UPDATE_LISTENER)) {
			holder.setListener(listener, getItem(position));
		} else {
			onBindViewHolder(holder, position);
		}
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView textView;
		private ItemClickListener listener;

		public ViewHolder(@NonNull View itemView, ItemClickListener listener) {
			super(itemView);
			this.textView = itemView.findViewById(R.id.tvLocationName);
			this.listener = listener;
		}

		@SuppressLint("SetTextI18n")
		public void bindTo(PlaceInfo item) {
			textView.setText(item.getToponymName() + ", " +
					(item.getAdminName1().isEmpty() ? "" : item.getAdminName1() + ", ") +
					item.getCountryName());
		}

		public void setListener(ItemClickListener listener, PlaceInfo item) {
			this.listener = listener;

			if (this.listener != null) {
				itemView.setOnClickListener(v -> {
					this.listener.click(item);
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

	public interface ItemClickListener {
		void click(PlaceInfo item);
	}

	private enum PayloadType {
		UPDATE_LISTENER
	}

	public void setOnItemClickListener(ItemClickListener listener) {
		this.listener = listener;

		notifyItemRangeChanged(0, getItemCount(), PayloadType.UPDATE_LISTENER);
	}
}