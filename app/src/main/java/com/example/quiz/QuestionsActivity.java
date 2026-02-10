package com.example.quiz;

import static com.example.quiz.DbQuery.g_questionList;
import static com.example.quiz.DbQuery.g_selected_test_index;
import static com.example.quiz.DbQuery.g_testList;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.quiz.Adapters.QuestionGridAdapter;
import com.example.quiz.Models.QuestionModel;

import java.util.concurrent.TimeUnit;

/**
 * QuestionsActivity
 *
 * Purpose:
 * This activity controls the quiz-taking flow. It is responsible for loading
 * questions, displaying them using the appropriate fragment type, handling
 * navigation between questions, tracking time, and managing submission.
 *
 * Why this activity is used:
 * - Acts as the single controller for the entire quiz experience.
 * - Supports multiple question types by dynamically loading different fragments.
 * - Keeps question navigation, status tracking, and timing logic centralized.
 * - Provides tools for users to bookmark questions and mark them for review.
 *
 * How it works:
 * 1. Loads the list of questions from the database using DbQuery.loadQuestions().
 * 2. Displays questions using the appropriate fragment based on question type
 *    (SingleChoiceFragment, MultiSelectFragment, or TrueFalseFragment).
 * 3. Tracks the current question index and updates question status
 *    (NOT_VISITED, UNANSWERED, ANSWERED, REVIEW) as the user progresses.
 * 4. Allows navigation between questions using Next/Previous buttons or a
 *    navigation drawer containing a grid of all questions.
 * 5. Supports bookmarking questions and marking them for review, updating
 *    visual indicators and question status accordingly.
 * 6. Starts a countdown timer based on the test duration and updates the UI
 *    every second.
 * 7. Automatically submits the quiz when the timer finishes or when the user
 *    confirms submission.
 * 8. Calculates the time taken and navigates to ScoreActivity to display results.
 */

public class QuestionsActivity extends AppCompatActivity {

