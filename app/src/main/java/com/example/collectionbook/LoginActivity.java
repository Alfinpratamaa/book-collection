package com.example.collectionbook;

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

import com.example.collectionbook.databinding.ActivityLoginBinding;
import com.example.collectionbook.helper.FullScreenHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        FullScreenHelper.makeFullScreen(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.navToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        binding.btnLogin.setOnClickListener(v -> {
            loginUser();
        });
        setDrawableEndClickListener(binding.etPassword, true);
    }

    private void loginUser() {
        String email = binding.eTEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        showLoading(true);

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and Password are required", Toast.LENGTH_SHORT).show();
            showLoading(false);
        } else {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            showLoading(false);
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showLoading(false);
                            if (e instanceof FirebaseAuthInvalidUserException) {
                                Toast.makeText(LoginActivity.this, "User not registered", Toast.LENGTH_SHORT).show();
                            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }


    }
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            binding.btnLoginText.setVisibility(View.GONE);
            binding.loadingAnimation.setVisibility(View.VISIBLE);
        } else {
            binding.btnLoginText.setVisibility(View.VISIBLE);
            binding.loadingAnimation.setVisibility(View.GONE);
        }
    }
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
        }
        editText.setSelection(editText.getText().length());
    }
}
