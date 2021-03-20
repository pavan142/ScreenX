package com.frankenstein.screenx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.models.AppGroup;
import com.frankenstein.screenx.models.Screenshot;
import com.frankenstein.screenx.ui.adapters.ScreensAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

public class AppGroupActivity extends AppCompatActivity {

    private GridView _gridView;
    private ScreensAdapter _adapter;
    private Logger _logger;
    private ScreenFactory _sf;
    private String _appName;
    private SwipeRefreshLayout _pullToRefresh;
    private static final int SCREEN_ACTIVITY_INTENT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_grid);
        _logger = Logger.getInstance("FILES");
        _gridView = findViewById(R.id.grid_view);
        _sf = ScreenFactory.getInstance();
        _appName = getIntent().getStringExtra("APP_GROUP_NAME");
        _pullToRefresh = findViewById(R.id.pull_to_refresh);
        _pullToRefresh.setOnRefreshListener(() -> refresh());
        updateAdapter();
    }


    private void refresh() {
        _logger.log("AppGroupActivity: Refreshing Screenshots");
        _sf.refresh(getApplicationContext(), () -> postRefresh());
    }

    private void postRefresh() {
        _logger.log("AppGroupActivity: Successfully refreshed data");
        _pullToRefresh.setRefreshing(false);
        updateAdapter();
    }


    private void updateAdapter() {
        AppGroup ag = _sf.appgroups.get(_appName);
        if (ag == null || ag.screenshots.size() == 0) {
            finish();
            return;
        }
        ArrayList<Screenshot> screens = ag.screenshots;
        _logger.log("Displaying Scrrens of Appgroup", _appName, screens.size());
        _adapter = new ScreensAdapter(getApplicationContext(), screens);
        _gridView.setAdapter(_adapter);
        _gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Screenshot selected = screens.get(i);
                _logger.log("The screen selected is", i, selected.name, "the appName: ", selected.appName);
                Intent intent = new Intent(getBaseContext(), ScreenActivity.class);
                intent.putExtra("SCREEN_NAME", selected.name);
                intent.putExtra("SCREEN_POSITION", i);
                startActivityForResult(intent, SCREEN_ACTIVITY_INTENT_CODE);
            }
        });
    }

    @Override
    protected void onResume() {
        refresh();
        super.onResume();
    }
}
