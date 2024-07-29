package com.example.collectionbook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.collectionbook.databinding.ActivityRegisterBinding;
import com.example.collectionbook.helper.FullScreenHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;

    private String name, email, password, confirmPassword;
    private boolean isPasswordVisible = false;
    private boolean isCPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        FullScreenHelper.makeFullScreen(this);

        showLoading(false);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.navToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });

        binding.btnRegister.setOnClickListener(v -> {
            validateUser();
        });
        setDrawableEndClickListener(binding.etPassword, true);
        setDrawableEndClickListener(binding.etCpassword, false);
    }

    private void validateUser() {
        name = binding.etName.getText().toString().trim();
        email = binding.eTEmail.getText().toString().trim();
        password = binding.etPassword.getText().toString().trim();
        confirmPassword = binding.etCpassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
        } else {
            createAccount();
        }
    }

    private void createAccount() {
        showLoading(true);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showLoading(false);
                        Toast.makeText(RegisterActivity.this, "Unknown Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserInfo() {
        long timestamp = System.currentTimeMillis();
        String uid = Objects.requireNonNull(auth.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("uid", uid);
        hashMap.put("name", name);
        hashMap.put("email", email);
        hashMap.put("profileImage", "https://firebasestorage.googleapis.com/v0/b/book-collection-9f764.appspot.com/o/placeholder_profile.png?alt=media&token=6b492230-3cf7-4abf-a3f8-2fb936eb953c");
        hashMap.put("userType", "admin");
        hashMap.put("timestamp", timestamp);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showLoading(false);
                        Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showLoading(false);
                        Toast.makeText(RegisterActivity.this, "Unknown Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            binding.btnRegisterText.setVisibility(View.GONE);
            binding.loadingAnimation.setVisibility(View.VISIBLE);
        } else {
            binding.btnRegisterText.setVisibility(View.VISIBLE);
            binding.loadingAnimation.setVisibility(View.GONE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setDrawableEndClickListener(EditText editText, boolean isPassword) {
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
                    togglePasswordVisibility(editText, isPassword);
                    return true;
                }
            }
            return false;
        });
    }

    private void togglePasswordVisibility(EditText editText, boolean isPassword) {
        if (isPassword) {
            if (isPasswordVisible) {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock_gray, 0, R.drawable.eye_hide, 0);
            } else {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock_gray, 0, R.drawable.eye_show, 0);
            }
            isPasswordVisible = !isPasswordVisible;
        } else {
            if (isCPasswordVisible) {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock_gray, 0, R.drawable.eye_hide, 0);
            } else {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock_gray, 0, R.drawable.eye_show, 0);
            }
            isCPasswordVisible = !isCPasswordVisible;
        }
        editText.setSelection(editText.getText().length());
    }
}
