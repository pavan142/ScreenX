package com.example.screenx;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ScreenFactory {
    private static ScreenFactory _instance;

    public Map<String, AppGroup> appgroups = new HashMap<>();
    public Map<String, Screenshot> nameToScreen = new HashMap<>();

    private ArrayList<AppGroup> dateSorted = new ArrayList<>();
    private ArrayList<AppGroup> alphaSorted = new ArrayList<>();

    private boolean _initialized = false;
    private final Logger _logger = Logger.getInstance("ScreenFactory");
    private final Map<String, String> _packageToAppName = new HashMap<>();
    private final PackageManager _pm;

    public static ScreenFactory getInstance(Context context) {
        if (ScreenFactory._instance == null) {
            ScreenFactory._instance = new ScreenFactory(context);
        }
        return ScreenFactory._instance;
    }
    private ScreenFactory(Context context) {
        _pm = context.getPackageManager();
    }

    private String getAppName(String packageId) {
        if (!_packageToAppName.containsKey(packageId)) {
            ApplicationInfo ai;
            try {
                ai = _pm.getApplicationInfo( packageId, 0);
            } catch (final PackageManager.NameNotFoundException e) {
                ai = null;
            }
            final String appName = (String) (ai != null ? _pm.getApplicationLabel(ai) : "");
            _packageToAppName.put(packageId, appName);
        }
        return _packageToAppName.get(packageId);
    }

    private String getSourceApp(String filename) {
        Pattern pattern = Pattern.compile("_[[a-z]*.]*.jpg$");
        Matcher matcher = pattern.matcher(filename);

        if (matcher.find()) {
            String matched = matcher.group();
            String packageId = matched.substring(1, matched.length() - 4);
            String appName = getAppName(packageId);
            appName = (appName == "") ? "Miscellaneous" : appName;
            return appName;
        }
        return "Miscellaneous";
    }

    private void analyzeFiles() {
        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/DCIM/Screenshots";
            _logger.log("Path: ", path);
            File directory = new File(path);

            if (directory.exists())
                _logger.log("the directory exists: ", directory.getAbsolutePath());
            else
                _logger.log("the directory does not exist: " + directory.getAbsolutePath());

            _logger.log("Size: ", directory.canRead(), directory.canWrite(), directory.canExecute());
            File[] files = directory.listFiles();
            _logger.log("Size: "+ files.length);

            for (File file : files) {
                String fileName = file.getName();
                String appName = getSourceApp(fileName);
                AppGroup ag;
                if (!appgroups.containsKey(appName)) {
                    ag = new AppGroup(appName);
                    appgroups.put(appName, ag);
                }
                ag = appgroups.get(appName);
                String filePath = path + "/" + fileName;
                Screenshot screen = new Screenshot(fileName, filePath, appName);
                ag.screenshots.add(screen);
                nameToScreen.put(screen.name, screen);
            }

            for (AppGroup ag : appgroups.values()) {
                ag.sort();
                ag.mascot = ag.screenshots.get(0);
                ag.lastModified = ag.mascot.file.lastModified();
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
        Collections.sort(dateSorted, new Comparator<AppGroup>() {
            @Override
            public int compare(AppGroup appGroup, AppGroup t1) {
                long result = (t1.lastModified - appGroup.lastModified);
                int output = (result >=0 ) ? 1: -1;
                return output;
            }
        });

        Collections.sort(alphaSorted, new Comparator<AppGroup>() {
            @Override
            public int compare(AppGroup appGroup, AppGroup t1) {
                return appGroup.appName.compareTo(t1.appName);
            }
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

    public void initialize() {
        if(_initialized)
            return;
        analyzeFiles();
        _initialized = true;
    }
}
