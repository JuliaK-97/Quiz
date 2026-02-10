package com.example.quiz;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quiz.Adapters.RankAdapter;

/**
 * LeaderboardFragment
 *
 * Purpose:
 * This fragment displays the global leaderboard of users ranked by their TOTAL_SCORE
 * (sum of all top scores across tests). It shows:
 * - The top 20 users retrieved from Firestore.
 * - The current user's score and rank.
 * - The total number of users in the system.
 *
 * Why this fragment is used:
 * - Provides a competitive view of user performance across the app.
 * - Ensures fairness by ranking based on overall score rather than just one test.
 * --so if one user does 5 tests and gets 100 for each test he will be ranked higher than someone that did 2 tests and got 100% for both
 * - Allows the current user to see their position relative to others.
 *
 * How it works:
 * 1. Initializes the leaderboard UI (RecyclerView, TextViews, progress dialog).
 * 2. Calls DbQuery.getTopUsers() to fetch the top 20 users sorted by TOTAL_SCORE.
 * 3. Updates the adapter with the retrieved data.
 * 4. If the current user is not in the top 20, calculates their rank using calculateRank().
 * 5. Displays the user's TOTAL_SCORE and rank in the UI.
 * 6. Handles errors gracefully with a Toast message.
 */
public class LeaderboardFragment extends Fragment {

    private TextView totalUsersTV, imageTV, myScoreTV, myRankTV;
    private RecyclerView usersView;
    private RankAdapter adapter;
    private Dialog progressDialog;
    private TextView dialogText;

    public LeaderboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("LeaderBoard");

        initViews(view);

        // Setup progress dialog
        progressDialog = new Dialog(getContext());
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = progressDialog.findViewById(R.id.dialog_text);
        dialogText.setText("Loading...");
        progressDialog.show();

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        usersView.setLayoutManager(layoutManager);

        adapter = new RankAdapter(DbQuery.g_usersList);
        usersView.setAdapter(adapter);

        // Fetch top users from Firestore
        DbQuery.getTopUsers(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                adapter.notifyDataSetChanged();
                if (DbQuery.myPerformance.getOverallScore() != 0) { //  use overallScore
                    if (!DbQuery.isMe0nTopList) { // if user is not in top 20, calculate rank
                        calculateRank();
                    }
                    myScoreTV.setText("Score: " + DbQuery.myPerformance.getOverallScore()); // ✅ show overallScore
                    myRankTV.setText("Rank - " + DbQuery.myPerformance.getRank());
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure() {
                Toast.makeText(getContext(), "Something went wrong! Please try again!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });

        totalUsersTV.setText("Total Users: " + DbQuery.g_usersCount);
        imageTV.setText(DbQuery.myPerformance.getName().toUpperCase().substring(0, 1));
        return view;
    }

    private void initViews(View view) {
        totalUsersTV = view.findViewById(R.id.lb_total_users);
        imageTV = view.findViewById(R.id.lb_img_tv);
        myScoreTV = view.findViewById(R.id.lb_score);
        myRankTV = view.findViewById(R.id.lb_rank);
        usersView = view.findViewById(R.id.lb_users_view);
    }

    /**
     * calculateRank
     *
     * Purpose:
     * Calculates the current user's rank if they are not in the top 20 list.
     *
     * How it works:
     * - Finds the lowest 0verall score among the top 20 users.
     * - Estimates the user's slot among the remaining users based on their Overall score.
     * - Assigns a rank accordingly.
     */
    private void calculateRank() {
        int lowTopScore = DbQuery.g_usersList.get(DbQuery.g_usersList.size() - 1).getOverallScore(); // ✅ use overallScore
        int remaining_slots = DbQuery.g_usersCount - 20;
        int mySlot = (DbQuery.myPerformance.getOverallScore() * remaining_slots) / lowTopScore; // ✅ use overallScore
        int rank;
        if (lowTopScore != DbQuery.myPerformance.getOverallScore()) {
            rank = DbQuery.g_usersCount - mySlot;
        } else {
            rank = 21;
        }
        DbQuery.myPerformance.setRank(rank);
    }
}
