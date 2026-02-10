package com.example.quiz;

import static com.example.quiz.DbQuery.g_catList;
import static com.example.quiz.DbQuery.g_selected_test_index;
import static com.example.quiz.DbQuery.loadQuestions;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
/**
 * StartTestActivity
 *
 * Purpose:
 * This activity displays the test details (category, test number, total questions,
 * best score, and time limit) and allows the user to start the quiz. It loads the
 * required questions from the database before the quiz begins.
 *
 * Why this activity is used:
 * - Provides a clear summary of the selected test before the user starts.
 * - Ensures questions are loaded and ready before navigating to the quiz screen.
 * - Prevents the user from starting the quiz without the required data.
 * - Maintains consistent navigation and user feedback during loading.
 *
 * How it works:
 * 1. Enables edge-to-edge layout and sets the activity layout.
 * 2. Initializes UI elements and click listeners.
 * 3. Shows a progress dialog while questions are loaded from the database.
 * 4. Calls DbQuery.loadQuestions() with a callback:
 *    - On success: updates the UI with test details and dismisses the dialog.
 *    - On failure: dismisses the dialog and shows an error Toast.
 * 5. When the user clicks "Start Test", opens QuestionsActivity and finishes the current activity.
 * 6. When the back button is clicked, closes the activity and returns to the previous screen.
 */

public class StartTestActivity extends AppCompatActivity {

    private TextView catName, testNo, totalQs, bestScore, time;
    private Button startTestBtn;
    private ImageView backBtn;
    private Dialog progressDialog;
    private TextView dialogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start_test);

        init();

        progressDialog = new Dialog(StartTestActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = progressDialog.findViewById(R.id.dialog_text);
        dialogText.setText("Loading...");
        progressDialog.show();

        loadQuestions(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                setData();
                progressDialog.dismiss();
            }

            @Override
            public void onFailure() {
                progressDialog.dismiss();
                Toast.makeText(StartTestActivity.this,
                        "Something went wrong! Please try again later!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init() {
        catName = findViewById(R.id.st_cat_name);
        testNo = findViewById(R.id.st_test_no);
        totalQs = findViewById(R.id.st_value_qs);
        bestScore = findViewById(R.id.st_value_score);
        time = findViewById(R.id.st_value_time);
        startTestBtn = findViewById(R.id.start_Tbtn);
        backBtn = findViewById(R.id.st_backBtn);

        // Ensure back button is clickable and closes activity
        backBtn.setClickable(true);
        backBtn.setOnClickListener(v -> {
            Log.d("StartTestActivity", "Back button clicked");
            finish();
        });

        startTestBtn.setOnClickListener(v -> {
            Intent intent = new Intent(StartTestActivity.this, QuestionsActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setData() {
        catName.setText(g_catList.get(DbQuery.g_selected_cat_index).getName());
        testNo.setText("Test No. " + (g_selected_test_index + 1));
        totalQs.setText(String.valueOf(DbQuery.g_questionList.size()));
        bestScore.setText(String.valueOf(DbQuery.g_testList.get(g_selected_test_index).getTopScore()));
        time.setText(String.valueOf(DbQuery.g_testList.get(g_selected_test_index).getTime()));
    }
}
