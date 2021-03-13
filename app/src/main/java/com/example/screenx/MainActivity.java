package com.example.screenx;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_READ_STORAGE = 0;

    private GridView _gridView;
    private AppGroupsAdapter _adapter;
    private Logger _logger;
    private ScreenFactory _sf;
    private SwipeRefreshLayout _pullToRefresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appgroup_grid);
        _logger = Logger.getInstance("FILES");

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
        _gridView = (GridView)findViewById(R.id.grid_view);
        requestStoragePermission();
        _sf = ScreenFactory.getInstance(getApplicationContext());
        _sf.initialize();
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

    public void attachAdapter() {
        ArrayList<Screenshot> mascots = new ArrayList<>();
        ArrayList<AppGroup> appgroups = _sf.getAppGroups(Utils.SortingCriterion.Date);
        for (AppGroup ag: appgroups)
            mascots.add(ag.mascot);
        for (Screenshot s: mascots)
            _logger.log(s.appName,s.name,s.file.getAbsolutePath());
        _adapter = new AppGroupsAdapter(getApplicationContext(), mascots);
        _gridView.setAdapter(_adapter);
        _gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Screenshot selected = mascots.get(i);
                _logger.log("The app group selected is", i, "the appName: ", selected.appName);
                Intent intent = new Intent(getBaseContext(), AppGroupActivity.class);
                intent.putExtra("APP_GROUP_NAME", selected.appName);
                startActivity(intent);
            }
        });
    }

    private void requestStoragePermission() {
        // Getting Permission for reading exteernal storage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
        }
    }
}
