package com.example.development_01.core.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import com.example.development_01.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private ImageButton backButton;
    private ChipGroup chipGroupSuggestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 1. Get View components by their respective id's
        searchView = findViewById(R.id.searchView);
        backButton = findViewById(R.id.imageButton);
        chipGroupSuggestions = findViewById(R.id.chipGroupSuggestions);

        // 2. Return to Dashboard on backButton
        backButton.setOnClickListener(v -> {
            finish();
        });

        // 3. Handle Keyboard Search Submissions
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    launchPostSearch(query.trim());
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // 4. Handle "Suggested Searches" Chip Clicks
        // Using individual click listeners is more reliable for Action chips
        for (int i = 0; i < chipGroupSuggestions.getChildCount(); i++) {
            View child = chipGroupSuggestions.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setOnClickListener(v -> {
                    String chipText = chip.getText().toString();
                    searchView.setQuery(chipText, false);
                    launchPostSearch(chipText);
                });
            }
        }
    }

    // 5. Helper Methods to start new activity
    private void launchPostSearch(String searchQuery) {
        Intent intent = new Intent(SearchActivity.this, PostSearchActivity.class);
        intent.putExtra("SEARCH_QUERY", searchQuery);
        startActivity(intent);
    }
}