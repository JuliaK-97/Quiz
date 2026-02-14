package com.example.quiz.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quiz.DbQuery;
import com.example.quiz.Models.RankModel;
import com.example.quiz.R;

import java.util.List;

/**
 * RankAdapter
 * Purpose:
 * This adapter binds user ranking data to the leaderboard RecyclerView.
 * It displays the top users in the system based on their TOTAL_SCORE
 * (sum of all test scores) along with their rank and name.
 * Why this adapter is used:
 * - Powers the leaderboard UI, showing each user's rank, name, and overall score.
 * - Keeps UI binding logic separate from the Activity/Fragment.
 * - Ensures consistency with DbQuery ranking logic (sorted by TOTAL_SCORE).
 * - Limits display to the top 10 users for readability and performance.
 * How it works:
 * 1. Receives a list of RankModel objects (userList) containing name, overall score, and rank.
 * 2. For each item:
 *    - Inflates rank_item_layout.
 *    - Binds user name, rank, and overall score to TextViews.
 *    - Displays the first letter of the user’s name as a circular initial.
 * 3. If the current user is present in the list, it uses DbQuery.myPerformance to show the
 *    most up-to-date information for the current user.
 * 4. Limits the displayed users to the top 10 using Math.min().
 *
 * Notes:
 * - This adapter is UI-driven and depends on Android framework classes.
 * - Unit testing is not required; testing should be done via instrumentation/UI tests.
 */
public class RankAdapter extends RecyclerView.Adapter<RankAdapter.ViewHolder> {
    private List<RankModel> userList;

    public RankAdapter(List<RankModel> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public RankAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rank_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankAdapter.ViewHolder holder, int position) {
        RankModel user = userList.get(position);

        //If this is the current user, use myPerformance (same as Leaderboard/Account)
        if (DbQuery.myPerformance.getName().equals(user.getName())) {
            holder.setData(//for each user you want to get their name, rank and overall score from the database
                    DbQuery.myPerformance.getName(),
                    DbQuery.myPerformance.getOverallScore(),
                    DbQuery.myPerformance.getRank()
            );
        } else {
            // Otherwise, use the values from the RankModel list
            holder.setData(
                    user.getName(),
                    user.getOverallScore(),
                    user.getRank()
            );
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(userList.size(), 10); //this method limits the displayed users to the top 10 users only (saves memory)
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTV, rankTV, scoreTV, imgTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.name);
            rankTV = itemView.findViewById(R.id.rank);
            scoreTV = itemView.findViewById(R.id.score);
            imgTV = itemView.findViewById(R.id.img_text);
        }
        //The setData method is used to set the fetched values to the textviews and imageviews.
        private void setData(String name, int overallScore, int rank) {
            nameTV.setText(name);
            scoreTV.setText("Score: " + overallScore);
            rankTV.setText("Rank - " + rank);
            imgTV.setText(name.toUpperCase().substring(0, 1));//Displays the first letter of the users name in the circle imageview
        }
    }
}
