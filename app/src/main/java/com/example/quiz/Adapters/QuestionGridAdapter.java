package com.example.quiz.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.quiz.DbQuery;
import com.example.quiz.Models.QuestionModel;
import com.example.quiz.QuestionsActivity;
import com.example.quiz.R;

/**
 * QuestionGridAdapter
 * Purpose:
 * This adapter binds quiz questions to a GridView inside QuestionsActivity.
 * It displays each question number and uses color-coded backgrounds to represent
 * the question’s current status (answered, unanswered, not visited, or marked for review).
 * It also allows users to quickly navigate to any question by tapping the grid item.
 * Why this adapter is used:
 * - Provides a compact overview of quiz progress.
 * - Helps users track which questions still need attention.
 * - Allows direct navigation to any question in the quiz.
 * - Keeps the grid UI logic separate from QuestionsActivity.
 * How it works:
 * 1. Receives the total number of questions (numOfQues).
 * 2. For each grid item:
 *   - Inflates the layout (ques_grid_item) if no recycled view is available.
 *   - Sets the question number (i + 1).
 *   - Retrieves the question status from DbQuery.g_questionList.
 *   - Applies a background tint color based on status:
 *   -- ANSWERED  -> Green
 *   -- UNANSWERED -> Red
 *   -- NOT_VISITED -> Grey
 *   -- REVIEW -> Pink
 *   - Adds an OnClickListener that calls QuestionsActivity.goToQuestion(i) to jump directly to that question.
 * Notes:
 * - This adapter is UI-driven and relies on Android framework components.
 * - Unit testing is not required; testing should be done via instrumentation/UI tests.
 */

public class QuestionGridAdapter extends BaseAdapter {
    private int numOfQues;

    public QuestionGridAdapter(int numOfQues) {
        this.numOfQues = numOfQues;
    }

    @Override
    public int getCount() {
        return numOfQues;
    }

    @Override
    public Object getItem(int i) {
        return DbQuery.g_questionList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View myview;
        if(view == null)
        {
          myview = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ques_grid_item,viewGroup,false);
        }//If no recycled view is available the getView() will inflate the ques_grid_item layout.
        else
        {
            myview = view;
        }

        TextView quesTV = myview.findViewById(R.id.ques_num);//Here the method displays the question number in a textview
        quesTV.setText(String.valueOf(i+1));
        int status = DbQuery.g_questionList.get(i).getStatus();//here the method retrieves the status from the global question_list - from DbQuery

        switch (status) {
            case QuestionModel.ANSWERED:
                quesTV.setBackgroundTintList(
                        ContextCompat.getColorStateList(viewGroup.getContext(), R.color.green)
                );
                break;

            case QuestionModel.UNANSWERED:
                quesTV.setBackgroundTintList(
                        ContextCompat.getColorStateList(viewGroup.getContext(), R.color.red)
                );
                break;

            case QuestionModel.NOT_VISITED:
                quesTV.setBackgroundTintList(
                        ContextCompat.getColorStateList(viewGroup.getContext(), R.color.grey)
                );
                break;

            case QuestionModel.REVIEW:
                quesTV.setBackgroundTintList(
                        ContextCompat.getColorStateList(viewGroup.getContext(), R.color.pink)
                );
                break;
            default:
                break;


        }

        myview.setOnClickListener(v -> {
            if (viewGroup.getContext() instanceof QuestionsActivity) {
                ((QuestionsActivity) viewGroup.getContext()).goToQuestion(i);
            }
        });




        return myview;
    }
}
