package com.frankenstein.screenx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.models.AppGroup;
import com.frankenstein.screenx.models.Screenshot;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

public class AppGroupActivity extends MultipleSelectActivity {

    private Logger _logger;
    private String _appName;
    private TextView _mTextView;
    private SwipeRefreshLayout _pullToRefresh;
    private static final int SCREEN_ACTIVITY_INTENT_CODE = 1;
    private boolean _mPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.appgrouppage);
        super.onCreate(savedInstanceState);
        _logger = Logger.getInstance("AppGroupActivity");
        _appName = getIntent().getStringExtra("APP_GROUP_NAME");
        _pullToRefresh = findViewById(R.id.pull_to_refresh);
        _pullToRefresh.setOnRefreshListener(() -> refresh());
        _mTextView = findViewById(R.id.appgrouppage_title);
        _mTextView.setText(_appName);
        ScreenXApplication.screenFactory.screenshots.observe(this, this::postRefresh);
        updateAdapter();
    }


    private void refresh() {
        _logger.log("AppGroupActivity: Refreshing Screenshots");
        ScreenXApplication.screenFactory.refresh(this);
    }

    private void postRefresh(ArrayList<Screenshot> screens) {
        _logger.log("AppGroupActivity: Successfully refreshed data");
        _pullToRefresh.setRefreshing(false);
        updateAdapter();
    }


    @Override
    protected void updateAdapter() {
        AppGroup ag = ScreenXApplication.screenFactory.appgroups.get(_appName);
        if (ag == null || ag.screenshots.size() == 0) {
            finish();
            return;
        }
        boolean sameData = ag.screenshots.size() == mScreens.size();
        if (sameData) {
            for (int i = 0; i < ag.screenshots.size(); i++) {
                if (!ag.screenshots.get(i).name.equals(mScreens.get(i).name)) {
                    sameData = false;
                    break;
                }
            }
        }
        if (sameData)
            return;
        mScreens = (ArrayList<Screenshot>) ag.screenshots.clone();
        _logger.log("Displaying Scrrens of Appgroup", _appName, mScreens.size());
        super.updateAdapter();
    }

    @Override
    public Intent getTapIntent(Screenshot selected, int position) {
        Intent intent = new Intent(getBaseContext(), ScreenActivity.class);
        intent.putExtra(ScreenActivity.INTENT_SCREEN_NAME, selected.name);
        intent.putExtra(ScreenActivity.INTENT_SCREEN_POSITION, position);
        intent.putExtra(ScreenActivity.INTENT_DISPLAY_TYPE, ScreenActivity.DISPLAY_APP_GROUP_ITEMS);
        return intent;
    }

    @Override
    protected void onResume() {
        if (_mPaused)
            refresh();
        _mPaused = false;
        super.onResume();
    }

    @Override
    protected void onPause() {
        _mPaused = true;
        super.onPause();
    }
}
