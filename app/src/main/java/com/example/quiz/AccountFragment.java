package com.example.quiz;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

/**
 * AccountFragment
 *
 * Purpose:
 * This fragment provides a personal dashboard for the user. It displays the user’s
 * total score, rank, name, and profile initials. It also provides quick access to
 * related screens such as bookmarks, profile, and leaderboard.
 *
 * Why this fragment is used:
 * - Shows the user’s overall performance (TOTAL_SCORE) in one place.
 * - Offers navigation to bookmarks, profile, and leaderboard without leaving the fragment.
 * - Keeps account-related logic separate from other screens for better organization.
 *
 * How it works:
 * 1. View Initialization:
 *  - initViews() binds all UI elements (buttons, text views, profile image).
 *  - Toolbar title is set to "My Account".
 * 2. Profile Display:
 *  - The user’s name is fetched from DbQuery.myProfile.
 *  - The first letter of the name is shown as the profile icon.
 * 3. Loading Rank & Score:
 *  - A progress dialog is shown while fetching leaderboard data.
 *  - DbQuery.getTopUsers() loads the top users.
 *  - If the user is not in the top 20, calculateRank() estimates the rank.
 *  - Total score and rank are then displayed.
 * 4. Button Actions:
 * - Logout:
 * --Signs out using FirebaseAuth.
 * -- Opens LoginActivity2 and clears the activity stack.
 * - Bookmarks:
 * -- Opens BookMarksActivity to show bookmarked questions.
 * - Profile:
 * -- Opens MyProfileActivity for editing profile details.
 * - Leaderboard:
 * -- Switches bottom navigation to the leaderboard tab.
 *
 * Notes:
 * - This fragment depends on DbQuery global data (myProfile, myPerformance).
 * - UI updates are done only after data is successfully loaded.
 */
public class AccountFragment extends Fragment {
    private LinearLayout logoutBtn, bookmarkBtn, leaderboardBtn, profileBtn;
    private TextView rank, totalScoreTV, profile_img_text, nameTV;
    private ImageView profileImage;
    private BottomNavigationView bottomNavigationView;
    private Dialog progressDialog;
    private TextView dialogText;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        initViews(view);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("My Account");

        progressDialog = new Dialog(getContext());
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText = progressDialog.findViewById(R.id.dialog_text);
        dialogText.setText("Loading...");

        String userName = DbQuery.myProfile.getName();
        profile_img_text.setText(userName.toUpperCase().substring(0, 1));
        nameTV.setText(userName);

        progressDialog.show();
        DbQuery.getTopUsers(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                if (DbQuery.myPerformance.getOverallScore() != 0) {
                    if (!DbQuery.isMe0nTopList) { // if user not in top 20, calculate rank
                        calculateRank();
                    }
                    totalScoreTV.setText(String.valueOf(DbQuery.myPerformance.getOverallScore()));
                    rank.setText(String.valueOf(DbQuery.myPerformance.getRank()));
                } else {
                    totalScoreTV.setText("0");
                    rank.setText("NA");
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure() {
                Toast.makeText(getContext(), "Something went wrong! Please try again!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });

        // The logout button will take the user back to the login page through the use of an intent.
        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getContext(), LoginActivity2.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });
        // The bookmark button will take the user back to the questions that have been bookmarked, done through the use of an intent.
        bookmarkBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), BookMarksActivity.class);
            startActivity(intent);
        });
        // The profile Button will take the user to their profile page through the use of an intent.

        profileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MyProfileActivity.class);
            startActivity(intent);
        });
        // The leader board button will take the user to the leader board page through the use of an intent.

        leaderboardBtn.setOnClickListener(v -> {
            bottomNavigationView.setSelectedItemId(R.id.btm_nav_leaderboard);
        });

        return view;
    }

    private void initViews(View view) {
        logoutBtn = view.findViewById(R.id.logoutBtn);
        bookmarkBtn = view.findViewById(R.id.bookmark_btn);
        leaderboardBtn = view.findViewById(R.id.myA_leaderB_btn);
        profileBtn = view.findViewById(R.id.profile_btn);
        rank = view.findViewById(R.id.rank);
        totalScoreTV = view.findViewById(R.id.ma_totalScoreTV);
        profileImage = view.findViewById(R.id.profile_img);
        profile_img_text = view.findViewById(R.id.profile_img_text);
        nameTV = view.findViewById(R.id.ma_name);
        bottomNavigationView = getActivity().findViewById(R.id.bottom_nav_bar);
    }

    /**
     * calculateRank()
     * Purpose:
     * Calculates the current user's rank if they are not in the top 20 list.
     * How it works is that it first finds the lowest overallScore among the top 20 users, from there it calculate the number of remaining slots
     * The equation is remaining slots = user count - 20
     * Tt then estimates the user's slot among the remaining users based on their overallScore
     * The equation for this is (overall score * remaining slots)/lowTopScore
     * it then assigns the rank accordingly
     */
    private void calculateRank() {
        int lowTopScore = DbQuery.g_usersList.get(DbQuery.g_usersList.size() - 1).getOverallScore();
        int remaining_slots = DbQuery.g_usersCount - 20;
        int mySlot = (DbQuery.myPerformance.getOverallScore() * remaining_slots) / lowTopScore;
        int rank;
        if (lowTopScore != DbQuery.myPerformance.getOverallScore()) {
            rank = DbQuery.g_usersCount - mySlot;
        } else {
            rank = 21;//if the users rank has the same score as the 20th rank user they will be given the rank 21
        }
        DbQuery.myPerformance.setRank(rank);
    }
}
