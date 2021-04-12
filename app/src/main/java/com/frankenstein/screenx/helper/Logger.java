package com.frankenstein.screenx.helper;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class Logger {
    private String tag;
    private static final Map<String, Logger> instanceMap = new HashMap<String, Logger>();
    private static final String TAG_PREFIX="SCREENX-";
    public static Logger getInstance(String tag) {
        tag = TAG_PREFIX + tag;
        if (!instanceMap.containsKey(tag)) {
            instanceMap.put(tag, new Logger(tag));
        }
        return instanceMap.get(tag);
    }

    public static Logger getRawInstance(String tag) {
        if (!instanceMap.containsKey(tag)) {
            instanceMap.put(tag, new Logger(tag));
        }
        return instanceMap.get(tag);
    }

    protected Logger(String tag) {
        this.tag = tag;
    }

    protected String joinArgs(Object... args) {
        String message = "";
        for (int i = 0; i < args.length; i++) {
            message += args[i] + "\t";
        };
        return message;
    }

    public void d(Object... args) {
        Log.d(this.tag, joinArgs(args));
    }

    public void i(Object... args) {
        Log.i(this.tag, joinArgs(args));
    }

    public void w(Object... args) {
        Log.w(this.tag, joinArgs(args));
    }

    public void v(Object... args) {
        Log.v(this.tag, joinArgs(args));
    }

    public void e(Object... args) {
        Log.e(this.tag, joinArgs(args));
    }

    public void log(Object... args) {
        Log.i(this.tag, joinArgs(args));
    }
}
