package com.example.quiz;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quiz.Adapters.TestAdapter;
/**
 * TestActivity
 *
 * Purpose:
 * This activity displays a list of available tests for the selected category.
 * It loads test data and the user's previous scores, then shows them in a RecyclerView.
 *
 * Why this activity is used:
 * - Provides a dedicated screen for users to select a specific test within a category.
 * - Ensures all necessary data (tests and scores) is loaded before the UI is shown.
 * - Keeps test selection logic separated from other screens such as category listing
 *   or quiz-taking.
 *
 * How it works:
 * 1. Sets up the toolbar with the selected category name and enables the back button.
 * 2. Shows a progress dialog while data is being loaded.
 * 3. Loads test data using DbQuery.loadTestData().
 * 4. After tests load successfully, loads the user’s previous scores using DbQuery.loadMyScores().
 * 5. Initializes the TestAdapter with the loaded test list and sets it on the RecyclerView.
 * 6. Dismisses the progress dialog once data is loaded or shows a Toast on failure.
 * 7. Handles back navigation using the toolbar back arrow and finishes the activity.
 */

public class TestActivity extends AppCompatActivity {
    private RecyclerView testView;
    private Toolbar toolbar;
    private TestAdapter adapter;
    private Dialog progressDialog;
    private TextView dialogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);

            if (DbQuery.g_catList != null && DbQuery.g_selected_cat_index < DbQuery.g_catList.size()) {
                getSupportActionBar().setTitle(DbQuery.g_catList.get(DbQuery.g_selected_cat_index).getName());
            } else {
                getSupportActionBar().setTitle("Tests");
            }

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setTitleTextColor(Color.WHITE);
            toolbar.getNavigationIcon().setTint(Color.WHITE);
        }


        toolbar.setNavigationOnClickListener(v -> {
            Log.d("TestActivity", "Back arrow clicked via NavigationOnClickListener");
            finish();
        });

        testView = findViewById(R.id.test_recycler_view);

        progressDialog = new Dialog(TestActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = progressDialog.findViewById(R.id.dialog_text);
        dialogText.setText("Loading...");
        progressDialog.show();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        testView.setLayoutManager(layoutManager);

        DbQuery.loadTestData(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                DbQuery.loadMyScores(new MyCompleteListener() {
                    @Override
                    public void onSuccess() {
                        adapter = new TestAdapter(DbQuery.g_testList);
                        testView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure() {
                        progressDialog.dismiss();
                        Toast.makeText(TestActivity.this, "Failed to load scores!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure() {
                progressDialog.dismiss();
                Toast.makeText(TestActivity.this, "Failed to load tests!", Toast.LENGTH_SHORT).show();
            }
        });

        Log.d("TestActivity", "SupportActionBar = " + getSupportActionBar());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d("TestActivity", "Menu item pressed: " + item.getItemId());
        if (item.getItemId() == android.R.id.home) {
            Log.d("TestActivity", "Back arrow pressed via onOptionsItemSelected");
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
