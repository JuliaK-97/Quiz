package com.example.quiz;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.auth.FirebaseUser;

/**
 * SignUpActivity
 *
 * Purpose:
 * This activity handles user registration using Firebase Authentication. It collects
 * user input, validates the data, creates a new Firebase account, and initializes
 * user profile data in the database before navigating to the main app screen.
 *
 * Why this activity is used:
 * - Provides a secure and reliable registration flow without building a custom backend.
 * - Ensures user data is validated before attempting account creation.
 * - Separates authentication from application-specific data storage using DbQuery.
 * - Prevents users from returning to the registration screen after successful sign-up.
 *
 * How it works:
 * 1. Collects the user’s name, email, password, and confirm password from input fields.
 * 2. Validates the inputs to ensure all fields are filled and passwords match.
 * 3. Displays a progress dialog while the sign-up process is in progress.
 * 4. Calls FirebaseAuth.createUserWithEmailAndPassword() to create the user account.
 * 5. On successful registration:
 *    - Calls DbQuery.createUserData() to store user profile details.
 *    - Calls DbQuery.loadData() to preload user-specific app data.
 *    - Navigates to MainActivity and finishes the SignUpActivity.
 * 6. On failure, displays an error message using a Toast and dismisses the progress dialog.
 */

public class SignUpActivity extends AppCompatActivity {
    private EditText name, email, pass, confirmPass;
    private Button signUpBtn;
    private ImageView backBtn;
    private FirebaseAuth mAuth;
    private String emailStr, passStr, confirmPassStr, nameStr;
    private Dialog progressDialog;
    private TextView dialogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        name = findViewById(R.id.username);
        email = findViewById(R.id.emailID);
        pass = findViewById(R.id.passwordN);
        confirmPass = findViewById(R.id.confirmPass);
        signUpBtn = findViewById(R.id.signup_btn);
        backBtn = findViewById(R.id.backBtn);


        progressDialog = new Dialog(SignUpActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = progressDialog.findViewById(R.id.dialog_text);
        dialogText.setText("Registering user...");

        mAuth = FirebaseAuth.getInstance();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()) {
                    signUpNewUser();
                }

            }
        });
    }
    private boolean validate()
    {
        nameStr = name.getText().toString().trim();
        passStr = pass.getText().toString().trim();
        emailStr = email.getText().toString().trim();
        confirmPassStr = confirmPass.getText().toString().trim();

        if(nameStr.isEmpty())
        {
            name.setError("Enter Your Name");
            return false;
        }

        if(emailStr.isEmpty())
        {
            email.setError("Enter Your Email");
            return false;
        }
        if(passStr.isEmpty())
        {
            pass.setError("Enter Your password");
            return false;
        }
        if(confirmPassStr.isEmpty())
        {
            confirmPass.setError("Confirm Your Password");
            return false;
        }
        if(passStr.compareTo(confirmPassStr)!=0)
        {
            Toast.makeText(SignUpActivity.this,"Password and Confirm Password should be the same!",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;

    }
    private void signUpNewUser()
    {
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(emailStr, passStr)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();

                            DbQuery.createUserData(emailStr, nameStr, new MyCompleteListener() {
                                @Override
                                public void onSuccess() {
                                    DbQuery.loadData(new MyCompleteListener() {
                                        @Override
                                        public void onSuccess() {
                                            progressDialog.dismiss();
                                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            SignUpActivity.this.finish();

                                        }

                                        @Override
                                        public void onFailure() {
                                            Toast.makeText(SignUpActivity.this, "Something went wrong! Please try again later!", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();

                                        }
                                    });


                                }

                                @Override
                                public void onFailure() {
                                    Toast.makeText(SignUpActivity.this, "Something went wrong! Please try again later!", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();

                                }
                            });


                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}