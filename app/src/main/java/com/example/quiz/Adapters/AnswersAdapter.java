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
 * AnswersAdapter
 *
 * Purpose:
 * This adapter binds quiz questions and their results to a RecyclerView in the
 * "Answers" screen. It displays each question along with the user’s selected answers
 * and highlights whether the response was correct, incorrect, or unanswered.
 *
 * Why this adapter is used:
 * - Provides a read-only review view that does not allow users to change answers.
 * - Allows the app to reuse the same question model data to display results.
 * - Highlights correct and incorrect answers using clear visual feedback.
 * - Keeps UI rendering logic separate from quiz logic handled by QuestionsActivity.
 *
 * How it works:
 * 1. Receives a list of QuestionModel objects representing the quiz session.
 * 2. For each item, binds the question text and options to the layout.
 * 3. Determines the user’s selected answer(s):
 *    - For single-choice and true/false questions, wraps the selected answer in a list.
 *    - For multi-select questions, uses the selected answer list directly.
 * 4. Compares the selected answers with the correct answers.
 * 5. Updates the UI:
 *    - Shows "CORRECT", "WRONG", or "UN-ANSWERED" in the result TextView.
 *    - Highlights selected options in red if wrong, and correct options in green.
 *    - Resets colors when unanswered.
 * 6. Supports true/false questions by hiding options C and D and displaying only two choices.
 */


public class AnswersAdapter extends RecyclerView.Adapter<AnswersAdapter.ViewHolder> {

    private List<QuestionModel> questionList;//receives the list from the question model

    public AnswersAdapter(List<QuestionModel> questionList) {
        this.questionList = questionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_answers_layout, parent, false);
        return new AnswersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuestionModel q = questionList.get(position);

       String ques = q.getQuestion();
       String a = q.getOptionA();
       String b = q.getOptionB();
       String c = q.getOptionC();
       String d = q.getOptionD();
        List<Integer> selected = new ArrayList<>();
        if ("multi_select".equals(q.getQuestionType())) {
            selected = q.getSelectedAnsList(); // already a list
        } else {
            int sel = q.getSelectedAns();
            if (sel != -1) {
                selected.add(sel); // wrap single int into a list
            }
        }

        List<Integer> correctAns = q.getCorrectAnswers();

        holder.setData(position, ques, a, b, c, d, selected, correctAns, q.getQuestionType());



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
                             List<Integer> selected, List<Integer> correctAns, String questionType) {

            quesNo.setText("Question No. " + (pos + 1));
            question.setText(ques);
            if ("true_false".equals(questionType)) {
                // Show only two options if the question is of type true/false
                optionA.setText("True");
                optionB.setText("False");

                optionC.setVisibility(View.GONE);
                optionD.setVisibility(View.GONE);

                resetOptionColors();

                if (selected.isEmpty()) {
                    result.setText("UN-ANSWERED");
                    result.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
                } else {
                    if (correctAns.containsAll(selected) && selected.containsAll(correctAns)) {
                        result.setText("CORRECT");
                        result.setTextColor(itemView.getContext().getResources().getColor(R.color.green_500));
                        setOptionColors(selected, R.color.green_500);
                    } else {
                        result.setText("WRONG");
                        result.setTextColor(itemView.getContext().getResources().getColor(R.color.red));
                        setOptionColors(selected, R.color.red);
                        setOptionColors(correctAns, R.color.green_500);
                    }
                }

            }
            else {//shows all options for single-choice and multiple-selection question types
                optionA.setText("A. " + a);
                optionB.setText("B. " + b);
                optionC.setText("C. " + c);
                optionD.setText("D. " + d);

                if (selected.isEmpty()) {
                    result.setText("UN-ANSWERED");
                    result.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
                    resetOptionColors();
                } else {
                    if (correctAns.containsAll(selected) && selected.containsAll(correctAns)) {
                        // Correct (all selected match all correct)
                        result.setText("CORRECT");
                        result.setTextColor(itemView.getContext().getResources().getColor(R.color.green_500));
                        setOptionColors(selected, R.color.green_500);
                    } else {
                        // Wrong
                        result.setText("WRONG");
                        result.setTextColor(itemView.getContext().getResources().getColor(R.color.red));
                        setOptionColors(selected, R.color.red);

                        // Highlight correct ones too
                        setOptionColors(correctAns, R.color.green_500);
                    }
                }
            }
        }

        // This method resets the color to the default version
        private void resetOptionColors() {
            optionA.setTextColor(itemView.getContext().getResources().getColor(R.color.text_normal));
            optionB.setTextColor(itemView.getContext().getResources().getColor(R.color.text_normal));
            optionC.setTextColor(itemView.getContext().getResources().getColor(R.color.text_normal));
            optionD.setTextColor(itemView.getContext().getResources().getColor(R.color.text_normal));
        }

        // Highlight multiple options
        private void setOptionColors(List<Integer> options, int color) {
            for (int opt : options) {
                switch (opt) {
                    case 1: optionA.setTextColor(itemView.getContext().getResources().getColor(color)); break;
                    case 2: optionB.setTextColor(itemView.getContext().getResources().getColor(color)); break;
                    case 3: optionC.setTextColor(itemView.getContext().getResources().getColor(color)); break;
                    case 4: optionD.setTextColor(itemView.getContext().getResources().getColor(color)); break;
                }
            }
        }
    }
}
