package com.example.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.graphics.Typeface;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
/**
 * SplashActivity2
 *
 * Purpose:
 * This activity displays a splash screen when the app starts, shows a short
 * animation, and then routes the user to the appropriate next screen based on
 * their authentication status.
 *
 * Why this activity is used:
 * - Provides a brief branded introduction to the app.
 * - Allows time for Firebase initialization before the user enters the app.
 * - Automatically redirects authenticated users to the main screen.
 * - Redirects unauthenticated users to the login screen.
 *
 * How it works:
 * 1. Enables edge-to-edge layout and sets the splash screen layout.
 * 2. Applies a custom font to the app name text view and starts the animation.
 * 3. Initializes FirebaseAuth and FirebaseFirestore instances.
 * 4. Uses a background thread to delay for 3 seconds.
 * 5. After the delay, checks FirebaseAuth.getCurrentUser():
 *    - If the user is already logged in, navigates to MainActivity.
 *    - If the user is not logged in, navigates to LoginActivity2.
 * 6. Finishes the splash activity so the user cannot return to it.
 */


public class SplashActivity2 extends AppCompatActivity {
    private TextView appName;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash2);

        appName = findViewById(R.id.app_name);
        Typeface typeface = ResourcesCompat.getFont(this,R.font.roboto_serif_italic);
        appName.setTypeface(typeface);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.myanim);
        appName.setAnimation(anim);

        mAuth = FirebaseAuth.getInstance();

        DbQuery.g_firestore = FirebaseFirestore.getInstance();

        new Thread() {
            @Override
            public void run(){
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if(mAuth.getCurrentUser() != null)
                {
                    Intent intent = new Intent(SplashActivity2.this, MainActivity.class);
                    startActivity(intent);
                    SplashActivity2.this.finish();
                }
                else
                {
                    Intent intent = new Intent(SplashActivity2.this, LoginActivity2.class);
                    startActivity(intent);
                    SplashActivity2.this.finish();
                }

            }

        }.start();
    }
}