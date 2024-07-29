package com.example.collectionbook.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.collectionbook.DetailActivity;
import com.example.collectionbook.R;
import com.example.collectionbook.model.ModelPdf;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {

    private List<ModelPdf> bookList;
    private Context context;

    public ListAdapter(Context context, List<ModelPdf> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fragment_list, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        ModelPdf book = bookList.get(position);
        holder.titleTv.setText(book.getTitle());
        holder.authorTv.setText(book.getAuthor());

        // Load image into ImageView using Glide
        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(context)
                    .load(book.getCoverUrl())
                    .error(R.drawable.no_cover)
                    .placeholder(R.drawable.no_cover) // Fallback image
                    .into(holder.coverImage);
        } else {
            holder.coverImage.setImageResource(R.drawable.no_cover); // Fallback image
        }

        // Set onClickListener to itemView
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("bookTitle", book.getTitle());
            intent.putExtra("bookAuthor", book.getAuthor());
            intent.putExtra("bookDescription", book.getDescription());
            intent.putExtra("bookImageUrl", book.getUrl());
            intent.putExtra("bookId", book.getId());
            intent.putExtra("datePublished", book.getUploadDate());
            intent.putExtra("category", book.getCategoryId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {

        TextView titleTv, authorTv;
        ImageView coverImage;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.titleTv);
            authorTv = itemView.findViewById(R.id.authorTv);
            coverImage = itemView.findViewById(R.id.coverImage);
        }
    }
}