    private TextView tvQuesID, timerTV, catNameTV;
    private Button submitBtn, markBtn;
    private ImageButton prevQsBtn, nextQsBtn, drawerCloseBtn;
    private ImageView qsListB, markImage, bookMarkB;
    private int currentIndex = 0;
    private DrawerLayout drawer;
    private GridView quesListGV;
    private QuestionGridAdapter adapter;
    private CountDownTimer timer;
    private long timeLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.questions_list_layout);

        init();

        DbQuery.loadQuestions(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                adapter = new QuestionGridAdapter(DbQuery.g_questionList.size());
                quesListGV.setAdapter(adapter);

                if (DbQuery.g_questionList.get(0).getStatus() == QuestionModel.NOT_VISITED) {
                    DbQuery.g_questionList.get(0).setStatus(QuestionModel.UNANSWERED);
                }

                showQuestion(DbQuery.g_questionList.get(0), 0);
                updateUI(0);
            }

            @Override
            public void onFailure() {
                Toast.makeText(QuestionsActivity.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
            }
        });

        nextQsBtn.setOnClickListener(v -> {
            if (currentIndex < DbQuery.g_questionList.size() - 1) {
                currentIndex++;
                QuestionModel nextQuestion = DbQuery.g_questionList.get(currentIndex);

                if (nextQuestion.getStatus() == QuestionModel.NOT_VISITED) {
                    nextQuestion.setStatus(QuestionModel.UNANSWERED);
                }

                showQuestion(nextQuestion, currentIndex);
                updateUI(currentIndex);
                adapter.notifyDataSetChanged();
            }
        });

        prevQsBtn.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                showQuestion(DbQuery.g_questionList.get(currentIndex), currentIndex);
                updateUI(currentIndex);
            }
        });

        qsListB.setOnClickListener(v -> {
            if (!drawer.isDrawerOpen(GravityCompat.END)) {
                drawer.openDrawer(GravityCompat.END);
            }
        });

        drawerCloseBtn.setOnClickListener(v -> {
            if (drawer.isDrawerOpen(GravityCompat.END)) {
                drawer.closeDrawer(GravityCompat.END);
            }
        });

        markBtn.setOnClickListener(v -> {
            if (markImage.getVisibility() != View.VISIBLE) {
                markImage.setVisibility(View.VISIBLE);
                DbQuery.g_questionList.get(currentIndex).setStatus(QuestionModel.REVIEW);
            } else {
                markImage.setVisibility(View.GONE);

                if (DbQuery.g_questionList.get(currentIndex).getSelectedAns() != -1 ||
                        (DbQuery.g_questionList.get(currentIndex).getSelectedAnsList() != null &&
                                !DbQuery.g_questionList.get(currentIndex).getSelectedAnsList().isEmpty())) {
                    DbQuery.g_questionList.get(currentIndex).setStatus(QuestionModel.ANSWERED);
                } else {
                    DbQuery.g_questionList.get(currentIndex).setStatus(QuestionModel.UNANSWERED);
                }
            }
            adapter.notifyDataSetChanged();
        });

        submitBtn.setOnClickListener(v -> submitTest());

        bookMarkB.setOnClickListener(v ->{
            addToBookMark();

                }
                );

        startTimer();
    }

    private void init() {
        tvQuesID = findViewById(R.id.tv_qsID);
        timerTV = findViewById(R.id.tv_timer);
        catNameTV = findViewById(R.id.qa_catName);
        submitBtn = findViewById(R.id.submit_btn);
        markBtn = findViewById(R.id.mark_review_btn);
        prevQsBtn = findViewById(R.id.prev_qs_btn);
        nextQsBtn = findViewById(R.id.next_qs_btn);
        qsListB = findViewById(R.id.question_list_gridBtn);
        drawer = findViewById(R.id.qs_drawer_layout);
        drawerCloseBtn = findViewById(R.id.drawer_closeBtn);
        quesListGV = findViewById(R.id.ques_list_gridV);
        markImage = findViewById(R.id.mark_image);
        bookMarkB = findViewById(R.id.qa_bookmarkB);
    }

    private void showQuestion(QuestionModel question, int index) {
        Fragment fragment;
        switch (question.getQuestionType()) {
            case "single_choice":
                fragment = SingleChoiceFragment.newInstance(question, index);
                break;
            case "multi_select":
                fragment = MultiSelectFragment.newInstance(question, index);
                break;
            case "true_false":
                fragment = TrueFalseFragment.newInstance(question, index);
                break;
            default:
                Toast.makeText(this, "Unsupported question type", Toast.LENGTH_SHORT).show();
                return;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.question_container, fragment)
                .commit();
    }

    public void updateUI(int index) {
        tvQuesID.setText("Question " + (index + 1) + " of " + DbQuery.g_questionList.size());
        catNameTV.setText(DbQuery.g_catList.get(DbQuery.g_selected_cat_index).getName());
        if(g_questionList.get(index).isBookmarked())
        {
            bookMarkB.setImageResource(R.drawable.ic_bookmark_selected);
        }
        else {
            bookMarkB.setImageResource(R.drawable.ic_bookmark);
        }
        if (DbQuery.g_questionList.get(index).getStatus() == QuestionModel.REVIEW) {
            markImage.setVisibility(View.VISIBLE);
        } else {
            markImage.setVisibility(View.GONE);
        }
    }

    private void startTimer() {
        long totalTime = g_testList.get(g_selected_test_index).getTime() * 60 * 1000;
        DbQuery.startTime = System.currentTimeMillis();

        timer = new CountDownTimer(totalTime + 1000, 1000) {
            @Override
            public void onFinish() {
                long totalTime = g_testList.get(g_selected_test_index).getTime() * 60 * 1000;
                Intent intent = new Intent(QuestionsActivity.this, ScoreActivity.class);
                intent.putExtra("TIME_TAKEN", totalTime - timeLeft);
                startActivity(intent);
                QuestionsActivity.this.finish();
            }

            @Override
            public void onTick(long remainingTime) {
                timeLeft = remainingTime;
                String time = String.format("%02d:%02d min",
                        TimeUnit.MILLISECONDS.toMinutes(remainingTime),
                        TimeUnit.MILLISECONDS.toSeconds(remainingTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainingTime))
                );
                timerTV.setText(time);
            }
        };
        timer.start();
    }

    public QuestionGridAdapter getAdapter() {
        return adapter;
    }

    public void goToQuestion(int index) {
        currentIndex = index;
        QuestionModel question = DbQuery.g_questionList.get(index);

        if (question.getStatus() == QuestionModel.NOT_VISITED) {
            question.setStatus(QuestionModel.UNANSWERED);
        }

        showQuestion(question, index);
        updateUI(index);

        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        }
    }

    private void submitTest() {
        AlertDialog.Builder builder = new AlertDialog.Builder(QuestionsActivity.this);
        builder.setCancelable(false);

        View view = getLayoutInflater().inflate(R.layout.alert_dialog_layout, null);
        Button cancelBtn = view.findViewById(R.id.cancel_btn);
        Button confirmBtn = view.findViewById(R.id.confirm_btn);

        builder.setView(view);
        AlertDialog alertDialog = builder.create();

        cancelBtn.setOnClickListener(v -> alertDialog.dismiss());
        confirmBtn.setOnClickListener(v -> {
            if (timer != null) timer.cancel();
            alertDialog.dismiss();

            Intent intent = new Intent(QuestionsActivity.this, ScoreActivity.class);
            long totalTime = g_testList.get(g_selected_test_index).getTime() * 60 * 1000;
            intent.putExtra("TIME_TAKEN", totalTime - timeLeft);
            startActivity(intent);
            QuestionsActivity.this.finish();
        });

        alertDialog.show();
    }
    /*
    if a user clicks on the bookmark icon, the question is bookmarked. If they click on it again the bookmark will be removed
     */
    private void addToBookMark() {
        QuestionModel currentQuestion = g_questionList.get(currentIndex);

        if (currentQuestion.isBookmarked()) {
            currentQuestion.setBookmarked(false);
            bookMarkB.setImageResource(R.drawable.ic_bookmark); // unselected icon
        } else {
            currentQuestion.setBookmarked(true);
            bookMarkB.setImageResource(R.drawable.ic_bookmark_selected); // selected icon
        }
    }

}
