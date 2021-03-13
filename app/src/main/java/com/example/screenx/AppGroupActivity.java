package com.example.screenx;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

public class AppGroupActivity extends AppCompatActivity {

    private static final int REQUEST_READ_STORAGE = 0;

    private GridView _gridView;
    private ScreensAdapter _adapter;
    private Logger _logger;
    private ScreenFactory _sf;
    private String _appName;
    private SwipeRefreshLayout _pullToRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_grid);
        _logger = Logger.getInstance("FILES");
        _gridView = (GridView)findViewById(R.id.grid_view);
        _sf = ScreenFactory.getInstance(getApplicationContext());
        _sf.initialize();
        _appName = getIntent().getStringExtra("APP_GROUP_NAME");
        _pullToRefresh = findViewById(R.id.pull_to_refresh);
        class refreshTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... strings) {
                refresh();
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                postRefresh();
            }
        }


        _pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new refreshTask().execute();
            }
        });
        attachAdapter();
    }


    private void refresh() {
        _logger.log("MainActivity: Refreshing Screenshots");
        _sf.refresh();
    }

    private void postRefresh() {
        _logger.log("MainActivity: Successfully refreshed data");
        _pullToRefresh.setRefreshing(false);
        attachAdapter();
    }


    private void attachAdapter() {
        AppGroup ag = _sf.appgroups.get(_appName);
        if (ag == null)
            return;
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
                startActivity(intent);
            }
        });
    }
}
