package com.example.quiz;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quiz.Adapters.AnswersAdapter;
import com.example.quiz.Adapters.BookmarkAdapter;
/**
 * BookMarksActivity
 *
 * Purpose:
 * This activity displays all questions that the user has bookmarked/saved for later review.
 * It provides a scrollable list of bookmarked questions using a RecyclerView.
 *
 * Why this activity is used:
 * - Allows users to revisit difficult questions without retaking the full quiz.
 * - Keeps bookmarked question display separate from quiz and results screens.
 * - Provides a simple review interface without interaction (read-only).
 *
 * How it works:
 * 1. Toolbar Setup:
 * - A Toolbar is set as the ActionBar.
 * - Title is set to "Saved Questions".
 * - Back button is enabled for easy navigation.
 * 2. Progress Dialog:
 * - A custom Dialog is shown while bookmarks are loading.
 * - Prevents user interaction during the network/database fetch.
 *- Dialog is dismissed once loading completes or fails.
 * 3. RecyclerView Setup:
 * - Uses a LinearLayoutManager for vertical scrolling.
 * - After loading bookmarks, BookmarkAdapter is attached to the RecyclerView.
 * 4. Loading Bookmarks:
 * - DbQuery.loadBookMarks() retrieves bookmarked questions from Firestore.
 * - On success: Adapter is set and progress dialog dismissed.
 * - On failure: Progress dialog dismissed (no additional UI shown).
 * 5. Navigation:
 * - Back button in the toolbar closes the activity using finish().
 *
 * Notes:
 * - DbQuery.g_bookmarksList must be populated for the list to show items.
 * - This activity is read-only and does not allow editing bookmarks.
 */

public class BookMarksActivity extends AppCompatActivity {
    private RecyclerView questionsView;
    private Toolbar toolbar;
    private BookmarkAdapter adapter;
    private Dialog progressDialog;
    private TextView dialogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_marks);

        toolbar = findViewById(R.id.bm_toolbar);
        questionsView = findViewById(R.id.bm_recycler_view);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Saved Questions");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        progressDialog = new Dialog(BookMarksActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = progressDialog.findViewById(R.id.dialog_text);
        dialogText.setText("Loading...");
        progressDialog.show();

        // RecyclerView setup

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        questionsView.setLayoutManager(layoutManager);



        DbQuery.loadBookMarks(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                BookmarkAdapter adapter = new BookmarkAdapter(DbQuery.g_bookmarksList);
                questionsView.setAdapter(adapter);
                progressDialog.dismiss();

            }

            @Override
            public void onFailure() {
                progressDialog.dismiss();

            }
        });





    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            BookMarksActivity.this.finish(); // Close activity when back button pressed
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}