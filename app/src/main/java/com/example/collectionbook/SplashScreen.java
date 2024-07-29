package com.example.collectionbook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.collectionbook.helper.FullScreenHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FullScreenHelper.makeFullScreen(this);

        VideoView videoView = findViewById(R.id.videoView);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.artpop); // Ganti 'intro' dengan nama file video Anda
        videoView.setVideoURI(videoUri);
        videoView.setOnCompletionListener(mp -> {
            redirectToNextActivity();
        });
        videoView.start();
    }

    private void redirectToNextActivity() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Pengguna sudah login, arahkan ke MainActivity
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
        } else {
            // Pengguna belum login, arahkan ke IntroActivity
            startActivity(new Intent(SplashScreen.this, IntroActivity.class));
        }
        finish();
    }
}
