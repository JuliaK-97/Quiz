package com.example.quiz;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
/**
 * MyProfileActivity
 *
 * Purpose:
 * This activity allows users to view and update their profile information,
 * specifically their name and phone number, while keeping their email address
 * read-only. It provides a controlled editing experience with validation and
 * feedback during save operations.
 *
 * Why this activity is used:
 * - Gives users a dedicated screen to manage their personal information.
 * - Prevents accidental changes to immutable data such as email addresses.
 * - Improves user experience by clearly separating view and edit states.
 * - Centralizes profile update logic instead of scattering it across the app.
 *
 * How it works:
 * 1. Sets up a Toolbar with a back button and the title "My profile".
 * 2. Displays the user’s current profile information retrieved from DbQuery.myProfile.
 * 3. Starts in a read-only state where input fields are disabled and edit actions
 *    are hidden.
 * 4. When edit mode is enabled, allows the user to update their name and phone
 *    number while keeping the email field locked.
 * 5. Validates user input to ensure the name is not empty and the phone number,
 *    if provided, is numeric and exactly 10 digits.
 * 6. Shows a progress dialog while updated profile data is being saved.
 * 7. Saves the changes using DbQuery.saveProfileData() and handles the result
 *    through a callback.
 * 8. On success, updates the UI, exits edit mode, and notifies the user.
 *    On failure, displays an error message and restores the previous state.
 */

public class MyProfileActivity extends AppCompatActivity {
    private EditText name, email, phone;
    private LinearLayout editBtn, buttonLayout;
    private TextView profileTV;
    private Button saveBtn, cancelBtn;
    private String nameStr, phoneStr;
    private Dialog progressDialog;
    private TextView dialogText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("My profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.getNavigationIcon().setTint(Color.WHITE);


        progressDialog = new Dialog(MyProfileActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = progressDialog.findViewById(R.id.dialog_text);
        dialogText.setText("Saving...");


        name = findViewById(R.id.mp_name);
        email = findViewById(R.id.mp_email);
        phone = findViewById(R.id.mp_number);
        editBtn = findViewById(R.id.edit_btn);
        saveBtn = findViewById(R.id.mp_save_btn);
        cancelBtn = findViewById(R.id.mp_cancelBtn);
        profileTV = findViewById(R.id.mp_profileTV);
        buttonLayout = findViewById(R.id.button_layout);

        disableEditing();
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableEditing();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableEditing();
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate())
                {
                    saveData();
                }
            }
        });

    }
    private void disableEditing()
    {
        name.setEnabled(false);
        email.setEnabled(false);
        phone.setEnabled(false);

        buttonLayout.setVisibility(View.GONE);
        name.setText(DbQuery.myProfile.getName());
        email.setText(DbQuery.myProfile.getEmail());

        if(DbQuery.myProfile.getPhone() != null)
            phone.setText(DbQuery.myProfile.getPhone());
        String profileName = DbQuery.myProfile.getName();

        profileTV.setText(profileName.toUpperCase().substring(0,1));


    }
    private void enableEditing()
    {
        name.setEnabled(true);
        email.setEnabled(false);
        phone.setEnabled(true);
        buttonLayout.setVisibility(View.VISIBLE);
    }
    private boolean validate()
    {
        nameStr = name.getText().toString();
        phoneStr = phone.getText().toString();

        if(nameStr.isEmpty())
        {
            name.setError("Name can not be empty!");
            return false;
        }
        if(! phoneStr.isEmpty())
        {
            if(! ((phoneStr.length() == 10) && (TextUtils.isDigitsOnly(phoneStr))))
            {
               phone.setError("Enter valid phone number!");
               return false;
            }
        }
        return true;
    }
    private void saveData()
    {
        progressDialog.show();
        if(phoneStr.isEmpty())
            phoneStr = null;
        DbQuery.saveProfileData(nameStr, phoneStr, new MyCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MyProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                disableEditing();
                progressDialog.dismiss();

            }

            @Override
            public void onFailure() {
                Toast.makeText(MyProfileActivity.this, "Something went wrong! Please try again later!",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();

            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            MyProfileActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}