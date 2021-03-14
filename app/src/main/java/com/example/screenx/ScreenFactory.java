package com.example.screenx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;

class ScreenFactory {
    private static ScreenFactory _instance;

    public Map<String, AppGroup> appgroups = new HashMap<>();
    public Map<String, Screenshot> nameToScreen = new HashMap<>();

    private ArrayList<AppGroup> dateSorted = new ArrayList<>();
    private ArrayList<AppGroup> alphaSorted = new ArrayList<>();

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
        dateSorted.clear();
        alphaSorted.clear();
        for (AppGroup ag: appgroups.values())
            ag.screenshots.clear();
        nameToScreen.clear();
        try {
            for (Screenshot screen: screens) {
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

            for (AppGroup ag : appgroups.values()) {
                if (ag.screenshots.size() == 0) {
                    appgroups.remove(ag.appName);
                    continue;
                }
                ag.sort();
                ag.mascot = ag.screenshots.get(0);
                ag.lastModified = ag.mascot.lastModified;
//                _files.log(ag.print());
            }
            sort();
        } catch (Exception e) {
            _logger.log("got an error: ", e.getMessage());
        }
    }

    public void sort() {
        for (AppGroup i: appgroups.values()) {
            dateSorted.add(i);
            alphaSorted.add(i);
        }

        Collections.sort(dateSorted, (AppGroup appGroup, AppGroup t1) -> {
                long result = (t1.lastModified - appGroup.lastModified);
                int output = (result >=0 ) ? 1: -1;
                return output;
            });

        Collections.sort(alphaSorted, (AppGroup appGroup, AppGroup t1) -> {
                return appGroup.appName.compareTo(t1.appName);
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

    public void loadScreens(Context context, ScreenSortListener screenSortListener) {
        if(_initialized)
            return;
        ScreensFetchedListener listener = (screens) -> {
                screenSortListener.onSorted();
        };
        new GetScreensAsyncTask().execute(context, listener);
        _initialized = true;
    }

    public void refresh(Context context, ScreenSortListener screenSortListener) {
        _initialized = false;
        this.loadScreens(context, screenSortListener);
    }
}
