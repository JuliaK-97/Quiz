package com.example.quiz.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.quiz.DbQuery;
import com.example.quiz.Models.CategoryModel;
import com.example.quiz.R;
import com.example.quiz.TestActivity;

import java.util.List;

/**
 * CategoryAdapter
 *
 * Purpose:
 * This adapter displays all quiz categories in a GridView/ListView on the home screen.
 * Each category item shows the category name and the number of tests available.
 * When a category is clicked, it navigates the user to the TestActivity for that category.
 *
 * Why this adapter is used:
 * - Separates UI binding logic from Activity code (cleaner architecture).
 * - Provides a reusable way to show categories using a GridView/ListView.
 * - Handles click events to navigate users to the correct category tests.
 * - Keeps the category list dynamic using setCategories() for updates.
 *
 * How it works:
 * 1. Receives a list of CategoryModel objects representing available categories.
 * 2. For each category item:
 * - Inflates the category layout (cat_item_layout).
 * - Sets category name and number of tests.
 * - Adds an OnClickListener that:
 *   -- Sets the selected category index in DbQuery (g_selected_cat_index).
 *   -- Starts TestActivity to show tests in that category.
 * 3. Uses BaseAdapter methods to provide item count, item retrieval, and view recycling.
 *
 * Notes:
 * - This adapter is UI-driven; it relies on Android framework components.
 * - Unit testing is not required; testing should be done via instrumentation/UI tests.
 */

public class CategoryAdapter extends BaseAdapter {
    private List<CategoryModel> cat_list;

    public CategoryAdapter(List<CategoryModel> cat_list) {
        this.cat_list = cat_list;
    }

    // The setCategories method allows one to update the category list dynamically while also refreshing the UI
    public void setCategories(List<CategoryModel> newList) {
        this.cat_list = newList;
        notifyDataSetChanged();
    }
    //The getCount method returns the number of categories
    @Override
    public int getCount() {
        return cat_list.size();
    }
    //The getItem method returns the category at position i.
    @Override
    public Object getItem(int i) {
        return cat_list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i; // this method returns the position as a stable ID
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        View myView;
        if (view == null) {
            myView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.cat_item_layout, viewGroup, false);
        } else {
            myView = view;
        }

        myView.setOnClickListener(v -> {
            DbQuery.g_selected_cat_index = i;
            Intent intent = new Intent(v.getContext(), TestActivity.class);
            v.getContext().startActivity(intent);
        });

        TextView catName = myView.findViewById(R.id.cat_name);
        TextView noOfTests = myView.findViewById(R.id.no_of_tests);

        CategoryModel category = cat_list.get(i);
        catName.setText(category.getName());
        noOfTests.setText("Tests: " + category.getNoOfTests());

        return myView;
    }
}
