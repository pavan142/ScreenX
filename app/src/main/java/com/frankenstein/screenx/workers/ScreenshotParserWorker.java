package com.frankenstein.screenx.workers;

import android.content.Context;

import com.frankenstein.screenx.helper.Logger;
import com.frankenstein.screenx.helper.ScreenshotParser;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ScreenshotParserWorker extends Worker {
    private Logger _mLogger = Logger.getInstance("ScreenshotParserWoker");
    public ScreenshotParserWorker(Context context, WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        _mLogger.log("Doing work", Thread.currentThread().toString());
        return Result.success();
    }
}
