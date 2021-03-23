package com.frankenstein.screenx;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import android.content.Intent;
import android.widget.RelativeLayout;

import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.helper.PermissionHelper;
import com.frankenstein.screenx.helper.ScreenshotParser;
import com.frankenstein.screenx.models.AppGroup;
import com.frankenstein.screenx.models.Screenshot;
import com.frankenstein.screenx.ui.adapters.HomePageAdapter;

import static com.frankenstein.screenx.Constants.PROGRESSBAR_TRANSITION;
import static com.frankenstein.screenx.helper.FileHelper.CUSTOM_SCREENSHOT_DIR;
import static com.frankenstein.screenx.helper.FileHelper.createIfNot;

public class MainActivity extends AppCompatActivity {

    private GridView _mGridView;
    private HomePageAdapter _adapter;
    private Logger _mLogger;
    private ScreenFactory _sf;
    private SwipeRefreshLayout _pullToRefresh;
    private View _mProgressBar;
    private View _mPermissionsDisplay;
    private View _mStoragePermissionsView;
    private View _mUsagePermissionsView;
    private View _mOverlayPermissionsView;

    private boolean _mPermissionsDenied = false;
    private Handler _mHandler;
    private boolean _mInitializing = true;
    private boolean mGridInitialized = false;
    private boolean _mRefreshInProgress = false;
    private boolean _mSortByDate = true;
    private boolean _mPaused = false;
    public Utils utils;

    private FrameLayout _mHomePageContent;
    private RelativeLayout _mHomePageContentEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        utils = Utils.getInstance();
        utils.setContext(getApplicationContext());
        _mLogger = Logger.getInstance("MainActivity");
        _mLogger.log("-----ONCREATE------");
        _mHandler = new Handler(Looper.myLooper());
        _pullToRefresh = findViewById(R.id.pull_to_refresh);
        _pullToRefresh.setOnRefreshListener(() -> refresh(this::postRefresh));

        _mGridView = findViewById(R.id.grid_view);
        _mHomePageContent = findViewById(R.id.homepage_content);
        _mHomePageContentEmpty = findViewById(R.id.homepage_content_empty);

        _sf = ScreenFactory.getInstance();
        _sf.dateSorted.observe(this, this::dateSortedListChanged);
        _sf.alphaSorted.observe(this, this::alphaSortedListChanged);

        _mProgressBar = findViewById(R.id.progress_bar);

        _mPermissionsDisplay = findViewById(R.id.permissions_display);

        _mStoragePermissionsView = findViewById(R.id.storage_permissions);
        _mStoragePermissionsView.setOnClickListener(view -> goToStorageSettings());

        _mUsagePermissionsView = findViewById(R.id.usage_permissions);
        _mUsagePermissionsView.setOnClickListener(view -> goToUsageSettings());

        _mOverlayPermissionsView = findViewById(R.id.overlay_permissions);
        _mOverlayPermissionsView.setOnClickListener(view -> goToOverlaySettings());

