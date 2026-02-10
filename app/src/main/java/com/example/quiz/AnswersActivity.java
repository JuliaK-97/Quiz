package com.example.quiz;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quiz.Adapters.AnswersAdapter;
import com.example.quiz.Models.QuestionModel;
/**
 * AnswersActivity
 *
 * Purpose:
 * This activity displays a review screen where the user can see all quiz questions,
 * their selected answers, and whether each response was correct or incorrect.
 *
 * Why this activity is used:
 * - Provides a read-only review of the completed quiz.
 * - Allows users to analyze their performance and learn from mistakes.
 * - Keeps review UI separate from quiz-taking UI for better organization.
 *
 * How it works:
 * 1. Toolbar Setup:
 * - A Toolbar is set as the ActionBar.
 * - Title is set to "Answers".
 * - Back button is enabled for easy navigation.
 * 2. RecyclerView Setup:
 * - A LinearLayoutManager is used for vertical scrolling.
 * - AnswersAdapter is attached to the RecyclerView.
 * - DbQuery.g_questionList (global question list) is passed to the adapter.
 * 3. Navigation:
 * - Back button in the toolbar closes the activity using finish().
 *
 * Notes:
 * - This activity relies on DbQuery.g_questionList being populated from the previous quiz session.
 * - No user interaction is allowed; the screen is purely for review purposes.
 */

public class AnswersActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView answersView;
    private AnswersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answers);

        // Toolbar setup
        toolbar = findViewById(R.id.a_toolbar);
        answersView = findViewById(R.id.aa_recycler_view);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Answers");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // RecyclerView setup

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        answersView.setLayoutManager(layoutManager);
        AnswersAdapter adapter = new AnswersAdapter(DbQuery.g_questionList);
        answersView.setAdapter(adapter);





    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close activity when back button pressed
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
