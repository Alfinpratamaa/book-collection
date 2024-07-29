package com.example.collectionbook;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ReadActivity extends AppCompatActivity {

    private PDFView pdfView;
    private TextView pageInfoTextView;

    private String bookId;
    private String bookPdfUrl;
    private int lastReadPage = 0;
    private static final long MAX_BYTES_PDF = 10485760; // 10MB

    private static final String PREFS_NAME = "ReadPrefs";
    private static final String KEY_LAST_PAGE = "LastPage_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        // Initialize views
        pdfView = findViewById(R.id.pdfView);
        pageInfoTextView = findViewById(R.id.pageInfoTextView);

        // Get book ID and PDF URL from intent
        bookId = getIntent().getStringExtra("bookId");
        bookPdfUrl = getIntent().getStringExtra("bookPdfUrl");

        // Load last read page from SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        lastReadPage = preferences.getInt(KEY_LAST_PAGE + bookId, 0);

        // Show resume reading dialog if lastReadPage > 0
        if (lastReadPage > 0) {
            showResumeReadingDialog();
        } else {
            loadPdfFromUrl(bookPdfUrl, 0);
        }
    }

    private void showResumeReadingDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Resume Reading")
                .setMessage("Would you like to resume reading from where you left off?")
                .setPositiveButton("Yes", (dialog, which) -> loadPdfFromUrl(bookPdfUrl, lastReadPage))
                .setNegativeButton("No", (dialog, which) -> loadPdfFromUrl(bookPdfUrl, 0))
                .show();
    }

    private void loadPdfFromUrl(String pdfUrl, int pageNumber) {
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(bytes -> {
                    pdfView.fromBytes(bytes)
                            .defaultPage(pageNumber)
                            .onPageChange((page, pageCount) -> {
                                pageInfoTextView.setText(String.format("Page %d / %d", page + 1, pageCount));
                                saveLastReadPage(page);
                            })
                            .load();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ReadActivity.this, "Failed to load PDF", Toast.LENGTH_SHORT).show();
                    Log.e("ReadActivity", "Failed to load PDF: " + e.getMessage());
                });
    }

    private void saveLastReadPage(int page) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_LAST_PAGE + bookId, page);
        editor.apply();
    }
}
