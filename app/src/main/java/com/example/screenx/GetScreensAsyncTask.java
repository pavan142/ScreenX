package com.example.screenx;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GetScreensAsyncTask extends AsyncTask<Object, Void, ArrayList<Screenshot>> {

    private final Map<String, String> _packageToAppName = new HashMap<>();
    private Logger _mLogger;

    protected ScreensFetchedListener mlistener;

    public GetScreensAsyncTask() {
        super();
        _mLogger = Logger.getInstance("FILES_IO");
    }

    private String getAppName(PackageManager _pm, String packageId) {
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

    private String getSourceApp(Context context, String filename) {
        final PackageManager _pm = context.getPackageManager();
        Pattern pattern = Pattern.compile(Constants.SCREENSHOT_PATTERN);
        Matcher matcher = pattern.matcher(filename);

        if (matcher.find()) {
            String matched = matcher.group();
            String packageId = matched.substring(1, matched.length() - 4);
            String appName = getAppName(_pm, packageId);
            appName = (appName == "") ? "Miscellaneous" : appName;
            return appName;
        }
        return "Miscellaneous";
    }

    @Override
    protected ArrayList<Screenshot> doInBackground(Object ...objects) {
        final Context context = (Context) objects[0];
        mlistener = (ScreensFetchedListener) objects[1];

        ArrayList<Screenshot> screens = new ArrayList<>();
        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/DCIM/Screenshots";
            _mLogger.log("Path: ", path);
            File directory = new File(path);

            if (directory.exists())
                _mLogger.log("the directory exists: ", directory.getAbsolutePath());
            else
                _mLogger.log("the directory does not exist: " + directory.getAbsolutePath());

            _mLogger.log("Size: ", directory.canRead(), directory.canWrite(), directory.canExecute());
            File[] files = directory.listFiles();
            _mLogger.log("Size: "+ files.length);

            for (File file : files) {
                String fileName = file.getName();
                String appName = getSourceApp(context, fileName);
                String filePath = path + "/" + fileName;
                Screenshot screen = new Screenshot(fileName, filePath, appName);
                screens.add(screen);
            }

            ScreenFactory.getInstance().analyzeScreens(screens);
        } catch (Exception e) {
            _mLogger.log("got an error: ", e.getMessage());
        }
        return screens;
    }

    @Override
    protected void onPostExecute(ArrayList<Screenshot> screens) {
        mlistener.onScreensFetched(screens);
    }
}
