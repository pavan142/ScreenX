package com.frankenstein.screenx;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;

class Logger {
    private String tag;
    private static final Map<String, Logger> instanceMap = new HashMap<String, Logger>();

    public static Logger getInstance(String tag) {
        if (!instanceMap.containsKey(tag)) {
            instanceMap.put(tag, new Logger(tag));
        }
        return instanceMap.get(tag);
    }

    private Logger(String tag) {
        this.tag = tag;
    }

    public void d(Object... args) {
        String message = "";
        for (int i = 0; i < args.length; i++) {
            message += args[i] + "\t";
        }
        Log.i(this.tag, message);
    }

    public void i(Object... args) {
        String message = "";
        for (int i = 0; i < args.length; i++) {
            message += args[i] + "\t";
        }
        Log.i(this.tag, message);
    }

    public void w(Object... args) {
        String message = "";
        for (int i = 0; i < args.length; i++) {
            message += args[i] + "\t";
        }
        Log.i(this.tag, message);
    }

    public void v(Object... args) {
        String message = "";
        for (int i = 0; i < args.length; i++) {
            message += args[i] + "\t";
        }
        Log.i(this.tag, message);
    }

    public void e(Object... args) {
        String message = "";
        for (int i = 0; i < args.length; i++) {
            message += args[i] + "\t";
        }
        Log.i(this.tag, message);
    }

    public void log(Object... args) {
        String message = "";
        for (int i = 0; i < args.length; i++) {
            message += args[i] + "\t";
        }
        Log.i(this.tag, message);
    }
}
