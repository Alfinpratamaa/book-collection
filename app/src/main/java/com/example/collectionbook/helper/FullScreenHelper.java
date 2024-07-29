package com.example.collectionbook.helper;

import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class FullScreenHelper {

    public static void makeFullScreen(Activity activity) {
        Window window = activity.getWindow();

        // Mengatur flag untuk layar tanpa batas
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // Mendapatkan decor view dari aktivitas
        View decorView = window.getDecorView();

        // Mengatur opsi UI untuk menyembunyikan navigasi
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;

        // Mengatur visibilitas sistem UI
        decorView.setSystemUiVisibility(uiOptions);
    }
}
