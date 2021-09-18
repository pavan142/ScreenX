package com.frankenstein.screenx;

import android.app.Application;

import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.helper.ScreenshotParser;
import com.frankenstein.screenx.helper.TextHelper;

public class ScreenXApplication extends Application {

    private Logger _mLogger = Logger.getInstance("ScreenXApplication");

    public static ScreenFactory screenFactory;
    public static ScreenshotParser screenshotParser;
    public static TextHelper textHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        _mLogger.log("onCreate: ", Utils.timePassed());
        // Singleton objects that live through out the application lifecycle
        // Don't change this order, as they have are ordered as per the
        // dependency chain, texthelper and screenshotparser both depend on
        // screenFactory
        screenFactory = ScreenFactory.init(getApplicationContext());
        textHelper = TextHelper.init(getApplicationContext());
        screenshotParser = ScreenshotParser.init(getApplicationContext());
    }
}
