package com.example.collectionbook.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.collectionbook.R;

public class SortAdapter extends RecyclerView.Adapter<SortAdapter.SortViewHolder> {

    private final String[] sortCharacters;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String character);
    }

    public SortAdapter(String[] sortCharacters, OnItemClickListener listener) {
        this.sortCharacters = sortCharacters;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SortViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sort_list, parent, false);
        return new SortViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SortViewHolder holder, int position) {
        String character = sortCharacters[position];
        holder.characterTv.setText(character);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(character));
    }

    @Override
    public int getItemCount() {
        return sortCharacters.length;
    }

    public static class SortViewHolder extends RecyclerView.ViewHolder {

        TextView characterTv;

        public SortViewHolder(@NonNull View itemView) {
            super(itemView);
            characterTv = itemView.findViewById(R.id.charTv);
        }
    }
}
