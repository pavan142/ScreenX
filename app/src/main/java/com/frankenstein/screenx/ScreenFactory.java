package com.frankenstein.screenx;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;

import com.frankenstein.screenx.helper.TextHelper;
import com.frankenstein.screenx.helper.UsageStatsHelper;
import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.models.AppGroup;
import com.frankenstein.screenx.models.Screenshot;
import com.frankenstein.screenx.multithreading.GetScreensAsyncTask;

import androidx.lifecycle.MutableLiveData;

import static com.frankenstein.screenx.helper.AppHelper.getScreenFromFile;

public class ScreenFactory {
    private static ScreenFactory _instance;

    public Map<String, AppGroup> appgroups = new HashMap<>();
    private Map<String, Screenshot> nameToScreen = new HashMap<>();
    public MutableLiveData<ArrayList<Screenshot>> screenshots = new MutableLiveData<>();

    public ArrayList<AppGroup> dateSorted = new ArrayList<>();
    public ArrayList<AppGroup> alphaSorted = new ArrayList<>();

    private boolean _initialized = false;
    private final Logger _logger = Logger.getInstance("ScreenFactory");
    private static final Logger _mTimeLogger = Logger.getInstance("TIME");

    public static ScreenFactory init(Context context) {
        if (_instance != null)
            return _instance;
        _instance = new ScreenFactory();
        return _instance;
    }

    public static ScreenFactory getInstance() {
        return _instance;
    }

    private ScreenFactory() {}

    public void analyzeScreens(ArrayList<Screenshot> screens) {
        Long start = System.currentTimeMillis();
        _logger.log("Analyzing screens", Thread.currentThread().toString());
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
            Long end = System.currentTimeMillis();
            _mTimeLogger.log("Posting Screenshots value to livedata on UI Thread, Time taken for analyze Screens is", (end-start));
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


            Collections.sort(newDateSorted, (AppGroup appGroup, AppGroup t1) -> {
                long result = (t1.lastModified - appGroup.lastModified);
                int output = (result >=0 ) ? 1: -1;
                return output;
            });

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

    public void onScreenAdded(Context context,String filepath) {
        _logger.log("ScreenFactory: onScreenAdded", filepath);
        File file = new File(filepath);
        Screenshot screen = getScreenFromFile(context, file);
        addScreen(screen);

        ArrayList<Screenshot> newScreenshots = screenshots.getValue();
        newScreenshots.add(screen);

        sort();
        _logger.log("Posting Screenshots value to livedata on UI Thread");
        screenshots.postValue(newScreenshots);
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
    }

    public void removeScreenList(ArrayList<String> toBeRemoved) {
        for (String name: toBeRemoved)
            nameToScreen.remove(toBeRemoved);
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
    }
}
