package com.frankenstein.screenx;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.MediaStore;

import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.helper.PermissionHelper;
import com.frankenstein.screenx.models.AppGroup;
import com.frankenstein.screenx.models.Screenshot;
import com.frankenstein.screenx.multithreading.GetScreensAsyncTask;
import com.google.firebase.perf.metrics.AddTrace;

import androidx.lifecycle.MutableLiveData;

import static com.frankenstein.screenx.helper.AppHelper.processNewScreen;
import static com.frankenstein.screenx.helper.SortHelper.DESC_TIME;

public class ScreenFactory {
    private static ScreenFactory _instance;

    public Map<String, AppGroup> appgroups = new HashMap<>();
    private Map<String, Screenshot> nameToScreen = new HashMap<>();
    public MutableLiveData<ArrayList<Screenshot>> screenshots = new MutableLiveData<>();

    public ArrayList<AppGroup> dateSorted = new ArrayList<>();
    public ArrayList<AppGroup> alphaSorted = new ArrayList<>();

    private boolean _initialized = false;
    private final Logger _logger = Logger.getInstance("ScreenFactory");
    private boolean _monitoring = false;
    private Handler _monitorHandler;
    private Handler _mainHandler;
    private HandlerThread _monitorThread;
    private Context _context;
    private static final String MONITOR_THREAD_NAME = "MONITOR_THREAD_NAME";

    public static ScreenFactory init(Context context) {
        if (_instance != null)
            return _instance;
        _instance = new ScreenFactory(context);
        return _instance;
    }

    public static ScreenFactory getInstance() {
        return _instance;
    }

    private ScreenFactory(Context context) {
        _context = context;
        _monitorThread = new HandlerThread(MONITOR_THREAD_NAME);
        _monitorThread.start();
        _monitorHandler = new Handler(_monitorThread.getLooper());
        _mainHandler = new Handler(Looper.getMainLooper());
        startMonitor();
    }

    @AddTrace(name = "analyze_all_screens")
    public void analyzeScreens(ArrayList<Screenshot> screens) {
        ArrayList<Screenshot> newScreenshots = new ArrayList<>();
        for (AppGroup ag: appgroups.values())
            ag.screenshots.clear();
        nameToScreen.clear();
        try {
            for (Screenshot screen: screens) {
                addScreen(screen);
                newScreenshots.add(screen);
            }
            sort();
            screenshots.postValue(newScreenshots);
        } catch (Exception e) {
            _logger.log("got an error: ", e.getMessage());
        }
    }

    public void sort() {
        try {
            ArrayList<AppGroup> newDateSorted = new ArrayList<>();
            ArrayList<AppGroup> newAlphaSorted = new ArrayList<>();

            for (AppGroup ag : appgroups.values()) {
                if (ag.screenshots.size() == 0) {
                    appgroups.remove(ag.appName);
                    continue;
                }
                ag.sort();
                ag.mascot = ag.screenshots.get(0);
                ag.lastModified = ag.mascot.lastModified;
            }

            for (AppGroup i: appgroups.values()) {
                newDateSorted.add(i);
                newAlphaSorted.add(i);
            }

            DESC_TIME(newDateSorted);

            Collections.sort(newAlphaSorted, (AppGroup appGroup, AppGroup t1) -> {
                return appGroup.appName.compareTo(t1.appName);
            });

            // dateSorted and alphaSorted are not thread safe. They are directly used by adapter to populate
            // the views on main thread.
            // So we create new arrays, populate them and then assign it to the dateSorted and alphaSorted
            // had we gone the way of dateSorted.clear(), or alphaSorted.clear(), the adapter
            // views will be broken when that happens. With this flow, the array reference
            // held by the adapter is still valid. The adapter's array will only be reassigned when
            // the Livedata event(from MutableLiveData<screenshots> is fired onto the main thread)
            dateSorted = newDateSorted;
            alphaSorted = newAlphaSorted;
        } catch (Exception e) {
            _logger.log("Sort: got an error", e.getMessage());
        }

    }

    public void addScreen(Screenshot screen) {
        try {
            String appName = screen.appName;
            AppGroup ag;
            if (!appgroups.containsKey(appName)) {
                ag = new AppGroup(appName);
                appgroups.put(appName, ag);
            }
            ag = appgroups.get(appName);
            ag.screenshots.add(screen);
            nameToScreen.put(screen.name, screen);
        } catch(Exception e) {
            _logger.log("Error in AddScreen", e.getMessage());
        }

    }

    public void onScreenAdded(Context context, String filepath) {
        _monitorHandler.post(() -> {
            if (screenshots.getValue() == null)
                return;
            _logger.log("ScreenFactory: onScreenAdded", filepath);
            File file = new File(filepath);
            if (nameToScreen.containsKey(file.getName())) {
                _logger.log("new screen already exists", filepath);
                return;
            }
            Screenshot screen = processNewScreen(context, file);
            addScreen(screen);
            ScreenXApplication.textHelper.updateScreenAppName(screen);
            ArrayList<Screenshot> newScreenshots = screenshots.getValue();
            newScreenshots.add(screen);

            sort();
            _logger.log("Posting Screenshots value to livedata on UI Thread");

            _mainHandler.post(() -> {
                screenshots.postValue(newScreenshots);
            });
        });
    }

    public ArrayList<AppGroup> getAppGroups(Utils.SortingCriterion sort) {
        if(sort == Utils.SortingCriterion.Date)
            return dateSorted;
        return alphaSorted;
    }

    public Screenshot findScreenByName(String name) {
        return nameToScreen.get(name);
    }

    public void removeScreen(String name) {
        nameToScreen.remove(name);
        ScreenXApplication.textHelper.deleteScreenshotFromUI(name);
    }

    public void removeScreenList(ArrayList<String> toBeRemoved) {
        for (String name: toBeRemoved)
            nameToScreen.remove(toBeRemoved);
        ScreenXApplication.textHelper.deleteScreenshotListFromUI(toBeRemoved);
    }

    public void loadScreens(Context context) {
        if(_initialized)
            return;
        new GetScreensAsyncTask().execute(context);
        _initialized = true;
    }

    public void refresh(Context context) {
        _logger.log("received refresh request", Thread.currentThread().toString());
        _initialized = false;
        this.loadScreens(context);
        this.startMonitor();
    }

    public void startMonitor() {
        if (_monitoring)
            return;
        _monitoring = true;
        _logger.log("Starting Image Monitoring");
        _context.getContentResolver().registerContentObserver(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true,
                new ContentObserver(_monitorHandler) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        if (uri == null)
                            return;
                        _logger.log("New Image Detected", uri.toString(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        super.onChange(selfChange);

                        if (!PermissionHelper.hasStoragePermission(_context))
                            return;

                        if (!uri.toString().contains(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())) {
                            return;
                        }

                        Cursor cursor =  _context.getContentResolver().query(uri,
                                new String[]{MediaStore.Images.Media.DISPLAY_NAME,
                                        MediaStore.Images.Media.DATA,
                                        MediaStore.Images.Media.DATE_ADDED},
                                null,
                                null,
                                MediaStore.Images.Media.DATE_ADDED + " DESC"
                        );
                        if (cursor.moveToFirst()) {
                            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                            if (isScreenshot(path)) {
                                _logger.log("It's a screenshot", path);
                                onScreenAdded(_context, path);
                            }
                        }
                    }
                }
        );
    }

    private boolean isScreenshot(String path) {
        File file = new File(path);
        return (file.getAbsolutePath().contains("Screenshot") ||
                file.getAbsolutePath().contains("screenshot"));
    }
}
