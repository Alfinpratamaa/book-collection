package com.example.collectionbook;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.collectionbook.helper.LoadingDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class EditBookActivity extends AppCompatActivity {

    private EditText etTitle, etAuthor, etDescription;
    private TextView tvUploadDate, tvCategory, tvFileName, tvFileName2;
    private Button btnUpdate;
    private FrameLayout frameUploadBox, frameUploadBox2;
    private Uri pdfUri, imageUri;
    private String bookId;

    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;
    private String selectedCategoryId, selectedCategoryTitle;

    private FirebaseAuth firebaseAuth;
    private static final int PDF_PICK_CODE = 1000;
    private static final int IMAGE_PICK_CODE = 1001;

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editbook);

        loadingDialog = new LoadingDialog(this);

        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        etDescription = findViewById(R.id.etDescription);
        tvUploadDate = findViewById(R.id.inputDate);
        tvCategory = findViewById(R.id.inputCategory);
        tvFileName = findViewById(R.id.textview_file_name);
        tvFileName2 = findViewById(R.id.textview_file_name2);
        btnUpdate = findViewById(R.id.btnSaveBook);
        frameUploadBox = findViewById(R.id.frame_upload_box);
        frameUploadBox2 = findViewById(R.id.frame_upload_box2);

        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> onBackPressed());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        firebaseAuth = FirebaseAuth.getInstance();

        loadPdfCategories();
        loadBookDetails();

        tvUploadDate.setOnClickListener(v -> showPickerDialog());
        tvCategory.setOnClickListener(v -> showCategoryPickDialog());

        frameUploadBox.setOnClickListener(v -> pickPdf());
        frameUploadBox2.setOnClickListener(v -> pickImage());

        btnUpdate.setOnClickListener(v -> validateAndUpdateBook());
    }

    private void loadPdfCategories() {
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String categoryId = "" + ds.child("id").getValue();
                    String categoryTitle = "" + ds.child("category").getValue();

                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("EditActivity", "onCancelled: " + error.getMessage());
            }
        });
    }

    private void loadBookDetails() {
        loadingDialog.show();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String title = "" + snapshot.child("title").getValue();
                    String author = "" + snapshot.child("author").getValue();
                    String description = "" + snapshot.child("description").getValue();
                    String uploadDate = "" + snapshot.child("uploadDate").getValue();
                    String categoryId = "" + snapshot.child("categoryId").getValue();
                    String pdfUri = "" + snapshot.child("url").getValue();
                    String imageUri = "" + snapshot.child("coverUrl").getValue();

                    etTitle.setText(title);
                    etAuthor.setText(author);
                    etDescription.setText(description);
                    tvUploadDate.setText(uploadDate);

                    // Load category title
                    DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("Categories");
                    categoryRef.child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot categorySnapshot) {
                            loadingDialog.dismiss();
                            if (categorySnapshot.exists()) {
                                String categoryTitle = "" + categorySnapshot.child("category").getValue();
                                tvCategory.setText(categoryTitle);
                                selectedCategoryId = categoryId;
                                selectedCategoryTitle = categoryTitle;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            loadingDialog.dismiss();
                            Log.e("EditActivity", "onCancelled: " + error.getMessage());
                        }
                    });

                    tvFileName.setText(pdfUri.isEmpty() ? "No file selected" : "File uploaded");
                    tvFileName2.setText(imageUri.isEmpty() ? "No file selected" : "Image uploaded");
                } else {
                    loadingDialog.dismiss();
                    Toast.makeText(EditBookActivity.this, "Book not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
                Log.e("EditActivity", "onCancelled: " + error.getMessage());
            }
        });
    }

    private void showPickerDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_year_picker, null);

        final NumberPicker yearPicker = view.findViewById(R.id.year_picker);
        final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(1000);
        yearPicker.setMaxValue(currentYear);
        yearPicker.setValue(tvUploadDate.getText().toString().isEmpty() ? currentYear : Integer.parseInt(tvUploadDate.getText().toString()));
        yearPicker.setDisplayedValues(null);
        yearPicker.setWrapSelectorWheel(false);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setTitle("Choose Year Published");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedYear = yearPicker.getValue();
                tvUploadDate.setText(String.valueOf(selectedYear));
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showCategoryPickDialog() {
        Log.d("EditActivity", "showCategoryPickDialog: Showing category pick dialog");

        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i < categoryTitleArrayList.size(); i++) {
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, (dialog, which) -> {
                    selectedCategoryTitle = categoryTitleArrayList.get(which);
                    selectedCategoryId = categoryIdArrayList.get(which);
                    tvCategory.setText(selectedCategoryTitle);
                })
                .show();
    }

    private void pickPdf() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, PDF_PICK_CODE);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PDF_PICK_CODE && data != null && data.getData() != null) {
                pdfUri = data.getData();
                tvFileName.setText(pdfUri.getLastPathSegment());
            } else if (requestCode == IMAGE_PICK_CODE && data != null && data.getData() != null) {
                imageUri = data.getData();
                tvFileName2.setText(imageUri.getLastPathSegment());
            } else {
                if (requestCode == PDF_PICK_CODE) {
                    tvFileName.setText("No file selected");
                } else if (requestCode == IMAGE_PICK_CODE) {
                    tvFileName2.setText("No file selected");
                }
            }
        }
    }

    private void validateAndUpdateBook() {
        String title = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String uploadDate = tvUploadDate.getText().toString().trim();
        String category = tvCategory.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }

        if (author.isEmpty()) {
            etAuthor.setError("Author is required");
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            return;
        }

        if (uploadDate.isEmpty()) {
            tvUploadDate.setError("Upload date is required");
            return;
        }

        if (category.isEmpty()) {
            tvCategory.setError("Category is required");
            return;
        }

        loadingDialog.show();

        if (pdfUri == null && imageUri == null) {
            updateBookToDb("");
        } else if (pdfUri != null) {
            uploadFileToFirebase("pdf", pdfUri, url -> {
                updateBookToDb(url);
            });
        } else {
            uploadFileToFirebase("image", imageUri, url -> {
                updateBookToDb(url);
            });
        }
    }

    private void uploadFileToFirebase(String fileType, Uri fileUri, OnSuccessListener<String> onSuccessListener) {
        String filePathAndName = "Books/" + fileType + "_" + firebaseAuth.getUid() + "_" + System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    String uploadedFileUrl = uriTask.getResult().toString();
                    onSuccessListener.onSuccess(uploadedFileUrl);
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(EditBookActivity.this, "Upload failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBookToDb(String uploadedFileUrl) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", etTitle.getText().toString().trim());
        hashMap.put("author", etAuthor.getText().toString().trim());
        hashMap.put("description", etDescription.getText().toString().trim());
        hashMap.put("uploadDate", tvUploadDate.getText().toString().trim());
        hashMap.put("categoryId", selectedCategoryId);

        if (!uploadedFileUrl.isEmpty()) {
            if (pdfUri != null) {
                hashMap.put("url", uploadedFileUrl);
            } else if (imageUri != null) {
                hashMap.put("coverUrl", uploadedFileUrl);
            }
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).updateChildren(hashMap)
                .addOnSuccessListener(aVoid -> {
                    loadingDialog.dismiss();
                    Toast.makeText(EditBookActivity.this, "Book updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(EditBookActivity.this, "Failed to update book due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
