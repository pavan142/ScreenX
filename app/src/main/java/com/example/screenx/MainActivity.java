package com.example.screenx;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;

import static com.example.screenx.Constants.PROGRESS_BAR_PERIOD;
import static com.example.screenx.Constants.PROGRESS_BAR_TRANSITION;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_READ_STORAGE = 0;

    private GridView _gridView;
    private AppGroupsAdapter _adapter;
    private Logger _logger;
    private ScreenFactory _sf;
    private SwipeRefreshLayout _pullToRefresh;
    private View _mProgressBar;
    private Handler _mHandler;
    private  boolean _mInitializing = true;
    private TimerTask _mTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appgroup_grid);

        _logger = Logger.getInstance("FILES");
        _mHandler = new Handler();
        _pullToRefresh = findViewById(R.id.pull_to_refresh);
        _pullToRefresh.setOnRefreshListener(() -> refresh());
        _gridView = findViewById(R.id.grid_view);
        _sf = ScreenFactory.getInstance();

        _mProgressBar = findViewById(R.id.progress_bar);

        requestStoragePermission();
        initialize();
    }

    private void showProgressBar() {
        _gridView.setAlpha(0);
        _mProgressBar.setVisibility(View.VISIBLE);
        _mTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (!_mInitializing) {
                        hideProgressBar();
                    }
                });
            }
        };
        new Timer().scheduleAtFixedRate(_mTask, 0, PROGRESS_BAR_PERIOD);

    }

    private void hideProgressBar() {
        _mTask.cancel();
        _mProgressBar.animate().alpha(0).setDuration(PROGRESS_BAR_TRANSITION);
        _gridView.animate().alpha(1).setDuration(PROGRESS_BAR_TRANSITION);
        attachAdapter();
    }

    private void initialize() {
        showProgressBar();
        _logger.log("MainActivity: Initializing Screenshots");
        _sf.refresh(getApplicationContext(), () -> postInitialization());
    }

    private void refresh() {
        _logger.log("MainActivity: Refreshing Screenshots");
        _sf.refresh(getApplicationContext(), () -> postRefresh());
    }

    private void postInitialization() {
        _logger.log("MainActivity: Successfully initialized screenshots");
        _mInitializing = false;
    }

    private void postRefresh() {
        _logger.log("MainActivity: Successfully refreshed screenshots");
        _pullToRefresh.setRefreshing(false);
        attachAdapter();
    }

    public void attachAdapter() {
        ArrayList<Screenshot> mascots = new ArrayList<>();
        ArrayList<AppGroup> appgroups = _sf.getAppGroups(Utils.SortingCriterion.Date);
        for (AppGroup ag: appgroups)
            mascots.add(ag.mascot);
        for (Screenshot s: mascots)
            _logger.log(s.appName,s.name);
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
