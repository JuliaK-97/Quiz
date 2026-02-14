
package com.example.quiz.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quiz.Models.QuestionModel;
import com.example.quiz.R;

import java.util.ArrayList;
import java.util.List;
/**
 * BookmarkAdapter
 * Purpose:
 * This adapter binds bookmarked quiz questions to a RecyclerView in the
 * "Bookmarks" screen. It displays each bookmarked question along with the
 * correct answer(s), without showing the user’s selected answer or status.
 * Why this adapter is used:
 * - Provides a clean, read-only view of bookmarked questions.
 * - Allows the app to reuse the same QuestionModel data loaded from Firestore.
 * - Helps users review correct answers without allowing interaction.
 * - Keeps UI rendering logic separate from quiz logic handled by QuestionsActivity.
 * How it works:
 * 1. Receives a list of QuestionModel objects representing bookmarked questions.
 * 2. For each item, binds the question text and options to the layout.
 * 3. Checks the question type:
 *    - If true/false: only shows two options (True and False) and hides options C and D.
 *    - Otherwise: shows all four options (A–D).
 * 4. Builds a readable answer string from the correct answers list.
 * 5. Displays the correct answer(s) in the result TextView.
 * 6. Does not show any user-selected answer or attempt status (read-only).
 */


public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {

    private List<QuestionModel> questionList;

    public BookmarkAdapter(List<QuestionModel> questionList) {
        this.questionList = questionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bookmark_layout, parent, false);
        return new BookmarkAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkAdapter.ViewHolder holder, int position) {
        QuestionModel q = questionList.get(position);

        String ques = q.getQuestion();
        String a = q.getOptionA();
        String b = q.getOptionB();
        String c = q.getOptionC();
        String d = q.getOptionD();


        List<Integer> correctAns = q.getCorrectAnswers();

        holder.setData(position, ques, a, b, c, d, correctAns, q.getQuestionType());



    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView quesNo, question, optionA, optionB, optionC, optionD, result;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            quesNo = itemView.findViewById(R.id.quesNo);
            question = itemView.findViewById(R.id.question);
            optionA = itemView.findViewById(R.id.optionA);
            optionB = itemView.findViewById(R.id.optionB);
            optionC = itemView.findViewById(R.id.optionC);
            optionD = itemView.findViewById(R.id.optionD);
            result = itemView.findViewById(R.id.result);

        }


        private void setData(int pos, String ques, String a, String b, String c, String d,
                             List<Integer> correctAns, String questionType) {

            quesNo.setText(itemView.getContext().getString(R.string.question_number, pos + 1));
            question.setText(ques);

            if ("true_false".equals(questionType)) {
                // Only show two options
                optionA.setText(itemView.getContext().getString(R.string.true_option));
                optionB.setText(itemView.getContext().getString(R.string.false_option));

                optionC.setVisibility(View.GONE);
                optionD.setVisibility(View.GONE);

                // Build correct answer string
                StringBuilder answerText = new StringBuilder("Answer: ");
                for (int ansIndex : correctAns) {
                    switch (ansIndex) {
                        case 1:
                            answerText.append("True ");
                            break;
                        case 2:
                            answerText.append("False ");
                            break;
                    }
                }
                result.setText(answerText.toString().trim());

            } else {
                // Default for single_choice / multi_select
                optionA.setText(itemView.getContext().getString(R.string.option_a, a));
                optionB.setText(itemView.getContext().getString(R.string.option_b, b));
                optionC.setText(itemView.getContext().getString(R.string.option_c, c));
                optionD.setText(itemView.getContext().getString(R.string.option_d, d));

                optionC.setVisibility(View.VISIBLE);
                optionD.setVisibility(View.VISIBLE);

                StringBuilder answerText = new StringBuilder("Answer: ");
                for (int ansIndex : correctAns) {
                    switch (ansIndex) {
                        case 1:
                            answerText.append("A. ").append(a).append(" ");
                            break;
                        case 2:
                            answerText.append("B. ").append(b).append(" ");
                            break;
                        case 3:
                            answerText.append("C. ").append(c).append(" ");
                            break;
                        case 4:
                            answerText.append("D. ").append(d).append(" ");
                            break;
                    }
                }
                result.setText(answerText.toString().trim());


            }


        }
    }
}