        _mHomePageContentEmpty.setVisibility(View.GONE);
        _mProgressBar.setVisibility(View.GONE);
        _mPermissionsDisplay.setVisibility(View.GONE);
        checkPermissions();
    }

    private void startScreenXService() {
        Intent intent = new Intent(this, ScreenXService.class);
        intent.setAction(ScreenXService.ACTION_ENABLE_SERVICE);
        startForegroundService(intent);
    }

    private void goToStorageSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void goToUsageSettings() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void goToOverlaySettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void hideEmptyPage() {
        _mLogger.log("Hiding Empty Page");
        _pullToRefresh.setVisibility(View.VISIBLE);
        _mHomePageContentEmpty.setVisibility(View.GONE);
    }

    private void showEmptyPage() {
        _mLogger.log("Showing Empty Page");
        _mHomePageContentEmpty.setVisibility(View.VISIBLE);
        _pullToRefresh.setVisibility(View.GONE);
    }

    private void alphaSortedListChanged(ArrayList<AppGroup> appgroups) {
        _mLogger.log("MainActivity: alphaSortedListChanged");
        if (_mRefreshInProgress ||_mSortByDate)
            return;
        attachAdapter();
    }

    private void dateSortedListChanged(ArrayList<AppGroup> appgroups) {
        _mLogger.log("MainActivity: dateSortedListChanged");
        if (_mRefreshInProgress || !_mSortByDate)
            return;
        attachAdapter();
    }

    private void showProgressBar() {
        _mHomePageContent.setAlpha(0);
        _mHomePageContent.setVisibility(View.VISIBLE);

        _mPermissionsDisplay.setVisibility(View.GONE);

        _mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mGridInitialized = true;
        _mProgressBar.animate().alpha(0).setDuration(PROGRESSBAR_TRANSITION);
        _mHomePageContent.animate().alpha(1).setDuration(PROGRESSBAR_TRANSITION);
        _mHandler.postDelayed(() -> _mProgressBar.setVisibility(View.GONE), 1500);
        attachAdapter();

        _mLogger.log("current thread is", Thread.currentThread().toString());
        ScreenshotParser.getInstance().parse();
    }

    private void showPermissionRequestScreen() {
        _mPermissionsDenied = true;
        _mPermissionsDisplay.setVisibility(View.VISIBLE);
        _mProgressBar.setVisibility(View.GONE);
        _mHomePageContent.setVisibility(View.GONE);
    }

    private void permissionsGranted() {
        _mPermissionsDenied = false;
        showProgressBar();
        _mLogger.log("MainActivity: Initializing Screenshots");
        createIfNot(CUSTOM_SCREENSHOT_DIR);
        refresh(this::postInitialization);
        _mLogger.log("MainActivity: Launching ScreenXService");
        if (!PermissionHelper.hasOverlayPermission(this))
            PermissionHelper.requestOverlayPermission(this, 1000);
        else
            _mLogger.log("MainActivity: Has permission for overlay");
        startScreenXService();
    }

    private void refresh(ScreenFactory.ScreenRefreshListener listener) {
        _mLogger.log("MainActivity: Refreshing Screenshots");
        _mRefreshInProgress = true;
        _sf.refresh(getApplicationContext(), () -> {
            _mRefreshInProgress = false;
            listener.onRefresh();
        });
    }

    private void postInitialization() {
        _mLogger.log("MainActivity: Successfully initialized screenshots");
        _mInitializing = false;
        hideProgressBar();
    }

    private void postRefresh() {
        _mLogger.log("MainActivity: Successfully refreshed screenshots");
        _pullToRefresh.setRefreshing(false);
        attachAdapter();
    }

    public void attachAdapter() {
        ArrayList<Screenshot> mascots = new ArrayList<>();
        Utils.SortingCriterion criterion = (_mSortByDate) ? Utils.SortingCriterion.Date: Utils.SortingCriterion.Alphabetical;
        ArrayList<AppGroup> appgroups = _sf.getAppGroups(criterion);
        if (appgroups.size() == 0) {
          showEmptyPage();
          return;
        }

        for (AppGroup ag : appgroups)
            mascots.add(ag.mascot);
//        for (Screenshot s : mascots)
//            _logger.log(s.appName, s.name);
        _adapter = new HomePageAdapter(getApplicationContext(), mascots);
        _mGridView.setAdapter(_adapter);
        _mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Screenshot selected = mascots.get(i);
                _mLogger.log("The app group selected is", i, "the appName: ", selected.appName);
                Intent intent = new Intent(getBaseContext(), AppGroupActivity.class);
                intent.putExtra("APP_GROUP_NAME", selected.appName);
                startActivity(intent);
            }
        });
        hideEmptyPage();
    }

/*
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
                        showPermissionRequestScreen();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        _logger.log("Permission Rational Should be shown");
                        showPermissionRequestScreen();
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }
*/

    public void checkPermissions() {
        boolean storagePermissions = PermissionHelper.hasStoragePermission(this);
        boolean usagePermissions = PermissionHelper.hasUsagePermission(this);
        boolean overlayPermissions = PermissionHelper.hasOverlayPermission(this);

        if (storagePermissions && usagePermissions && overlayPermissions) {
            _mLogger.log("Has all the permissions");
            permissionsGranted();
            return;
        }

        _mLogger.log("Permissions Missing:: storage ->", storagePermissions, "  usage ->", usagePermissions, "overlay ->", overlayPermissions);
        showPermissionRequestScreen();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && _mPermissionsDenied) {
            checkPermissions();
        }
    }

    @Override
    protected void onResume() {
        if (mGridInitialized)
            refresh(this::postRefresh);
        super.onResume();

        // This step is to reinitialize the floating touch bar, once it is closed
        // TODO: Start the service with that specific intent itself, instead of generic intent
        if (_mPaused)
            startScreenXService();
        _mPaused = false;
    }

    @Override
    protected void onPause() {
        _mPaused = true;
        super.onPause();
    }
}
