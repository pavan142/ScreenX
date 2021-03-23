package com.frankenstein.screenx.helper;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.frankenstein.screenx.Constants;
import com.frankenstein.screenx.models.Screenshot;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppHelper {
    private static final Map<String, String> _packageToAppName = new HashMap<>();
    private static final Logger _mLogger = Logger.getRawInstance("AppHelper");

    private static String getAppName(PackageManager _pm, String packageId) {
        if (!_packageToAppName.containsKey(packageId)) {
            ApplicationInfo ai;
            try {
                ai = _pm.getApplicationInfo( packageId, 0);
            } catch (final PackageManager.NameNotFoundException e) {
                _mLogger.d("NameNotFound", packageId);
                ai = null;
            }
            // (TODO: If appName comes out to be null for some reason, as a hail mary, we could take the last part of the packagename `com.example.android.chrome` -> chrome`
            final String appName = (String) (ai != null ? _pm.getApplicationLabel(ai) : "");
            _packageToAppName.put(packageId, appName);
        }
        return _packageToAppName.get(packageId);
    }


    private static String getSourceApp(Context context, String filename) {
        final PackageManager _pm = context.getPackageManager();
        Pattern pattern = Pattern.compile(Constants.SCREENSHOT_PREFIX_PATTERN);
        Matcher matcher = pattern.matcher(filename);

        if (!matcher.find()) {
            _mLogger.d("No Match Found", filename);
            return "Miscellaneous";
        }

        String matched = matcher.group();
        int endIndex = filename.length() - 4;
        int startIndex = matched.length();
        if (endIndex <= startIndex) {
            _mLogger.d("No Match Found", filename);
            return "Miscellaneous";
        }
        String packageId = filename.substring(startIndex, endIndex);
        String appName = getAppName(_pm, packageId);
        appName = (appName == "") ? "Miscellaneous" : appName;
        return appName;
    }


    public static Screenshot getScreenFromFile(Context context, File file) {
        String fileName = file.getName();
        String appName = getSourceApp(context, fileName);
        Screenshot screen = new Screenshot(fileName, file.getAbsolutePath(), appName);
        return screen;
    }
}
