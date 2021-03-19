package com.frankenstein.screenx;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;

import com.frankenstein.screenx.helper.PermissionHelper;
import com.frankenstein.screenx.models.AppGroup;
import com.frankenstein.screenx.models.Screenshot;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import static com.frankenstein.screenx.Constants.PROGRESSBAR_PERIOD;
import static com.frankenstein.screenx.Constants.PROGRESSBAR_TRANSITION;
import static com.frankenstein.screenx.helper.FileHelper.CUSTOM_SCREENSHOT_DIR;
import static com.frankenstein.screenx.helper.FileHelper.createIfNot;

public class MainActivity extends AppCompatActivity {

    private GridView _mGridView;
    private AppGroupsAdapter _adapter;
    private Logger _logger;
    private ScreenFactory _sf;
    private SwipeRefreshLayout _pullToRefresh;
    private View _mProgressBar;
    private View _mPermissionsDisplay;
    private View _mPermissionsText;

    private boolean _mPermissionsDenied = false;
    private Handler _mHandler;
    private boolean _mInitializing = true;
    private TimerTask _mTask;
    public Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appgroup_grid);

        utils = Utils.getInstance();
        utils.setContext(getApplicationContext());
        _logger = Logger.getInstance("FILES");
        _logger.log("----------MainActivity: ONCREATE---------");
        _mHandler = new Handler();
        _pullToRefresh = findViewById(R.id.pull_to_refresh);
        _pullToRefresh.setOnRefreshListener(() -> refresh());
        _mGridView = findViewById(R.id.grid_view);
        _sf = ScreenFactory.getInstance();

        _mProgressBar = findViewById(R.id.progress_bar);
        _mPermissionsDisplay = findViewById(R.id.permissions_display);
        _mPermissionsDisplay.setVisibility(View.GONE);
        _mPermissionsText = findViewById(R.id.permissions_text);
        _mPermissionsText.setOnClickListener(view -> goToAppSettings());

        requestStoragePermission();
    }

    private void goToAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void showProgressBar() {
        _mGridView.setAlpha(0);
        _mGridView.setVisibility(View.VISIBLE);
        _mPermissionsDisplay.setVisibility(View.GONE);
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
        new Timer().scheduleAtFixedRate(_mTask, 0, PROGRESSBAR_PERIOD);
    }

    private void hideProgressBar() {
        _mTask.cancel();
        _mProgressBar.animate().alpha(0).setDuration(PROGRESSBAR_TRANSITION);
        _mGridView.animate().alpha(1).setDuration(PROGRESSBAR_TRANSITION);
        _mHandler.postDelayed(() -> _mProgressBar.setVisibility(View.GONE), 1500);
        attachAdapter();
    }

    private void showPermissionDeniedError() {
        _mPermissionsDenied = true;
        _mPermissionsDisplay.setVisibility(View.VISIBLE);
        _mProgressBar.setVisibility(View.GONE);
        _mGridView.setVisibility(View.GONE);
    }

    private void permissionsGranted() {
        _mPermissionsDenied = false;
        showProgressBar();
        _logger.log("MainActivity: Initializing Screenshots");
        createIfNot(CUSTOM_SCREENSHOT_DIR);
        _sf.refresh(getApplicationContext(), () -> postInitialization());
        _logger.log("MainActivity: Launching ScreenXService");
        if (!PermissionHelper.hasOverlayPermission(this))
            PermissionHelper.requestOverlayPermission(this, 1000);
        else
            _logger.log("MainActivity: Has permission for overlay");
        Intent intent = new Intent(this, ScreenXService.class);
        intent.setAction(ScreenXService.ACTION_ENABLE_SERVICE);
        startForegroundService(intent);
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
        for (AppGroup ag : appgroups)
            mascots.add(ag.mascot);
//        for (Screenshot s : mascots)
//            _logger.log(s.appName, s.name);
        _adapter = new AppGroupsAdapter(getApplicationContext(), mascots);
        _mGridView.setAdapter(_adapter);
        _mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        // Reading only read storage because read storage is grouped under the same umbrella as
        // write storage and if the user accepted one , the other would be automatically granted
        _logger.log("Dexter checking for permissions");
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        _logger.log("Permission Granted");
                        permissionsGranted();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        _logger.log("Permission Denied");
                        showPermissionDeniedError();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        _logger.log("Permission Rational Should be shown");
                        showPermissionDeniedError();
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && _mPermissionsDenied &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
            permissionsGranted();
        }
    }
}
