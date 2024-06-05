package com.example.ccp.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ccp.common.Common;
import com.example.ccp.common.retrofit.model.BuildingResponse;
import com.example.ccp.databinding.RcySearchBinding;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private List<BuildingResponse> items = new ArrayList<>();
    private final ItemCallback itemCallback;

    public SearchAdapter(ItemCallback itemCallback) {
        this.itemCallback = itemCallback;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update(List<BuildingResponse> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RcySearchBinding binding = RcySearchBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final RcySearchBinding binding;
        public ViewHolder(RcySearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(BuildingResponse item) {
            Common.log("item : " + item);
            binding.tvPlace.setText(item.name+" ("+item.address+")");
            binding.tvPlace.setOnClickListener(view ->
                itemCallback.selectItem(item));
        }
    }

    public interface ItemCallback { void selectItem(BuildingResponse item); }
}