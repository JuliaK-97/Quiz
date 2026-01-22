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