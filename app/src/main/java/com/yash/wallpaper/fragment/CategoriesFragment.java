package com.yash.wallpaper.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.yash.wallpaper.R;
import com.yash.wallpaper.activity.CategoryActivity;
import com.yash.wallpaper.adapter.CategoryAdapter;
import com.yash.wallpaper.communication.CommunicationHelper;

public class CategoriesFragment extends Fragment implements ValueEventListener {

    private CategoryAdapter adapter;
    private GridView categories;

    public CategoriesFragment() {
    }

    public static Fragment newInstance() {
        CategoriesFragment categoriesFragment = new CategoriesFragment();
        return categoriesFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        init(view);
        initCategorySelectListener();

        return view;
    }

    private void initCategorySelectListener() {
        categories.setOnItemClickListener((adapterView, view1, i, l) -> {
            triggerIntent(i);
        });
    }

    private void triggerIntent(int i) {
        Intent intent = new Intent(getContext(), CategoryActivity.class);
        intent.putExtra("category", i);
        startActivity(intent);
    }

    private void init(View view) {
        categories = view.findViewById(R.id.categories_gridview);
        adapter = new CategoryAdapter(getContext(), CommunicationHelper.WALLPAPER_CATEGORIES);
        categories.setAdapter(adapter);
    }


    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
