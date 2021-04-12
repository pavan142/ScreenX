package com.frankenstein.screenx.helper;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class TimeLogger extends Logger {
    private String tag;
    private static final  TimeLogger _mInstance = new TimeLogger();
    private static final String TIME_TAG = "SCREENX-TIME";

    private final long _mStart = System.currentTimeMillis();
    private long _mPrevTime;
    public static TimeLogger getInstance() {
         return _mInstance;
    }

    private TimeLogger() {
        super(TIME_TAG);
        _mPrevTime = System.currentTimeMillis();
    }

    private String getTimeString() {
        long currTime = System.currentTimeMillis();
        long delta = currTime - _mPrevTime;
        _mPrevTime = currTime;
        String timePassed = "@" + (currTime - _mStart)+ "ms" + "\t";
        timePassed += "~" + delta + "ms";
        return timePassed;
    }

    public void d(Object ... args) {
        super.d(getTimeString(),  joinArgs(args));
    }

    public void i(Object... args) {
        super.i(getTimeString(),  joinArgs(args));
    }

    public void w(Object... args) {
        super.w(getTimeString(),  joinArgs(args));
    }

    public void v(Object... args) {
        super.v(getTimeString(),  joinArgs(args));
    }

    public void e(Object... args) {
        super.e(getTimeString(),  joinArgs(args));
    }

    public void log(Object... args) {
        super.i(getTimeString(),  joinArgs(args));
    }
}
