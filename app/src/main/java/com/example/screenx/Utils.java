package com.example.screenx;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.InvocationTargetException;

class Utils {
    public enum SortingCriterion {
        Alphabetical,
        Date
    }

    private static final Utils _mInstance = new Utils();
    private Context context;
    private Logger _logger;

    public static Utils getInstance() {
        return _mInstance;
    }

    private Utils(){
        _logger = Logger.getInstance("UTILS");
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isNavigationBarOnSide () {
        Point appUsableSize = getAppUsableScreenSize();
        Point realScreenSize = getRealScreenSize();
        return appUsableSize.x < realScreenSize.x;
    }

    public boolean isNavigationBarOnBottom () {
        Point appUsableSize = getAppUsableScreenSize();
        Point realScreenSize = getRealScreenSize();
        return appUsableSize.y < realScreenSize.y;
    }


    public int getNavigationBarHeight() {
        Point appUsableSize = getAppUsableScreenSize();
        Point realScreenSize = getRealScreenSize();

        _logger.log("Usable size: ", appUsableSize.x, appUsableSize.y, "real size", realScreenSize.x, realScreenSize.y);
        if (isNavigationBarOnBottom()) {
            return (realScreenSize.y - appUsableSize.y);
        }

        // navigation bar is not present or is on side
        return 0;
    }

    public Point getAppUsableScreenSize() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public Point getRealScreenSize() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size);
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (IllegalAccessException e) {} catch (InvocationTargetException e) {} catch (NoSuchMethodException e) {}
        }

        return size;
    }
}
