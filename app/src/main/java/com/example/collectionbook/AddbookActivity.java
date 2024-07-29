package com.example.collectionbook;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.collectionbook.databinding.ActivityAddbookBinding;
import com.example.collectionbook.helper.FullScreenHelper;
import com.example.collectionbook.helper.LoadingDialog;
import com.google.android.gms.tasks.OnFailureListener;
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
import java.util.Objects;

public class AddbookActivity extends AppCompatActivity {

    private ActivityAddbookBinding binding;
    private FirebaseAuth firebaseAuth;

    private static final int PICK_FILE_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST = 2;
    private TextView textViewFileName;
    private String title = "", author = "", description = "", uploadDate = "";
    private Uri fileUri = null;
    private Uri coverUri = null;

    private Dialog customDialog;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2;

    private String selectedCategoryId, selectedCategoryTitle;
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    private static final int STORAGE_PERMISSION_CODE = 23;

    private LoadingDialog loadingDialog;

    private ActivityResultLauncher<Intent> storageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                // Android is 11 (R) or above
                                if (Environment.isExternalStorageManager()) {
                                    // Manage External Storage Permissions Granted
                                    Log.d("storage-permission", "onActivityResult: Manage External Storage Permissions Granted");
                                    Toast.makeText(AddbookActivity.this, "Storage Permissions Granted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AddbookActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Below android 11
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddbookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingDialog = new LoadingDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FullScreenHelper.makeFullScreen(this);

        binding.backBtn.setOnClickListener(v -> {
            startActivity(new Intent(AddbookActivity.this, MainActivity.class));
            finish();
        });

        binding.btnSaveBook.setOnClickListener(v -> {
            validateData();
        });

        binding.inputCategory.setOnClickListener(v -> {
            if (categoryTitleArrayList.isEmpty()) {
                Toast.makeText(this, "Categories are loading, please wait...", Toast.LENGTH_SHORT).show();
            } else {
                categoryPickDialog();
            }
        });

        binding.inputDate.setOnClickListener(v -> {
            showPickerDialog();
        });

        // Initialize the custom dialog
        initCustomDialog();

        // Open modal on click
        binding.openModalCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog.show();
            }
        });

        // Check storage permission on runtime
        checkStoragePermission();

        FrameLayout frameUploadBox = findViewById(R.id.frame_upload_box);
        textViewFileName = findViewById(R.id.textview_file_name);

        frameUploadBox.setOnClickListener(v -> openFilePicker());

        // Add onClickListener for cover image picker
        binding.frameUploadBox2.setOnClickListener(v -> openImagePicker());
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android is 11 (R) or above
            if (!Environment.isExternalStorageManager()) {
                requestForStoragePermissions();
            }
        } else {
            // Below Android 11
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void requestForStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        } else {
            // Below Android 11
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    STORAGE_PERMISSION_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showPickerDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_year_picker, null);

        final NumberPicker yearPicker = view.findViewById(R.id.year_picker);
        final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(1000);
        yearPicker.setMaxValue(currentYear);
        yearPicker.setValue(currentYear);
        yearPicker.setDisplayedValues(null);
        yearPicker.setWrapSelectorWheel(false);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setTitle("Choose Year Published");
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            int selectedYear = yearPicker.getValue();
            binding.inputDate.setText(String.valueOf(selectedYear));
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // open dialog to showing list of category
    private void categoryPickDialog() {
        Log.d("show-category", "categoryPickDialog: Showing category pick dialog");

        //get string array of categories from arrayList
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i < categoryTitleArrayList.size(); i++) {
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, (dialog, which) -> {
                    //handle item click
                    //get clicked item from dialog list
                    selectedCategoryTitle = categoryTitleArrayList.get(which);
                    selectedCategoryId = categoryIdArrayList.get(which);
                    //set to category textview
                    binding.inputCategory.setText(selectedCategoryTitle);
                })
                .show();
    }

    private void loadPdfCategories() {
        Log.d("show-category", "loadPdfCategories: Loading pdf categories...");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        // DB reference to load categories ... db > Categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear(); // clear before adding data
                categoryIdArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // get id and title of category
                    String categoryId = "" + ds.child("id").getValue();
                    String categoryTitle = "" + ds.child("category").getValue();

                    // add to respective arraylists
                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);

                    Log.d("show-category", "onDataChange: " + categoryTitle);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // method to open the file picker
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_FILE_REQUEST);
    }

    // method to open the image picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_FILE_REQUEST) {
                fileUri = data.getData();
                String displayName = getFileName(fileUri);
                textViewFileName.setText(displayName);
            } else if (requestCode == PICK_IMAGE_REQUEST) {
                    coverUri = data.getData();
                String displayName = getFileName(coverUri);
                binding.textviewFileName2.setText(displayName);
            }
        }
    }

    // method to get the file name from the Uri
    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void validateData() {
        title = binding.etTitle.getText().toString().trim();
        author = binding.etAuthor.getText().toString().trim();
        description = binding.etDescription.getText().toString().trim();
        uploadDate = binding.inputDate.getText().toString().trim();

        if (title.isEmpty() || author.isEmpty() || description.isEmpty() || uploadDate.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
        } else if (fileUri == null) {
            Toast.makeText(this, "Please select a PDF file", Toast.LENGTH_SHORT).show();
        } else if (coverUri == null) {
            Toast.makeText(this, "Please select a cover image", Toast.LENGTH_SHORT).show();
        } else {
            uploadCoverImageToStorage();
        }
    }

    private void uploadCoverImageToStorage() {
        loadingDialog.show();

        String timestamp = "" + System.currentTimeMillis();
        String filePathAndName = "Covers/" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(coverUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    String uploadedCoverImageUrl = "" + uriTask.getResult();
                    uploadPdfToStorage(uploadedCoverImageUrl, timestamp);
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(AddbookActivity.this, "Cover image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadPdfToStorage(String uploadedCoverImageUrl, String timestamp) {
        String filePathAndName = "Books/" + timestamp;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    String uploadedPdfUrl = "" + uriTask.getResult();
                    uploadPdfInfoToDb(uploadedPdfUrl, uploadedCoverImageUrl, timestamp);
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(AddbookActivity.this, "PDF upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadPdfInfoToDb(String uploadedPdfUrl, String uploadedCoverImageUrl, String timestamp) {
        String uid = Objects.requireNonNull(firebaseAuth.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("id", timestamp);
        hashMap.put("timestamp", timestamp);
        hashMap.put("title", title);
        hashMap.put("author", author);
        hashMap.put("description", description);
        hashMap.put("categoryId", selectedCategoryId);
        hashMap.put("uploadDate", uploadDate);
        hashMap.put("url", uploadedPdfUrl);
        hashMap.put("coverUrl", uploadedCoverImageUrl);
        hashMap.put("accessCount", 0);
        hashMap.put("downloadCount", 0);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    loadingDialog.dismiss();
                    Toast.makeText(AddbookActivity.this, "Book uploaded successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AddbookActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(AddbookActivity.this, "Failed to upload book info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void initCustomDialog() {
        customDialog = new Dialog(this);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.custom_dialog_add_category);
        Objects.requireNonNull(customDialog.getWindow()).setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        customDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        customDialog.setCancelable(true);

        ImageButton buttonModalCategory = findViewById(R.id.openModalCategory);
        TextView inputModalCategory = findViewById(R.id.inputCategory);

        buttonModalCategory.setOnClickListener(view -> {
            String category = inputModalCategory.getText().toString().trim();
            if (category.isEmpty()) {
                Toast.makeText(AddbookActivity.this, "Category cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                saveCategoryToFirebase(category);
            }
        });
    }

    private void saveCategoryToFirebase(String category) {
        long timestamp = System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + timestamp);
        hashMap.put("category", "" + category);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", "" + firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    Log.d("add-category", "saveCategoryToFirebase: Category successfully added");
                    binding.inputCategory.setText("");
                    customDialog.dismiss();
                    loadPdfCategories();
                })
                .addOnFailureListener(e -> {
                    Log.e("add-category", "saveCategoryToFirebase: Category addition failed due to " + e.getMessage());
                    customDialog.dismiss();
                });
    }
}
