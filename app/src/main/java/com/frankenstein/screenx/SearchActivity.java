package com.frankenstein.screenx;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.models.Screenshot;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;

import static com.frankenstein.screenx.helper.ArrayHelper.Same;

public class SearchActivity extends MultipleSelectActivity {

    private AppCompatAutoCompleteTextView _mSearch;
    private final Logger _mLogger = Logger.getInstance("SearchActivity");
    private LiveData<ArrayList<String>> _mLiveMatches = new LiveData<ArrayList<String>>() {};
    private ArrayList<String> _mMatches;
    private ImageButton _mClearSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.searchpage);
        super.onCreate(savedInstanceState);
        _mSearch = findViewById(R.id.automcomplete_search);
        _mClearSearch = findViewById(R.id.clear_search_bar);
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
                checkAndToggleClear(count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                _mLogger.log("afterTextChagned", s.toString());
                _mLiveMatches.observe(SearchActivity.this, SearchActivity.this::onLiveMatches);
            }
        });
        _mSearch.requestFocus();
        _mClearSearch.setOnClickListener((View view) -> {
            _mSearch.setText("");
        });
    }

    public void checkAndToggleClear(int count) {
        if (count > 0) {
            _mClearSearch.setVisibility(View.VISIBLE);
        } else {
            _mClearSearch.setVisibility(View.GONE);
        }
    }

    public void onLiveMatches(ArrayList<String> matches) {
        _mLogger.log("number of matches", matches.size());
        updateAdapter(matches);
    }

    private void updateAdapter(ArrayList<String> matches) {
        ArrayList<Screenshot> newScreens = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            _mLogger.log("Matched", matches.get(i));
            Screenshot screen = ScreenXApplication.screenFactory.findScreenByName(matches.get(i));
            if (screen != null)
                newScreens.add(screen);
        }
        if (Same(newScreens, mScreens))
            return;
        mScreens = newScreens;
        _mMatches = matches;
        _mLogger.log("Displaying grid with items = ", mScreens.size());
        super.updateAdapter();
    }

    @Override
    public Intent getTapIntent(Screenshot selected, int position) {
        Intent intent = new Intent(getBaseContext(), ScreenActivity.class);
        intent.putExtra(ScreenActivity.INTENT_SCREEN_NAME, selected.name);
        intent.putExtra(ScreenActivity.INTENT_SCREEN_POSITION, position);
        intent.putExtra(ScreenActivity.INTENT_SEARCH_MATCHES, _mMatches);
        intent.putExtra(ScreenActivity.INTENT_DISPLAY_TYPE, ScreenActivity.DISPLAY_SEARCH_RESULTS);
        return intent;
    }
}
