package com.frankenstein.screenx;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.frankenstein.screenx.database.ScreenShotEntity;
import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.helper.TimeLogger;
import com.frankenstein.screenx.models.Screenshot;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;

import static com.frankenstein.screenx.helper.ArrayHelper.Same;
import static com.frankenstein.screenx.helper.SortHelper.DESC_SCREENS_BY_TIME;

public class SearchActivity extends MultipleSelectActivity {

    private AppCompatAutoCompleteTextView _mSearch;
    private final Logger _mLogger = Logger.getInstance("SearchActivity");
    private final Logger _mTimeLogger = TimeLogger.getInstance();
    private LiveData<List<String>> _mLiveMatches;
    private ArrayList<String> _mMatches;
    private ImageButton _mClearSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.searchpage);
        super.onCreate(savedInstanceState);
        _mSearch = findViewById(R.id.automcomplete_search);
        _mClearSearch = findViewById(R.id.clear_search_bar);
        _mLiveMatches = ScreenXApplication.textHelper.searchScreenshots("");
        _mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                _mLiveMatches.removeObservers(SearchActivity.this);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                _mTimeLogger.log("requesting search");
                _mLiveMatches = ScreenXApplication.textHelper.searchScreenshots(s.toString());
                checkAndToggleClear(count);
            }

            @Override
            public void afterTextChanged(Editable s) {
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

    public void onLiveMatches(List<String> matches) {
        _mTimeLogger.log("Got matches", matches.size());
        ArrayList<String> screens = new ArrayList<>();
        _mLogger.log("Total matched screenshots by search = ", matches.size());
        for (String name: matches) {
            Screenshot screen = ScreenXApplication.screenFactory.findScreenByName(name);
            if ( screen != null)
                screens.add(name);
        }
        DESC_SCREENS_BY_TIME(screens);
        updateAdapter(screens);
    }

    private void updateAdapter(ArrayList<String> matches) {
        ArrayList<Screenshot> newScreens = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
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
