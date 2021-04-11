package com.frankenstein.screenx;

import java.util.regex.Pattern;

public class Constants {
    public static final Pattern SCREENSHOT_SUFFIX_PATTERN = Pattern.compile("_[a-z0-9.]*.jpg$");
    public static final Pattern SCREENSHOT_PREFIX_PATTERN = Pattern.compile("^Screenshot_[0-9-]*_");
    public static final Pattern HEURISTIC_APPNAME_PATTERN = Pattern.compile("[a-zA-Z0-9]*$");
    public static final String SCREENSHOT_DIR = "ScreenX";
    public static final int PROGRESSBAR_TRANSITION = 700;
    public static final int TOOLBAR_TRANSITION = 100;
    public static final String DB_NAME="screenshots-db";
    public static final String DB_THREAD_NAME ="db-thread";
    public static final String FILE_PROVIDER_AUTHORITY="com.frankenstein.screenx.fileprovider";
    public static final String SCREENSHOT_DEFAULT_APPGROUP = "Miscellaneous";
}
