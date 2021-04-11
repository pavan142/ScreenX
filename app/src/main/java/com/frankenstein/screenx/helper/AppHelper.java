package com.frankenstein.screenx.helper;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.frankenstein.screenx.Constants;
import com.frankenstein.screenx.models.Screenshot;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppHelper {
    private static final Map<String, String> _packageToAppName = new HashMap<>();
    private static final Logger _mLogger = Logger.getRawInstance("AppHelper");

    private static String getAppName(PackageManager _pm, String packageId) {
        if (!_packageToAppName.containsKey(packageId)) {
            _mLogger.d("Did not find in packageToAppName", packageId);
            ApplicationInfo ai;
            try {
                ai = _pm.getApplicationInfo( packageId, 0);
            } catch (final PackageManager.NameNotFoundException e) {
                _mLogger.d("PackageId not found", packageId);
                ai = null;
            }
            final String appName = (String) (ai != null ? _pm.getApplicationLabel(ai) : heuristicAppName(packageId));
            _packageToAppName.put(packageId, appName);
        }
        return _packageToAppName.get(packageId);
    }

    private static String heuristicAppName(String packageId) {
        Matcher matcher = Constants.HEURISTIC_APPNAME_PATTERN.matcher(packageId);
        if (!matcher.find()) {
            return Constants.SCREENSHOT_DEFAULT_APPGROUP;
        }
        String matched = matcher.group();
        // The maximum length is to stop it recognizing from hashed screenshots
        // like on Realme devices: Example: Screenshot_<time>_ae8e34gq5wgwgw43t314teggqwffae12.jpg
        if (matched.length() < 2 || matched.length() > 24)
            return Constants.SCREENSHOT_DEFAULT_APPGROUP;

        String appName = matched.substring(0, 1).toUpperCase() + matched.substring(1);
        _mLogger.log("Returning Heuristic App Name", appName);
        return appName;
    }

    private static String getSourceApp(Context context, String filename) {
        final PackageManager _pm = context.getPackageManager();
        Matcher matcher = Constants.SCREENSHOT_PREFIX_PATTERN.matcher(filename);

        if (!matcher.find()) {
            _mLogger.d("No Match Found", filename);
            return Constants.SCREENSHOT_DEFAULT_APPGROUP;
        }

        String matched = matcher.group();

        // JPG, PNG extensions
        int endIndex = filename.length() - 4;
        int startIndex = matched.length();
        if (endIndex <= startIndex) {
            _mLogger.d("No Match Found", filename);
            return Constants.SCREENSHOT_DEFAULT_APPGROUP;
        }
        String packageId = filename.substring(startIndex, endIndex);
        String appName = getAppName(_pm, packageId);
        appName = (appName == "") ? Constants.SCREENSHOT_DEFAULT_APPGROUP : appName;
        return appName;
    }


    public static Screenshot GetScreenFromFile(Context context, File file) {
        String fileName = file.getName();
        String appName = getSourceApp(context, fileName);
        Screenshot screen = new Screenshot(fileName, file.getAbsolutePath(), appName);
        return screen;
    }
}
