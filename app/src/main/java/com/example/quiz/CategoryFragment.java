package com.example.quiz;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.example.quiz.Adapters.CategoryAdapter;
/**
 * CategoryFragment
 *
 * Purpose:
 * This fragment displays all quiz categories in a grid layout. Users can select a category
 * to view its associated tests. It serves as the main entry point for starting a quiz.
 *
 * Why this fragment is used:
 * - Shows a list of categories without launching a new activity.
 * - Allows the app to reuse the same UI container (MainActivity) for different sections.
 * - Keeps category loading logic separate from the main activity.
 *
 * How it works:
 * 1. Toolbar Setup:
 * - Sets the toolbar title to "Categories" for clear navigation context.
 * 2. GridView Setup:
 * - Initializes CategoryAdapter using the global category list (DbQuery.g_catList).
 * - Sets the adapter to the GridView to display category cards.
 * 3. Loading Category Data:
 * - If the category list is empty, DbQuery.loadData() is called to fetch categories from Firestore.
 * - On success: adapter.notifyDataSetChanged() refreshes the grid view.
 * - On failure: shows a Toast message indicating load failure.
 * - If categories are already loaded, it simply refreshes the adapter.
 *
 * Notes:
 * - CategoryAdapter handles click navigation to TestActivity when a category is selected.
 * - DbQuery.g_catList must be kept up-to-date for the grid to display correct categories.
 */

public class CategoryFragment extends Fragment {

    private GridView catView;
    private CategoryAdapter adapter;

    public CategoryFragment() {
        // Required empty public constructor
    }

    public static CategoryFragment newInstance(String param1, String param2) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Categories");

        catView = view.findViewById(R.id.cat_Grid);

        //Initialize adapter with the global category list
        adapter = new CategoryAdapter(DbQuery.g_catList);
        catView.setAdapter(adapter);

        // nly load categories if list is empty
        if (DbQuery.g_catList.isEmpty()) {
            DbQuery.loadData(new MyCompleteListener() {
                @Override
                public void onSuccess() {
                    // Refresh adapter once categories are loaded
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure() {
                    Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // If already loaded, just refresh adapter
            adapter.notifyDataSetChanged();
        }

        return view;
    }
}
