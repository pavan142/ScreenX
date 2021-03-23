package com.frankenstein.screenx;

import android.app.Application;

import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.helper.ScreenshotParser;
import com.frankenstein.screenx.helper.TextHelper;

public class ScreenXApplication extends Application {

    private Logger _mLogger = Logger.getInstance("ScreenXApplication");

    @Override
    public void onCreate() {
        super.onCreate();
        ScreenshotParser.init(getApplicationContext());
        TextHelper.init(getApplicationContext());
    }
}
