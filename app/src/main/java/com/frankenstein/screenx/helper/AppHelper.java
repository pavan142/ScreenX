package com.frankenstein.screenx.helper;

import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.frankenstein.screenx.Constants;
import com.frankenstein.screenx.interfaces.TimeSortable;
import com.frankenstein.screenx.models.Screenshot;
import com.frankenstein.screenx.helper.UsageStatsHelper.Companion.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import static com.frankenstein.screenx.Constants.SCREENSHOT_DEFAULT_APPGROUP;
import static com.frankenstein.screenx.helper.SortHelper.ASC_TIME;
import static com.frankenstein.screenx.helper.TimeHelper.getReadableTime;

public class AppHelper {
    private static final Map<String, String> _packageToAppName = new HashMap<>();
    private static final Logger _mLogger = Logger.getRawInstance("AppHelper");

    private static String getAppName(PackageManager _pm, String packageId) {
        if (!_packageToAppName.containsKey(packageId)) {
            ApplicationInfo ai;
            try {
                ai = _pm.getApplicationInfo( packageId, 0);
            } catch (final PackageManager.NameNotFoundException e) {
                ai = null;
            }
            final String appName = (String) (ai != null ? _pm.getApplicationLabel(ai) : heuristicAppName(packageId));
            if (appName == null)
                return null;
            _packageToAppName.put(packageId, appName);
        }
        return _packageToAppName.get(packageId);
    }

    private static String heuristicAppName(String packageId) {
        Matcher matcher = Constants.HEURISTIC_APPNAME_PATTERN.matcher(packageId);
        if (!matcher.find()) {
            return null;
        }
        String matched = matcher.group();
        // The maximum length is to stop it recognizing from hashed screenshots
        // like on Realme devices: Example: Screenshot_<time>_ae8e34gq5wgwgw43t314teggqwffae12.jpg
        if (matched.length() < 2 || matched.length() > 24)
            return null;

        String appName = matched.substring(0, 1).toUpperCase() + matched.substring(1);
        _mLogger.log("Returning Heuristic App Name", appName);
        return appName;
    }

    private static void assignAppNamesViaUsageEvents(Context context, ArrayList<Screenshot> screens) {
        _mLogger.log("Starting assignAppNamesViaUsageEvents");
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        PackageManager pm = context.getPackageManager();
        ArrayList<ForeGroundAppEvent> fgEvents = UsageStatsHelper.getArchivedFgEventTimeline(usm);
        ArrayList<CompoundEvent> allEvents = new ArrayList<>();

        for (Screenshot screen: screens)
            allEvents.add(new CompoundEvent(screen));
        for (ForeGroundAppEvent event: fgEvents)
            allEvents.add(new CompoundEvent(event));


        // (TODO) ASC_TIME is misbehaving for some reason
//        ASC_TIME(allEvents);
        Collections.sort(allEvents, (compoundEvent, t1) -> {
            long result = t1.lastModified - compoundEvent.lastModified;
            int output = (result <= 0 ) ? 1: -1;
            return output;
        });
        ForeGroundAppEvent lastFgEvent = null;
        for (int i = 0; i < allEvents.size(); i++) {
            CompoundEvent event = allEvents.get(i);
            if (!event.isScreen) {
                lastFgEvent = event.fgEvent;
            } else if (i > 0 && lastFgEvent != null) {
                String lastSeenAppName = getAppName(pm, lastFgEvent.getMPackageName());
                event.screen.appName = lastSeenAppName == null ? SCREENSHOT_DEFAULT_APPGROUP: lastSeenAppName;
            } else {
                event.screen.appName = SCREENSHOT_DEFAULT_APPGROUP;
            }
        }
    }

    private static String getSourceApp(Context context, String filename) {
        PackageManager pm = context.getPackageManager();
        Matcher matcher = Constants.SCREENSHOT_PREFIX_PATTERN.matcher(filename);

        if (!matcher.find()) {
            return null;
        }

        String matched = matcher.group();

        // JPG, PNG extensions
        int endIndex = filename.length() - 4;
        int startIndex = matched.length();
        if (endIndex <= startIndex) {
            return null;
        }
        String packageId = filename.substring(startIndex, endIndex);
        String appName = getAppName(pm, packageId);
        appName = (appName.compareTo("") == 0) ? null : appName;
        return appName;
    }

    public static ArrayList<Screenshot> GetMultipleScreens(Context context, ArrayList<File> files) {
        ArrayList <Screenshot> screens = new ArrayList<>();
        ArrayList <Screenshot> nullAppScreens = new ArrayList<>();
        for (File file: files) {
            String fileName = file.getName();
            String appName = getSourceApp(context, fileName);
            Screenshot screen = new Screenshot(fileName, file.getAbsolutePath(), appName);
            screens.add(screen);
            if (appName == null) {
                nullAppScreens.add(screen);
            }
        }
        _mLogger.log("NullAppScreens Length", nullAppScreens.size());
        assignAppNamesViaUsageEvents(context, nullAppScreens);
        return screens;
    }


    public static Screenshot GetScreenFromFile(Context context, File file) {
        String fileName = file.getName();
        String appName = getSourceApp(context, fileName);
        Screenshot screen = new Screenshot(fileName, file.getAbsolutePath(), appName);
        return screen;
    }

    static class CompoundEvent extends TimeSortable {
        public Boolean isScreen = false;
        public long lastModified;
        public Screenshot screen;
        public ForeGroundAppEvent fgEvent;

        public CompoundEvent(ForeGroundAppEvent _fgEvent) {
            isScreen = false;
            fgEvent = _fgEvent;
            lastModified = fgEvent.getMTimestamp();
        }

        public CompoundEvent(Screenshot _screen) {
            isScreen = true;
            screen = _screen;
            lastModified = screen.lastModified;
        }
    }
}
