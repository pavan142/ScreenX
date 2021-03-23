package com.frankenstein.screenx.helper;

import android.content.Context;

import com.frankenstein.screenx.models.Screenshot;
import com.frankenstein.screenx.workers.ScreenshotParserWorker;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

public class ScreenshotParser {

    private static ScreenshotParser _instance;

    public static void init(Context context) {
        ScreenshotParser._instance = new ScreenshotParser(context);
    }

    public static ScreenshotParser getInstance() {
        return ScreenshotParser._instance;
    }

    Context _mContext;
    public ScreenshotParser(Context context) {
        this._mContext = context;
    }

    public void parse() {
        WorkRequest parseRequest = new OneTimeWorkRequest.Builder(ScreenshotParserWorker.class).build();
        WorkManager.getInstance(_mContext).enqueue(parseRequest);
    }
}
