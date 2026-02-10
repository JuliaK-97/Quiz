package com.example.quiz;

import static com.example.quiz.DbQuery.g_questionList;
import static com.example.quiz.DbQuery.g_selected_test_index;
import static com.example.quiz.DbQuery.g_testList;
import static com.example.quiz.DbQuery.myProfile;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.quiz.Models.QuestionModel;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * ScoreActivity
 *
 * Purpose:
 * This activity displays the results of a completed quiz. It calculates the
 * final score, shows detailed statistics (correct, incorrect, unattempted),
 * and provides options for users to view answers, reattempt the quiz, or
 * check the leaderboard.
 *
 * Why this activity is used:
 * - Provides users with clear feedback on their performance.
 * - Ensures results are saved and synchronized with the database.
 * - Allows users to review answers or retry the quiz without losing data.
 * - Keeps quiz completion logic centralized and separate from question navigation.
 *
 * How it works:
 * 1. Loads the global question list (DbQuery.g_questionList) and counts:
 *    correct, wrong, and unattempted answers.
 * 2. Calculates the final score as a percentage and displays it along with
 *    total questions and time taken.
 * 3. Saves the result to the database using DbQuery.saveResult().
 * 4. Synchronizes bookmarks by ensuring the global bookmark list
 *    (DbQuery.g_bmIdList) matches the current question bookmark states.
 * 5. Provides user actions:
 *    - View Answers: Opens AnswersActivity.
 *    - Reattempt: Resets question states and restarts QuestionsActivity.
 *    - Leaderboard: Navigates to the leaderboard screen.
 * 6. Uses a progress dialog to prevent interaction while data is being saved.
 */
public class ScoreActivity extends AppCompatActivity {
    private TextView scoreTV, timeTV, totalQsTV, correctQsTV, incorrectQsTV, unAtQsTV;
    private Button leaderBoardB, reattemptBtn, viewAnsBtn;
    private Dialog progressDialog;
    private TextView dialogText;
    private long timeTaken;
    private int finalScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_score);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Result");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        progressDialog = new Dialog(ScoreActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = progressDialog.findViewById(R.id.dialog_text);
        dialogText.setText("Loading...");
        progressDialog.show();

        init();
        loadData();
        setBookMarks();

        viewAnsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ScoreActivity.this, AnswersActivity.class);
            startActivity(intent);
        });

        reattemptBtn.setOnClickListener(v -> reAttempt());

        saveResult();
    }

    private void init() {
        scoreTV = findViewById(R.id.score);
        timeTV = findViewById(R.id.total_time);
        totalQsTV = findViewById(R.id.total_qs);
        correctQsTV = findViewById(R.id.correctQ);
        incorrectQsTV = findViewById(R.id.wrongQ);
        unAtQsTV = findViewById(R.id.un_attemptedQ);
        leaderBoardB = findViewById(R.id.sa_leaderboard_btn);
        reattemptBtn = findViewById(R.id.reattempt_btn);
        viewAnsBtn = findViewById(R.id.view_ansBtn);
    }

    private void loadData() {
        int correctQ = 0, wrongQ = 0, unattemptQ = 0;

        for (QuestionModel question : DbQuery.g_questionList) {
            if (!question.isAttempted()) {
                unattemptQ++;
            } else if (question.isCorrect()) {
                correctQ++;
            } else {
                wrongQ++;
            }
        }

        correctQsTV.setText(String.valueOf(correctQ));
        incorrectQsTV.setText(String.valueOf(wrongQ));
        unAtQsTV.setText(String.valueOf(unattemptQ));
        totalQsTV.setText(String.valueOf(DbQuery.g_questionList.size()));

        if (DbQuery.g_questionList.size() > 0) {
            finalScore = (correctQ * 100) / DbQuery.g_questionList.size();
        } else {
            finalScore = 0;
        }
        scoreTV.setText(String.valueOf(finalScore));

        timeTaken = getIntent().getLongExtra("TIME_TAKEN", 0);
        String time = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeTaken),
                TimeUnit.MILLISECONDS.toSeconds(timeTaken) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeTaken)));
        timeTV.setText(time);

        DbQuery.saveResult(finalScore, new MyCompleteListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
            }

            @Override
            public void onFailure() {
                Toast.makeText(ScoreActivity.this, "Something went wrong! Please try again later!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }


    private void reAttempt() {
        for (QuestionModel question : DbQuery.g_questionList) {
            question.setSelectedAns(-1);
            question.setSelectedAnsList(new ArrayList<>());
            question.setStatus(QuestionModel.NOT_VISITED);
        }

        Intent intent = new Intent(ScoreActivity.this, QuestionsActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveResult() {
        DbQuery.saveResult(finalScore, new MyCompleteListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
            }

            @Override
            public void onFailure() {
                Toast.makeText(ScoreActivity.this, "Something went wrong! Please try again later!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }
    /*
    a user can bookmark or unbookmark a question and this function will update the list of the bookmarked questions
     */
    private void setBookMarks()
    {
        for(int i=0; i<DbQuery.g_questionList.size(); i++)
        {
            QuestionModel q = g_questionList.get(i);
            if(q.isBookmarked())//this is where a question is already bookmarked, you first need to check if it is in the bm list and if it isn't you need to add it and update the count
            {
               if(! DbQuery.g_bmIdList.contains(q.getqID()))
               {
                   DbQuery.g_bmIdList.add(q.getqID());
                   DbQuery.myProfile.setBookmarksCount(DbQuery.g_bmIdList.size());
               }

            }//this is when the question is not bookmarked, once again you need to check if it is in the bm list or not, if it is it needs to be removed and the count updated
            else {
                if(DbQuery.g_bmIdList.contains(q.getqID()))
                {
                    DbQuery.g_bmIdList.remove(q.getqID());
                    DbQuery.myProfile.setBookmarksCount(DbQuery.g_bmIdList.size());
                }

            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            ScoreActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
