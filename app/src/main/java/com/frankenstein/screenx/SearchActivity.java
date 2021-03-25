package com.frankenstein.screenx;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;

import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.models.Screenshot;
import com.frankenstein.screenx.ui.adapters.SearchPageAdapter;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

public class SearchActivity extends AppCompatActivity {

    private AutoCompleteTextView _mSearch;
    private final Logger _mLogger = Logger.getInstance("SearchActivity");
    private LiveData<ArrayList<String>> _mLiveMatches = new LiveData<ArrayList<String>>() {};
    private GridView _mGridView;
    private SearchPageAdapter _mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchpage);
        _mSearch = findViewById(R.id.automcomplete_search);
        _mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                _mLiveMatches.removeObservers(SearchActivity.this);
                _mLogger.log("beforeTextChagned", s, start, count, after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                _mLogger.log("onTextChagned", s, start, before, count);
                _mLiveMatches = ScreenXApplication.textHelper.searchScreenshots(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                _mLogger.log("afterTextChagned", s.toString());
                _mLiveMatches.observe(SearchActivity.this, SearchActivity.this::onLiveMatches);
            }
        });
        _mSearch.requestFocus();
        _mGridView = findViewById(R.id.grid_view);
    }

    public void onLiveMatches(ArrayList<String> matches) {
        _mLogger.log("number of matches", matches.size());
        updateAdapter(matches);
    }

    private void updateAdapter(ArrayList<String> matches) {
        ArrayList<Screenshot> screens = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            _mLogger.log("Matched", matches.get(i));
            screens.add(ScreenXApplication.screenFactory.findScreenByName(matches.get(i)));
        }
        _mLogger.log("Displaying grid with items = ", screens.size());
        _mAdapter = new SearchPageAdapter(getApplicationContext(), screens);
        _mGridView.setAdapter(_mAdapter);
        _mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Screenshot selected = screens.get(i);
                _mLogger.log("The screen selected is", i, selected.name, "the appName: ", selected.appName);
                Intent intent = new Intent(getBaseContext(), ScreenActivity.class);
                intent.putExtra(ScreenActivity.INTENT_SCREEN_NAME, selected.name);
                intent.putExtra(ScreenActivity.INTENT_SCREEN_POSITION, i);
                intent.putExtra(ScreenActivity.INTENT_SEARCH_MATCHES, matches);
                intent.putExtra(ScreenActivity.INTENT_DISPLAY_TYPE, ScreenActivity.DISPLAY_SEARCH_RESULTS);
                startActivity(intent);
            }
        });
    }
}
