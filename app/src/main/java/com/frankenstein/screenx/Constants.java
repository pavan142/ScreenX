package com.frankenstein.screenx;

public class Constants {
    // TODO (packageNames can also include underscores in them, so instead of suffix pattern-matching, implement prefix matching (Screenshot_<time>_<packagename>.jpg)
    public static final String SCREENSHOT_PATTERN="_[a-z0-9.]*.jpg$";
    public static final String SCREENSHOT_DIR = "ScreenX";
    public static final int PROGRESSBAR_TRANSITION = 200;
    public static final int TOOLBAR_TRANSITION = 100;
    public static final String DB_NAME="screenshots-db";
    public static final String DB_THREAD_NAME ="db-thread";
}
