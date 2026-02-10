package com.example.quiz.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quiz.DbQuery;
import com.example.quiz.Models.TestModel;
import com.example.quiz.R;
import com.example.quiz.StartTestActivity;

import java.util.List;
/**
 * TestAdapter
 *
 * Purpose:
 * This adapter binds a list of TestModel objects to a RecyclerView.
 * Each item represents a single test within a category, showing its number,
 * the user’s top score, and a progress bar visualization. It also allows
 * navigation to StartTestActivity when a test is selected.
 *
 * Why this adapter is used:
 * - Provides a structured list of available tests for the user to choose from.
 * - Displays progress feedback (top score percentage) for each test.
 * - Enables direct navigation to start a selected test.
 *
 * How it works:
 * 1. Receives a list of TestModel objects (testList).
 * 2. Adapter overrides:
 *  - onCreateViewHolder() → inflates test_item_layout and creates a ViewHolder.
 *  - onBindViewHolder() → binds data for each test (position and top score).
 *  - getItemCount() → returns the number of tests in the list.
 * 3. ViewHolder:
 *  - Holds references to UI elements (testNo, topScore, progressBar).
 *  - setData():
 *  --Displays the test number (pos + 1).
 *  -- Shows the top score percentage.
 *  -- Updates the progress bar with the score.
 *  -- Sets an OnClickListener:
 *  - Updates DbQuery.g_selected_test_index with the selected test index.
 *  - Starts StartTestActivity via an Intent.
 *
 * Notes:
 * - Progress bar provides a quick visual indicator of performance.
 * - This adapter is UI-driven and depends on Android framework classes.
 * - Unit testing is not required here; testing should be done via instrumentation/UI tests.
 */


public class TestAdapter extends RecyclerView.Adapter<TestAdapter.ViewHolder> {
    private List<TestModel> testList;
    public TestAdapter(List<TestModel> testList){
        this.testList = testList;
    }

    @NonNull
    @Override
    public TestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.test_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestAdapter.ViewHolder holder, int position) {
        int progress = testList.get(position).getTopScore();
        holder.setData(position,progress);

    }

    @Override
    public int getItemCount() {
        return testList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView testNo;
        private TextView topScore;
        private ProgressBar progressBar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            testNo = itemView.findViewById(R.id.testNo);
            topScore = itemView.findViewById(R.id.scoretext);
            progressBar = itemView.findViewById(R.id.testProgressBar);



        }
        private void setData(int pos, int progress){
            testNo.setText("Test No: " + String.valueOf(pos + 1));
            topScore.setText(String.valueOf(progress)+" %");
            progressBar.setProgress(progress);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DbQuery.g_selected_test_index = pos;
                    Intent intent = new Intent(itemView.getContext(), StartTestActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });

        }
    }
}