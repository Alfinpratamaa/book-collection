package com.example.collectionbook;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.collectionbook.helper.LoadingDialog;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;

    private ImageView imgView;
    private EditText nameEt, emailEt;
    private Button saveBtn;
    private ImageButton backBtn;
    private Uri imageUri;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private LoadingDialog loadingDialog;
    private TextView cantEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        loadingDialog = new LoadingDialog(this);

        imgView = findViewById(R.id.imgView);
        nameEt = findViewById(R.id.nameEt);
        emailEt = findViewById(R.id.emailEt);
        saveBtn = findViewById(R.id.btnSave);
        backBtn = findViewById(R.id.backBtn);
        cantEdit = findViewById(R.id.cantEdit);

        emailEt.setOnClickListener(v->{
            cantEdit.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    cantEdit.setVisibility(View.GONE);
                }
            },2000);
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            storageReference = FirebaseStorage.getInstance().getReference().child("imageUser").child(userId).child("imagefile");

            loadProfile(userId);

            imgView.setOnClickListener(v -> {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_IMAGE);
            });

            saveBtn.setOnClickListener(v -> saveProfile(userId));
            backBtn.setOnClickListener(v -> finish());
        }
    }

    private void loadProfile(String userId) {
        loadingDialog.show();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingDialog.dismiss();
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String profileImage = snapshot.child("profileImage").getValue(String.class);

                    nameEt.setText(name);
                    emailEt.setText(email);
                    if (profileImage != null && !profileImage.isEmpty()) {
                        Picasso.get().load(profileImage).into(imgView);
                    }
                } else {
                    loadingDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Uknown Error :(", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
                loadingDialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imgView.setImageURI(imageUri);
        }
    }

    private void saveProfile(String userId) {
        loadingDialog.show();
        String name = nameEt.getText().toString();
        databaseReference.child("name").setValue(name);

        if (imageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = storageReference.putBytes(data);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                                databaseReference.child("profileImage").setValue(downloadUrl).addOnCompleteListener(task -> {
                                    loadingDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(EditProfileActivity.this, "Success update profile!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(EditProfileActivity.this, "Failed to update profile image URL in database", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingDialog.dismiss();
                                Toast.makeText(EditProfileActivity.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                loadingDialog.dismiss();
                e.printStackTrace();
                Toast.makeText(EditProfileActivity.this, "Failed to process image", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Check if profile image already exists in the database
            databaseReference.child("profileImage").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    loadingDialog.dismiss();
                    if (snapshot.exists()) {
                        // Profile image exists, only update the name
                        Toast.makeText(EditProfileActivity.this, "Success update profile!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    loadingDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Failed to check profile image existence", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}
