package com.example.quiz;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;

/**
 * MainActivity
 *
 * Purpose:
 * This activity acts as the landing page of the quiz application after login.
 * It provides a unified navigation experience that allows users to move between
 * the core sections of the app: Categories/Home, Leaderboard, and Account.
 *
 * Why this activity is used:
 * - It acts as a central hub for all primary fragments
 * - Combines a BottomNavigationView and a Navigation Drawer to offer flexible
 *   and intuitive navigation options.
 * - Displays user profile information in the navigation drawer to personalize
 *   the user experience.
 * - Keeps navigation logic centralized rather than duplicating it across fragments.
 *
 * How it works:
 * 1. Sets up a Toolbar as the app’s ActionBar and displays the default title ("Categories").
 * 2. Initializes a DrawerLayout with an ActionBarDrawerToggle to integrate the
 *    navigation drawer with the Toolbar.
 * 3. Configures a BottomNavigationView to allow quick switching between
 *    Categories, Leaderboard, and Account sections.
 * 4. Listens for navigation selections from both the bottom navigation bar
 *    and the navigation drawer, ensuring both routes load the same fragments.
 * 5. Loads and swaps fragments (CategoryFragment, LeaderboardFragment,
 *    AccountFragment) using setFragment(), avoiding unnecessary reloads.
 * 6. Retrieves the user’s profile data from DbQuery.myProfile and displays the
 *    user’s name and initial in the drawer header.
 * 7. Closes the navigation drawer automatically after a selection to maintain
 *    a smooth and consistent user experience.
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private FrameLayout main_frame;
    private TextView drawerProfileName, drawerProfileText;

    private final NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                int id = item.getItemId();
                if (id == R.id.btm_nav_home) {
                    setFragment(new CategoryFragment());
                    return true;
                } else if (id == R.id.btm_nav_leaderboard) {
                    setFragment(new LeaderboardFragment());
                    return true;
                } else if (id == R.id.btm_nav_account) {
                    setFragment(new AccountFragment());
                    return true;
                }
                return false;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Categories");

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        bottomNavigationView = findViewById(R.id.bottom_nav_bar);
        bottomNavigationView.setOnItemSelectedListener(navListener);

        main_frame = findViewById(R.id.main_frame);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerProfileName = navigationView.getHeaderView(0).findViewById(R.id.nav_drawer_name);
        drawerProfileText = navigationView.getHeaderView(0).findViewById(R.id.nav_drawer_text_image);

        String name = DbQuery.myProfile.getName();
        drawerProfileName.setText(name);
        drawerProfileText.setText(name.toUpperCase().substring(0, 1));

        bottomNavigationView.setSelectedItemId(R.id.btm_nav_home);
    }

    private void setFragment(Fragment fragment) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(main_frame.getId());
        if (currentFragment != null && currentFragment.getClass() == fragment.getClass()) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(main_frame.getId(), fragment);
        transaction.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(main_frame.getId());

        if (id == R.id.nav_home) {
            if (!(currentFragment instanceof CategoryFragment)) {
                setFragment(new CategoryFragment());
            }
            bottomNavigationView.setSelectedItemId(R.id.btm_nav_home);
        } else if (id == R.id.nav_leaderboard) {
            if (!(currentFragment instanceof LeaderboardFragment)) {
                setFragment(new LeaderboardFragment());
            }
            bottomNavigationView.setSelectedItemId(R.id.btm_nav_leaderboard);
        } else if (id == R.id.nav_account) {
            if (!(currentFragment instanceof AccountFragment)) {
                setFragment(new AccountFragment());
            }
            bottomNavigationView.setSelectedItemId(R.id.btm_nav_account);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
