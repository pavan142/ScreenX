package com.frankenstein.screenx;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;

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
    public Map<String, Screenshot> nameToScreen = new HashMap<>();

    public MutableLiveData<ArrayList<AppGroup>> dateSorted = new MutableLiveData<>();
    public MutableLiveData<ArrayList<AppGroup>> alphaSorted = new MutableLiveData<>();

    private boolean _initialized = false;
    private final Logger _logger = Logger.getInstance("ScreenFactory");

    public static ScreenFactory getInstance() {
        if (ScreenFactory._instance == null) {
            ScreenFactory._instance = new ScreenFactory();
        }
        return ScreenFactory._instance;
    }
    private ScreenFactory() {}

    public void analyzeScreens(ArrayList<Screenshot> screens) {
        for (AppGroup ag: appgroups.values())
            ag.screenshots.clear();
        nameToScreen.clear();
        try {
            for (Screenshot screen: screens) {
             addScreen(screen);
            }
            sort();
        } catch (Exception e) {
            _logger.log("got an error: ", e.getMessage());
        }
    }

    public void sort() {
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

        dateSorted.postValue(newDateSorted);
        alphaSorted.postValue(newAlphaSorted);
    }

    public void addScreen(Screenshot screen) {
        String appName = screen.appName;
        AppGroup ag;
        if (!appgroups.containsKey(appName)) {
            ag = new AppGroup(appName);
            appgroups.put(appName, ag);
        }
        ag = appgroups.get(appName);
        ag.screenshots.add(screen);
        nameToScreen.put(screen.name, screen);
    }

    public void onScreenAdded(Context context,String filepath) {
        _logger.log("ScreenFactory: onScreenAdded", filepath);
        File file = new File(filepath);
        addScreen(getScreenFromFile(context, file));
        sort();
    }

    public ArrayList<AppGroup> getAppGroups(Utils.SortingCriterion sort) {
        if(sort == Utils.SortingCriterion.Date)
            return dateSorted.getValue();
        return alphaSorted.getValue();
    }

    public Screenshot findScreenByName(String name) {
        return nameToScreen.get(name);
    }

    public void removeScreen(String name) {
        nameToScreen.remove(name);
    }

    public void loadScreens(Context context, ScreenRefreshListener screenSortListener) {
        if(_initialized)
            return;
        GetScreensAsyncTask.ScreensFetchedListener listener = (screens) -> {
                screenSortListener.onRefresh();
        };
        new GetScreensAsyncTask().execute(context, listener);
        _initialized = true;
    }

    public void refresh(Context context, ScreenRefreshListener screenSortListener) {
        _initialized = false;
        this.loadScreens(context, screenSortListener);
    }

    public interface ScreenRefreshListener {
        void onRefresh();
    }
}
