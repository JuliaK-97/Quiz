package com.example.quiz;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * LoginActivity2
 *
 * Purpose:
 * This activity manages user authentication using firebase authentication.
 * It allows users to log in with an email and password, provides feedback during
 * the authentication process, and routes users to the main page of the app once login is
 * successful.
 *
 * Why this activity is used:
 * - centralizes all login-related login to a single screen
 * - Uses firebase Authentication to avoid implementing a custom and less secure version
 * -improves user experience by validating input and providing clear feedback.
 * - Ensures users cannot access the main app without proper authentication
 *
 * How it works:
 * 1. collects the users email and password from input fields.
 * 2. validate that both fields are actually filled in before attempting authentication
 * -if fields are not filled in, it warns the user and prompts them to fill in the fields
 * 3. Displays a progress dialog while Firebase processes the login request.
 * 4. If the login is successful, it preloads user-specific data using loadData(), and then
 * redirects them to the main page of the app.
 * 5. if the login fails it displays an error message using a toast.
 * 6. For new users there is the option to navigate to the sign up page.
 */


public class LoginActivity2 extends AppCompatActivity {
   private EditText email;
    private EditText pass;
    private Button loginBtn;
    private TextView forgotPassBtn, signupBtn;
    private FirebaseAuth mAuth;
    private Dialog progressDialog;
    private TextView dialogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login2);

        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login_btn);
        forgotPassBtn = findViewById(R.id.forgot_password);
        signupBtn = findViewById(R.id.sign_up_btn);

        progressDialog = new Dialog(LoginActivity2.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = progressDialog.findViewById(R.id.dialog_text);
        dialogText.setText("Signing in...");
        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(validateData()){
                    login();
                }
            }
        });
        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity2.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

    }
    private boolean validateData(){
       if(email.getText().toString().isEmpty())
       {
           email.setError("Enter email address");
           return false;
       }
       if(pass.getText().toString().isEmpty())
       {
           pass.setError("Enter Password");
           return false;
       }
       return true;
    }
    private void login(){
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email.getText().toString().trim(), pass.getText().toString().trim())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity2.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            DbQuery.loadData(new MyCompleteListener() {
                                @Override
                                public void onSuccess() {
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(LoginActivity2.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                }

                                @Override
                                public void onFailure() {
                                    progressDialog.dismiss();
                                    Toast.makeText(LoginActivity2.this, "Something went wrong! Please try again later!", Toast.LENGTH_SHORT).show();

                                }
                            });

                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity2.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }
}