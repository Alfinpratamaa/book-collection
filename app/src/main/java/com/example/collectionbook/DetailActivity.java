package com.example.collectionbook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.collectionbook.helper.LoadingDialog;
import com.example.collectionbook.model.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DetailActivity extends AppCompatActivity {

    private PDFView pdfView;
    private TextView bookTitleTextView;
    private TextView bookAuthorTextView;
    private TextView bookDescriptionTextView;
    private TextView viewsCount;
    private Button readNowButton;
    private ImageButton editButton, bckBtn,bookmarkButton;
    private ImageButton deleteButton;
    private ImageView coverImage;

    private static final long MAX_BYTES_PDF = 10485760; // 10MB
    private static final String TAG = "DetailActivity";

    private String bookId;
    private String bookPdfUrl;

    private boolean isBookmarked = false;
    private DatabaseReference userRef;



    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        loadingDialog = new LoadingDialog(this);

        // Initialize views
        coverImage = findViewById(R.id.coverImage);
        bookTitleTextView = findViewById(R.id.bookTitleTextView);
        bookAuthorTextView = findViewById(R.id.bookAuthorTextView);
        bookDescriptionTextView = findViewById(R.id.bookDescriptionTextView);
        readNowButton = findViewById(R.id.btnRead);
        editButton = findViewById(R.id.editButton);
        deleteButton = findViewById(R.id.deleteButton);
        bckBtn = findViewById(R.id.backBtn);
        bookmarkButton = findViewById(R.id.bookmark);
        viewsCount = findViewById(R.id.viewsCount);;

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

            bookmarkButton.setOnClickListener(v -> {
                if (isBookmarked) {
                    bookmarkButton.setImageResource(R.drawable.bookmark_main);
                    removeBookmark();
                } else {
                    addBookmark();
                }
            });
        }

        // Get book ID from intent
        bookId = getIntent().getStringExtra("bookId");

        if (bookId != null) {
            // Fetch book data
            fetchBookData();
        } else {
            Log.e(TAG, "Book ID is missing");
            Toast.makeText(this, "Book ID is missing", Toast.LENGTH_SHORT).show();
        }

        bckBtn.setOnClickListener(v -> {
            startActivity(new Intent(DetailActivity.this, MainActivity.class));
            finish();
        });

        // Set button click listeners
        readNowButton.setOnClickListener(v -> {
            // Handle read now action
            Intent intent = new Intent(DetailActivity.this, ReadActivity.class);
            intent.putExtra("bookId", bookId);
            intent.putExtra("bookPdfUrl", bookPdfUrl);
            startActivity(intent);
        });

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, EditBookActivity.class);
            intent.putExtra("bookId", bookId);
            startActivity(intent);
        });

        deleteButton.setOnClickListener(v -> {
            deleteBook();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the book data when the activity resumes
        if (bookId != null) {
            fetchBookData();
        }
    }

    private void fetchBookData() {
        loadingDialog.show();
        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("Books").child(bookId);
        bookRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ModelPdf book = snapshot.getValue(ModelPdf.class);
                    if (book != null) {
                        // Update UI with book data
                        loadingDialog.dismiss();
                        bookTitleTextView.setText(book.getTitle());
                        bookAuthorTextView.setText(book.getAuthor());
                        viewsCount.setText(String.valueOf(book.getAccessCount()));
                        bookDescriptionTextView.setText(book.getDescription());
                        bookPdfUrl = book.getUrl();
                        Glide.with(DetailActivity.this)
                                .load(book.getCoverUrl())
                                .placeholder(R.drawable.no_cover) // Placeholder image
                                .error(R.drawable.no_cover) // Error image
                                .into(coverImage);



                        // Check if the book is bookmarked
                        if (userRef != null) {
                            checkIfBookmarked();
                        }
                    } else {
                        loadingDialog.dismiss();
                        Log.e(TAG, "Book data is null");
                        Toast.makeText(DetailActivity.this, "Book data is null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    loadingDialog.dismiss();
                    Log.e(TAG, "No book found with the given ID");
                    Toast.makeText(DetailActivity.this, "No book found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, "Failed to fetch book data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfBookmarked() {
        if (bookId == null) {
            Log.e("bookId : ", "Book ID is missing :");
            Toast.makeText(DetailActivity.this, "Book ID is missing :"+bookId, Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.child("bookmarks").child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isBookmarked = snapshot.exists();
                updateBookmarkButton();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, "Failed to check bookmark status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addBookmark() {
        userRef.child("bookmarks").child(bookId).setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                isBookmarked = true;
                updateBookmarkButton();
                Toast.makeText(DetailActivity.this, "Bookmarked", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DetailActivity.this, "Failed to bookmark", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeBookmark() {
        userRef.child("bookmarks").child(bookId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                isBookmarked = false;
                updateBookmarkButton();
                Toast.makeText(DetailActivity.this, "Bookmark removed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DetailActivity.this, "Failed to remove bookmark", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBookmarkButton() {
        if (isBookmarked) {
            bookmarkButton.setImageResource(R.drawable.bookmark_main);
        } else {
            bookmarkButton.setImageResource(R.drawable.unbookmark_main);
        }
    }



    private void deleteBook() {
        loadingDialog.show();
        if (bookPdfUrl != null && !bookPdfUrl.isEmpty()) {
            StorageReference pdfRef = FirebaseStorage.getInstance().getReferenceFromUrl(bookPdfUrl);
            pdfRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "PDF deleted successfully");
                        Toast.makeText(this, "Book Deleted", Toast.LENGTH_SHORT).show();
                        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("Books").child(bookId);
                        bookRef.removeValue()
                                .addOnSuccessListener(aVoid1 -> {
                                    loadingDialog.dismiss();
                                    Log.d(TAG, "Book data deleted successfully");
                                    startActivity(new Intent(DetailActivity.this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    loadingDialog.dismiss();
                                    Log.d(TAG, "Failed to delete book data: " + e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "Failed to delete PDF: " + e.getMessage());
                        loadingDialog.dismiss();
                    });
        } else {
            DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("Books").child(bookId);
            bookRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        loadingDialog.dismiss();
                        Log.d(TAG, "Book data deleted successfully");
                        finish();
                    })
                    .addOnFailureListener(e -> Log.d(TAG, "Failed to delete book data: " + e.getMessage()));
        }
    }
}

