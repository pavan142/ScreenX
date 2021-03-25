package com.frankenstein.screenx;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;

import com.frankenstein.screenx.database.ScreenShotEntity;
import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.models.Screenshot;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

public class SearchActivity extends AppCompatActivity {

    private AutoCompleteTextView _mSearch;
    private final Logger _mLogger = Logger.getInstance("SearchActivity");
    private LiveData<ArrayList<Screenshot>> _mLiveMatches = new LiveData<ArrayList<Screenshot>>() {};

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
    }

    public void onLiveMatches(ArrayList<Screenshot> matches) {
        _mLogger.log("number of matches", matches.size());
        for (int i = 0; i < matches.size(); i++) {
            _mLogger.log("Matched", matches.get(i).name);
        }
    }
}
