    package com.example.collectionbook.adapter;

    import android.content.Context;
    import android.content.Intent;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.ProgressBar;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;

    import com.bumptech.glide.Glide;
    import com.example.collectionbook.DetailActivity;
    import com.example.collectionbook.R;
    import com.example.collectionbook.helper.LoadingDialog;
    import com.example.collectionbook.model.ModelPdf;
    import com.github.barteksc.pdfviewer.PDFView;
    import com.google.android.gms.tasks.OnSuccessListener;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;
    import com.google.firebase.storage.FirebaseStorage;
    import com.google.firebase.storage.StorageReference;

    import java.util.List;

    public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookViewHolder> {

        private List<ModelPdf> bookList;
        private static final long MAX_BYTES_PDF = 10485760;
        private static final String TAG = "BookmarkAdapter";
        private Context context;

        public BookmarkAdapter(Context context, List<ModelPdf> bookList) {
            this.context = context;
            this.bookList = bookList;
        }

        @NonNull
        @Override
        public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookmark, parent, false);
            return new BookViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
            ModelPdf book = bookList.get(position);
            holder.titleTv.setText(book.getTitle());
            holder.authorTv.setText(book.getAuthor());
            holder.dateTv.setText(book.getUploadDate());

            // Load category based on categoryId
            loadCategory(book.getCategoryId(), holder.categoryTv);

            Glide.with(context)
                    .load(book.getCoverUrl())
                    .placeholder(R.drawable.no_cover) // Placeholder image
                    .error(R.drawable.no_cover) // Error image
                    .into(holder.coverImage);



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

        public static class BookViewHolder extends RecyclerView.ViewHolder {

            TextView titleTv, authorTv, dateTv, categoryTv;
            ImageView coverImage;

            public BookViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTv = itemView.findViewById(R.id.titleTv);
                authorTv = itemView.findViewById(R.id.authorTv);
                dateTv = itemView.findViewById(R.id.dateTv);
                categoryTv = itemView.findViewById(R.id.categoryTv);
                coverImage = itemView.findViewById(R.id.coverImage);
            }
        }



        private void loadCategory(String categoryId, TextView categoryTv) {
            DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference().child("Categories").child(categoryId);
            categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String category = snapshot.child("category").getValue(String.class);
                        if (category != null) {
                            categoryTv.setText(category);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "onCancelled: " + error.getMessage());
                }
            });
        }
    }
